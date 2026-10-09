package com.launcher.samiboxtv.presentation.components.cards

import android.content.Context
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.domain.model.AppCardStyle
import com.launcher.samiboxtv.presentation.components.cyberNeonGlow
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

/**
 * Helper para resolver dinámicamente recursos drawable por nombre sin romper
 * la compilación si un archivo es agregado o eliminado en runtime.
 */
private fun getDrawableResId(context: Context, vararg names: String): Int {
    val res = context.resources
    val pkg = context.packageName
    for (name in names) {
        val id = res.getIdentifier(name, "drawable", pkg)
        if (id != 0) return id
    }
    return 0
}

/**
 * Tarjeta interactiva Cyberpunk para gestionar aplicaciones (Mostrar / Ocultar apps).
 * Abre el diálogo AddAppDialog para controlar la visibilidad de apps instaladas.
 * Soporta carátula panorámica (baner_app.jpg / banner_app.jpg) o ícono retro centrado como fallback.
 */
@Composable
fun AddAppCyberCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp? = null,
    cardStyle: AppCardStyle = AppCardStyle.BANNER_16_9,
    focusRequester: FocusRequester? = null,
    showAppName: Boolean = true,
    onFocus: () -> Unit = {}
) {
    val context = LocalContext.current
    var isFocused by remember { mutableStateOf(false) }
    val cardFocusRequester = focusRequester ?: remember { FocusRequester() }

    val bannerAppResId = remember(context) {
        getDrawableResId(context, "baner_app", "banner_app", "bamer_app")
    }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "addAppCardScale"
    )

    val shape = RoundedCornerShape(10.dp)

    val sizeModifier = if (width != null && height != null) {
        Modifier
            .width(width)
            .height(height)
    } else {
        Modifier.aspectRatio(cardStyle.aspectRatio)
    }

    Box(
        modifier = modifier
            .then(sizeModifier)
            .focusRequester(cardFocusRequester)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onFocus()
                }
            }
            .focusable()
            .scale(scale)
            .cyberNeonGlow(
                isFocused = isFocused,
                glowColor = CyberAmber,
                cornerRadius = 10.dp,
                maxGlowRadius = 14.dp
            )
            .clip(shape)
            .background(if (isFocused) Color(0xFF19253F) else Color(0xFF090F1B))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) CyberAmber else CyberCyan.copy(alpha = 0.25f),
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp &&
                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    onClick()
                    true
                } else false
            }
    ) {
        if (bannerAppResId != 0) {
            // [1] BANNER PANORÁMICO DE GESTIONAR APPS (baner_app.jpg)
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(bannerAppResId)
                    .crossfade(false)
                    .build(),
                contentDescription = "Gestionar Aplicaciones",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Degradado inferior y títulos solo visibles si showAppName está activado
            if (showAppName) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 40f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "+ GESTIONAR APPS",
                        color = if (isFocused) CyberAmber else Color.White,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "CONFIG // MOSTRAR Y OCULTAR",
                        color = if (isFocused) CyberAmber.copy(alpha = 0.85f) else CyberCyan.copy(alpha = 0.70f),
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Badge superior derecho neón
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(0.8.dp, CyberAmber, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(CyberAmber)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CONFIG",
                        color = CyberAmber,
                        fontSize = 8.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // [2] FALLBACK: ÍCONO RETRO ADD (SI NO EXISTE EL BANNER)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(8.dp)
            ) {
                val saturationMatrix = remember(isFocused) {
                    ColorMatrix().apply {
                        if (!isFocused) setToSaturation(0f)
                    }
                }
                Image(
                    painter = painterResource(id = R.drawable.ic_add_retro),
                    contentDescription = "Gestionar Apps",
                    colorFilter = if (!isFocused) ColorFilter.colorMatrix(saturationMatrix) else ColorFilter.tint(CyberAmber),
                    modifier = Modifier
                        .size(if (!showAppName) 44.dp else 36.dp)
                        .alpha(if (isFocused) 1.0f else 0.6f)
                )
                if (showAppName) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "+ GESTIONAR APPS",
                        color = if (isFocused) CyberAmber else CyberGrey,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "MOSTRAR / OCULTAR",
                        color = if (isFocused) CyberAmber.copy(alpha = 0.85f) else CyberCyan.copy(alpha = 0.5f),
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}