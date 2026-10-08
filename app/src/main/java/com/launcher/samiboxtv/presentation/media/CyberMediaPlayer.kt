package com.launcher.samiboxtv.presentation.media

import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Text
import com.launcher.samiboxtv.domain.model.MediaFile
import com.launcher.samiboxtv.domain.model.MediaType
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Reproductor de video y audio nativo optimizado para Android TV y bajo consumo de RAM.
 * Soporta control D-Pad completo y HUD cyberpunk superpuesto.
 */
@OptIn(UnstableApi::class)
@Composable
fun CyberMediaPlayer(
    mediaFile: MediaFile,
    onClose: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    // Instancia única y local de ExoPlayer (se libera estrictamente en DisposableEffect)
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }
    var isHudVisible by remember { mutableStateOf(true) }
    var hudCountdown by remember { mutableStateOf(5) }

    // Interceptar botón BACK nativo del control remoto
    BackHandler {
        exoPlayer.stop()
        onClose()
    }

    // Configurar medio a reproducir
    LaunchedEffect(mediaFile.path) {
        val item = MediaItem.fromUri(mediaFile.uri)
        exoPlayer.setMediaItem(item)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    // Listener para actualizar estado de reproducción y duración
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    totalDurationMs = exoPlayer.duration.coerceAtLeast(0L)
                } else if (state == Player.STATE_ENDED) {
                    onPlayNext()
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // Loop de actualización periódica de la barra de progreso
    LaunchedEffect(isPlaying) {
        while (isActive) {
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            if (exoPlayer.duration > 0) {
                totalDurationMs = exoPlayer.duration
            }
            delay(500)
        }
    }

    // Temporizador de auto-ocultación del HUD en video
    LaunchedEffect(isHudVisible, hudCountdown) {
        if (isHudVisible && isPlaying && mediaFile.type == MediaType.VIDEO) {
            delay(1000)
            if (hudCountdown > 0) {
                hudCountdown--
            } else {
                isHudVisible = false
            }
        }
    }

    // Solicitar foco al iniciar para capturar D-Pad
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key.nativeKeyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            if (isPlaying) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                            }
                            isHudVisible = true
                            hudCountdown = 5
                            true
                        }

                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                            isHudVisible = true
                            hudCountdown = 5
                            true
                        }

                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(
                                if (totalDurationMs > 0) totalDurationMs else Long.MAX_VALUE
                            )
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                            isHudVisible = true
                            hudCountdown = 5
                            true
                        }

                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (mediaFile.type == MediaType.AUDIO) {
                                onPlayPrevious()
                            } else {
                                isHudVisible = true
                                hudCountdown = 6
                            }
                            true
                        }

                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (mediaFile.type == MediaType.AUDIO) {
                                onPlayNext()
                            } else {
                                isHudVisible = !isHudVisible
                                if (isHudVisible) hudCountdown = 5
                            }
                            true
                        }

                        KeyEvent.KEYCODE_BACK -> {
                            exoPlayer.stop()
                            onClose()
                            true
                        }

                        else -> false
                    }
                } else false
            }
    ) {
        if (mediaFile.type == MediaType.VIDEO) {
            // Render de video nativo
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Interfaz dedicada de Audio / Música Cyberpunk
            AudioVisualizerLayout(
                mediaFile = mediaFile,
                isPlaying = isPlaying,
                onPrevious = onPlayPrevious,
                onNext = onPlayNext
            )
        }

        // HUD OSD flotante superior e inferior
        AnimatedVisibility(
            visible = isHudVisible || mediaFile.type == MediaType.AUDIO,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerHudOverlay(
                mediaFile = mediaFile,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                totalDurationMs = totalDurationMs,
                onClose = {
                    exoPlayer.stop()
                    onClose()
                }
            )
        }
    }
}

@Composable
private fun AudioVisualizerLayout(
    mediaFile: MediaFile,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioPulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "discRotate"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0D1B36), Color(0xFF040710)),
                    radius = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Disco Cyber Neón giratorio
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF070D1B))
                    .border(2.5.dp, if (isPlaying) CyberCyan else CyberGrey, CircleShape)
                    .rotate(if (isPlaying) rotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                // Anillos concéntricos de vinilo
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .border(1.dp, CyberMagenta.copy(alpha = 0.5f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(CyberAmber.copy(alpha = 0.2f))
                        .border(1.5.dp, CyberAmber, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♫",
                        color = CyberAmber,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = mediaFile.name,
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "AUDIO REPRODUCTION // ${mediaFile.sizeMb} MB",
                color = CyberCyan,
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Atajos de control D-Pad
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[▲ PISTA ANTERIOR]",
                    color = CyberGrey,
                    fontSize = 10.sp,
                    fontFamily = ShareTechMonoFontFamily
                )
                Text(
                    text = if (isPlaying) "[OK: PAUSA]" else "[OK: PLAY]",
                    color = CyberAmber,
                    fontSize = 11.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "[▼ SIGUIENTE]",
                    color = CyberGrey,
                    fontSize = 10.sp,
                    fontFamily = ShareTechMonoFontFamily
                )
            }
        }
    }
}

@Composable
private fun PlayerHudOverlay(
    mediaFile: MediaFile,
    isPlaying: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Barra Superior: Título y Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (mediaFile.type == MediaType.VIDEO) "🎬 VIDEO" else "♫ AUDIO",
                    color = CyberMagenta,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = mediaFile.name,
                    color = Color.White,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "[ATRÁS / BACK: SALIR]",
                color = CyberAmber,
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Barra Inferior: Progreso OSD y Controles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .border(1.5.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(16.dp)
        ) {
            // Barra de progreso Neón
            val progress = if (totalDurationMs > 0) {
                (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF162540))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(CyberCyan)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Información de tiempo y controles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatTime(currentPositionMs)} / ${formatTime(totalDurationMs)}",
                    color = CyberCyan,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "◄ -10s",
                        color = CyberGrey,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp
                    )
                    Text(
                        text = if (isPlaying) "PAUSA [OK]" else "PLAY [OK]",
                        color = CyberAmber,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+10s ►",
                        color = CyberGrey,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "ESTADO: ${if (isPlaying) "REPRODUCIENDO" else "PAUSADO"}",
                    color = if (isPlaying) CyberCyan else CyberMagenta,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
