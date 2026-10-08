package com.launcher.samiboxtv.presentation.components.cards

import android.view.KeyEvent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.launcher.samiboxtv.domain.model.AppCardStyle
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

/**
 * Tarjeta interactiva Cyber IPTV para el feed principal
 * con soporte para ciclo de vida de Launcher TV (reordenamiento D-Pad y menú contextual).
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IptvCard(
    cardStyle: AppCardStyle = AppCardStyle.BANNER_16_9,
    channelCount: Int = 0,
    isLoading: Boolean = false,
    isGhostMode: Boolean = false,
    isAnyAppMoving: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onMoveDirection: (Int) -> Unit = {},
    onConfirmMove: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isGhostMode) {
        if (isGhostMode) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    val ghostAlpha by if (isGhostMode) {
        val transition = rememberInfiniteTransition(label = "iptvGhostPulse")
        transition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
            label = "iptvPulseAlpha"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val scaleValue = when {
        isGhostMode -> 1.12f
        isFocused -> 1.06f
        else -> 1.0f
    }

    val borderColor = when {
        isGhostMode -> CyberMagenta.copy(alpha = ghostAlpha)
        isFocused -> CyberAmber
        channelCount > 0 -> CyberCyan.copy(alpha = 0.6f)
        else -> Color(0xFF22324D)
    }

    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = if (isGhostMode) Color(0xFF1B0A26).copy(alpha = 0.85f) else Color(0xFF090F1C),
            focusedContainerColor = Color(0xFF131F38)
        ),
        scale = CardDefaults.scale(
            scale = 1.0f,
            focusedScale = 1.0f
        ),
        border = CardDefaults.border(
            border = Border(
                border = BorderStroke(if (isGhostMode) 2.5.dp else 1.dp, borderColor),
                shape = RoundedCornerShape(8.dp)
            ),
            focusedBorder = Border(
                border = BorderStroke(if (isGhostMode) 2.5.dp else 2.dp, if (isGhostMode) CyberMagenta else CyberAmber),
                shape = RoundedCornerShape(8.dp)
            ),
            pressedBorder = Border(
                border = BorderStroke(2.dp, CyberCyan),
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
                            KeyEvent.KEYCODE_MENU -> {
                                onLongClick()
                                return@onKeyEvent true
                            }
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                if (keyEvent.nativeKeyEvent.isLongPress) {
                                    onLongClick()
                                    return@onKeyEvent true
                                }
                                onClick()
                                return@onKeyEvent true
                            }
                        }
                    }
                }
                false
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isFocused) Color(0xFF1B2B4C) else Color(0xFF0D162B),
                            if (isFocused) Color(0xFF101C33) else Color(0xFF060B17)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📺 LIVE TV",
                        color = if (isFocused) CyberCyan else Color(0xFFA6C5E2),
                        fontSize = 11.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = when {
                            isGhostMode -> "⇄ MOVIENDO"
                            channelCount > 0 -> "● ONLINE"
                            else -> "○ M3U"
                        },
                        color = if (isGhostMode) CyberMagenta else if (channelCount > 0) CyberAmber else CyberGrey,
                        fontSize = 9.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "CYBER IPTV",
                        color = if (isFocused) Color.White else Color(0xFFE2E8F0),
                        fontSize = if (cardStyle == AppCardStyle.SQUARE_1_1) 12.sp else 13.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            isLoading -> "Cargando lista..."
                            channelCount > 0 -> "$channelCount Canales"
                            else -> "CONFIGURAR LISTA"
                        },
                        color = if (channelCount > 0) CyberCyan else CyberAmber,
                        fontSize = 10.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}