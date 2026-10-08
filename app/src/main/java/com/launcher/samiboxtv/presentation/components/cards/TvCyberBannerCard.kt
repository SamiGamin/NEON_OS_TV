package com.launcher.samiboxtv.presentation.components.cards

import android.view.KeyEvent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.rememberAsyncImagePainter
import com.launcher.samiboxtv.domain.model.AppCardStyle
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

/**
 * Tarjeta interactiva de Android TV para aplicaciones con soporte de estilo dinámico,
 * banners 16:9 y modo fantasma interactivo en vivo estilo Projectivy Launcher.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvCyberBannerCard(
    appItem: AppItem,
    cardStyle: AppCardStyle = AppCardStyle.BANNER_16_9,
    isGhostMode: Boolean = false,
    isAnyAppMoving: Boolean = false,
    isEditing: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveDirection: (Int) -> Unit = {},
    onConfirmMove: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Al activarse el modo fantasma, asegurar foco inmediato en la tarjeta activa
    LaunchedEffect(isGhostMode) {
        if (isGhostMode) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    // Animación de pulso continuo activa ÚNICAMENTE cuando está en modo fantasma
    val ghostAlpha = rememberGhostPulseAlpha(isGhostMode)

    val scaleValue = when {
        isGhostMode -> 1.12f // Flotando por encima del resto
        isFocused -> 1.06f
        else -> 1.0f
    }

    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = if (isGhostMode) Color(0xFF1B0A26).copy(alpha = 0.85f) else Color(0xFF0C1322),
            focusedContainerColor = Color(0xFF142038)
        ),
        scale = CardDefaults.scale(
            scale = 1.0f,
            focusedScale = 1.0f
        ),
        border = CardDefaults.border(
            border = Border(
                border = BorderStroke(
                    width = if (isGhostMode) 2.5.dp else if (isEditing) 2.dp else 1.dp,
                    color = if (isGhostMode) CyberMagenta else if (isEditing) CyberAmber else CyberCyan.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp)
            ),
            focusedBorder = Border(
                border = BorderStroke(
                    width = if (isGhostMode) 2.5.dp else 2.dp,
                    color = if (isGhostMode) CyberMagenta else if (isEditing) CyberAmber else CyberCyan
                ),
                shape = RoundedCornerShape(8.dp)
            ),
            pressedBorder = Border(
                border = BorderStroke(2.dp, CyberMagenta),
                shape = RoundedCornerShape(8.dp)
            )
        ),
        modifier = modifier
            .focusRequester(focusRequester)
            .scale(scaleValue)
            .alpha(if (isGhostMode) ghostAlpha else if (isAnyAppMoving) 0.5f else 1.0f)
            .aspectRatio(cardStyle.aspectRatio)
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    if (isGhostMode) {
                        when (keyEvent.key.nativeKeyCode) {
                            // Mover a la izquierda en vivo
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                onMoveDirection(-1)
                                return@onKeyEvent true
                            }
                            // Mover a la derecha en vivo
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                onMoveDirection(1)
                                return@onKeyEvent true
                            }
                            // Fijar y confirmar posición
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_NUMPAD_ENTER,
                            KeyEvent.KEYCODE_BACK -> {
                                onConfirmMove()
                                return@onKeyEvent true
                            }
                        }
                    } else if (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                               keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                               keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                        onClick()
                        return@onKeyEvent true
                    }
                }
                false
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        ) {
            val showBanner = cardStyle != AppCardStyle.SQUARE_1_1 && appItem.bannerDrawable != null
            if (showBanner) {
                Image(
                    painter = rememberAsyncImagePainter(model = appItem.bannerDrawable),
                    contentDescription = appItem.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF1A2744), Color(0xFF0A0F1D))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = appItem.iconDrawable),
                        contentDescription = appItem.name,
                        modifier = Modifier.size(if (cardStyle == AppCardStyle.SQUARE_1_1) 50.dp else 44.dp)
                    )
                }
            }

            // Degradado inferior para legibilidad
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xDD040711)),
                            startY = 60f
                        )
                    )
            )

            // Indicador flotante cuando está en modo reordenamiento fantasma
            if (isGhostMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .border(1.dp, CyberMagenta, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "◄ MOVER ►",
                        color = CyberMagenta,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else if (isEditing) {
                // Indicador visual alternativo de edición
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberAmber)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⇄ MOVIENDO",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Título de la app
            Text(
                text = appItem.name.uppercase(),
                color = if (isGhostMode) CyberMagenta else if (isFocused) CyberCyan else Color.White,
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .then(if (isFocused) Modifier.basicMarquee() else Modifier)
            )
        }
    }
}

@Composable
private fun rememberGhostPulseAlpha(isGhostMode: Boolean): Float {
    if (!isGhostMode) return 1.0f
    val infiniteTransition = rememberInfiniteTransition(label = "ghostPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaPulse"
    )
    return alpha
}