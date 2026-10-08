package com.launcher.samiboxtv.presentation.components

import android.view.KeyEvent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.presentation.theme.AppLayoutMode
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Cabecera HUD futurista Cyberpunk para NEONOS TV.
 * Contiene:
 * - Izquierda: Título de plataforma y perfil de pantalla con conteo de nodos.
 * - Derecha: Reloj militar digital HH:mm:ss aislado en corrutina, fecha cibernética,
 *   estado SYS_ONLINE y acceso directo a Ajustes.
 */
@Composable
fun CyberHeaderHud(
    layoutMode: AppLayoutMode,
    appCount: Int,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 48.dp, top = 22.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LADO IZQUIERDO: Branding militar y perfil activo
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NEONOS // SAMIBOX TV",
                    color = CyberCyan,
                    fontSize = 19.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.5.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberCyan.copy(alpha = 0.15f))
                        .border(0.5.dp, CyberCyan, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "V1.0",
                        color = CyberCyan,
                        fontSize = 9.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Badge de perfil dinámico
            Text(
                text = "PROFILE: ${layoutMode.name} // NODES: $appCount",
                color = CyberAmber.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }

        // LADO DERECHO: Reloj HH:mm:ss, fecha, indicador de estado y botón de ajustes
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reloj militar y estado en tiempo real (aislado en su propio composable)
            CyberMilitaryClock()

            // Botón enfocado para Ajustes
            CyberSettingsButton(onClick = onOpenSettings)
        }
    }
}

/**
 * Reloj digital militar (HH:mm:ss) con aislamiento estricto de recomposiciones.
 * Se actualiza cada segundo sin recomponer el feed de aplicaciones.
 */
@Composable
private fun CyberMilitaryClock(modifier: Modifier = Modifier) {
    var timeString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
        val dateFormat = SimpleDateFormat("yyyy.MM.dd // EEE", Locale.US)
        val dateHolder = Date()

        while (true) {
            val now = System.currentTimeMillis()
            dateHolder.time = now
            timeString = timeFormat.format(dateHolder)
            dateString = dateFormat.format(dateHolder).uppercase()

            val millisToNextSecond = 1000L - (now % 1000L)
            delay(millisToNextSecond.coerceAtLeast(100L))
        }
    }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (timeString.isEmpty()) "--:--:--" else timeString,
                color = CyberCyan,
                fontSize = 24.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Indicador SYS_ONLINE con pulso luminoso
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF091423))
                    .border(0.5.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = dotAlpha))
                )
                Text(
                    text = "SYS_ONLINE",
                    color = CyberCyan,
                    fontSize = 8.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = dateString,
            color = CyberGrey,
            fontSize = 10.sp,
            fontFamily = ShareTechMonoFontFamily,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun CyberSettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(shape)
            .background(if (isFocused) Color(0xFF1B2E54) else Color(0xFF0C1425))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) CyberAmber else CyberCyan.copy(alpha = 0.35f),
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    onClick()
                    true
                } else false
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = "Ajustes",
            colorFilter = ColorFilter.tint(if (isFocused) CyberAmber else CyberCyan),
            modifier = Modifier.size(22.dp)
        )
    }
}
