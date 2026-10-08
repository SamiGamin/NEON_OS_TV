package com.launcher.samiboxtv.presentation.components

import android.graphics.Paint
import android.graphics.RectF
import android.view.KeyEvent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.VirtualApps
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCard
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

/**
 * Modificador de resplandor neón ultraligero que usa el shadow layer nativo del Canvas.
 * No genera recomposiciones pesadas ni caídas de frames en GPUs Mali/PowerVR (TV Box 1-2GB RAM).
 */
fun Modifier.cyberNeonGlow(
    isFocused: Boolean,
    glowColor: Color = CyberCyan,
    cornerRadius: Dp = 10.dp,
    maxGlowRadius: Dp = 14.dp
): Modifier = if (!isFocused) this else this.drawBehind {
    val radiusPx = cornerRadius.toPx()
    val glowPx = maxGlowRadius.toPx()
    val androidColor = glowColor.toArgb()

    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            isAntiAlias = true
            color = androidColor
            style = Paint.Style.STROKE
            strokeWidth = 2.dp.toPx()
            setShadowLayer(glowPx, 0f, 0f, androidColor)
        }
        val rect = RectF(0f, 0f, size.width, size.height)
        canvas.nativeCanvas.drawRoundRect(rect, radiusPx, radiusPx, paint)
    }
}

/**
 * Tarjeta cibernética responsiva para aplicaciones en NEONOS TV.
 * Cuenta con feedback D-Pad suave, micro-escalado (1.0f a 1.06f), aura neón
 * y decodificación de iconos estricta a 96x96 px con Coil para mínimo impacto de memoria.
 */
@Composable
fun CyberAppCard(
    app: AppItem,
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isFavorite: Boolean = app.isFavorite,
    isGhostMode: Boolean = false,
    isEditing: Boolean = false,
    onMoveDirection: (Int) -> Unit = {},
    onConfirmMove: () -> Unit = {}
) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = when {
            isGhostMode -> 1.10f
            isFocused -> 1.06f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "cyberCardScale"
    )

    val shape = RoundedCornerShape(10.dp)

    // Selección de color neón según estado y tipo de nodo
    val isSystemNode = app.packageName == VirtualApps.PKG_IPTV || app.packageName == VirtualApps.PKG_MEDIA_HUB
    val glowColor = when {
        isGhostMode -> CyberMagenta
        isSystemNode -> CyberCyan
        isFavorite -> CyberAmber
        else -> CyberCyan
    }

    val borderColor = when {
        isGhostMode -> CyberMagenta
        isFocused -> glowColor
        isEditing -> CyberAmber
        else -> Color(0xFF19243C)
    }

    val containerBg = when {
        isGhostMode -> Color(0xFF220B2E)
        isFocused -> Color(0xFF14203A)
        else -> CyberCard
    }

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .scale(scale)
            .cyberNeonGlow(
                isFocused = isFocused || isGhostMode,
                glowColor = glowColor,
                cornerRadius = 10.dp,
                maxGlowRadius = 14.dp
            )
            .clip(shape)
            .background(containerBg)
            .border(
                width = if (isFocused || isGhostMode) 2.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isGhostMode) onConfirmMove() else onClick()
            }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    if (isGhostMode) {
                        when (keyEvent.key.nativeKeyCode) {
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                onMoveDirection(-1)
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                onMoveDirection(1)
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_NUMPAD_ENTER,
                            KeyEvent.KEYCODE_BACK -> {
                                onConfirmMove()
                                return@onKeyEvent true
                            }
                        }
                    } else {
                        when (keyEvent.key.nativeKeyCode) {
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                onClick()
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_MENU -> {
                                onLongClick()
                                return@onKeyEvent true
                            }
                        }
                    }
                }
                false
            }
    ) {
        // Fondo con degradado ciberespacial sutil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x99050A14)
                        )
                    )
                )
        )

        // Contenido central: Icono + Nombre
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            val iconModel = remember(app) {
                ImageRequest.Builder(context)
                    .data(app.iconDrawable ?: app.bannerDrawable)
                    .size(96, 96)
                    .crossfade(false)
                    .build()
            }

            // Icono optimizado
            AsyncImage(
                model = iconModel,
                contentDescription = app.name,
                modifier = Modifier
                    .size(if (height >= 140.dp) 52.dp else 42.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Nombre de la app con tipografía monospace militar/cyberpunk
            Text(
                text = app.name.uppercase(),
                color = if (isFocused) Color.White else Color(0xFFC0D2E8),
                fontSize = if (height >= 140.dp) 11.sp else 10.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Badge superior izquierdo para Favoritos
        if (isFavorite) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberAmber.copy(alpha = 0.25f))
                    .border(0.5.dp, CyberAmber, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "★",
                    color = CyberAmber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Badge superior derecho para Apps Virtuales / Sistema
        if (isSystemNode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberCyan.copy(alpha = 0.20f))
                    .border(0.5.dp, CyberCyan, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SYS",
                    color = CyberCyan,
                    fontSize = 8.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Overlay cuando está en modo reordenamiento fantasma
        if (isGhostMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, CyberMagenta, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "◄ MOVER ►",
                    color = CyberMagenta,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
