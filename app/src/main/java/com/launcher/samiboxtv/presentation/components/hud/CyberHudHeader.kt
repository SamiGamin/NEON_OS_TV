package com.launcher.samiboxtv.presentation.components.hud

import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.launcher.samiboxtv.BuildConfig
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CyberHudHeader(
    networkStatus: NetworkStatus,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bloque Izquierdo: Reloj + Telemetría de red
        Column {
            ClockView()
            Spacer(modifier = Modifier.height(4.dp))
            NetworkIndicator(networkStatus = networkStatus)
        }

        // Bloque Derecho: Branding y Botón de Ajustes
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.header_app_title),
                    color = CyberCyan,
                    fontSize = 18.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = stringResource(R.string.header_app_version, BuildConfig.VERSION_NAME),
                    color = CyberAmber,
                    fontSize = 10.sp,
                    fontFamily = ShareTechMonoFontFamily
                )
            }

            SettingsHudButton(onOpenSettings = onOpenSettings)
        }
    }
}



@Composable
fun SettingsHudButton(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .size(46.dp)
            .clip(shape)
            .background(if (isFocused) Color(0xFF1B2E54) else Color(0xFF0C1425))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) CyberAmber else CyberCyan.copy(alpha = 0.35f),
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    onOpenSettings()
                    true
                } else false
            }
            .focusable()
            .clickable { onOpenSettings() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_settings),
            contentDescription = "Ajustes",
            colorFilter = ColorFilter.tint(if (isFocused) CyberAmber else CyberCyan),
            modifier = Modifier.size(24.dp)
        )
    }
}