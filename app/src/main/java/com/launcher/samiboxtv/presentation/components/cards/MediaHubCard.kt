package com.launcher.samiboxtv.presentation.components.cards

import android.view.KeyEvent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Tarjeta de acceso directo para Cyber Media Hub en el feed principal.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MediaHubCard(
    cardStyle: AppCardStyle = AppCardStyle.BANNER_16_9,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = Color(0xFF070B16),
            focusedContainerColor = Color(0xFF10192E)
        ),
        scale = CardDefaults.scale(
            scale = 1.0f,
            focusedScale = 1.06f
        ),
        border = CardDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, CyberMagenta.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(8.dp)
            ),
            focusedBorder = Border(
                border = BorderStroke(2.dp, CyberMagenta),
                shape = RoundedCornerShape(8.dp)
            ),
            pressedBorder = Border(
                border = BorderStroke(2.dp, CyberCyan),
                shape = RoundedCornerShape(8.dp)
            )
        ),
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    onClick()
                    true
                } else false
            }
    ) {
        val aspectRatio = cardStyle.aspectRatio

        Box(
            modifier = Modifier
                .aspectRatio(aspectRatio)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (isFocused) Color(0xFF162544) else Color(0xFF091020),
                            if (isFocused) Color(0xFF0B1426) else Color(0xFF040812)
                        )
                    )
                )
                .padding(10.dp)
        ) {
            // Contenido central
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ícono Cyber de medios
                Text(
                    text = "▶ ☵ ♫",
                    color = if (isFocused) CyberCyan else CyberMagenta,
                    fontSize = if (cardStyle == AppCardStyle.SQUARE_1_1) 22.sp else 18.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "MEDIA HUB",
                    color = if (isFocused) Color.White else CyberCyan,
                    fontSize = if (cardStyle == AppCardStyle.SQUARE_1_1) 12.sp else 13.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "USB & LOCAL",
                    color = if (isFocused) CyberAmber else CyberGrey,
                    fontSize = 9.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Badge superior derecha
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isFocused) CyberMagenta.copy(alpha = 0.3f) else Color(0xFF131D33))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "EXPLORADOR",
                    color = if (isFocused) CyberMagenta else CyberCyan.copy(alpha = 0.7f),
                    fontSize = 7.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
