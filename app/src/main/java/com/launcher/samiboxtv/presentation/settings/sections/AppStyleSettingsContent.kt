package com.launcher.samiboxtv.presentation.settings.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.domain.model.AppCardStyle
import com.launcher.samiboxtv.presentation.home.HomeUiEvent
import com.launcher.samiboxtv.presentation.settings.components.SettingsActionItem
import com.launcher.samiboxtv.presentation.settings.components.tvClickable
import com.launcher.samiboxtv.presentation.theme.AppLayoutMode
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

@Composable
fun AppStyleSettingsSection(
    currentLayoutMode: AppLayoutMode,
    currentStyle: AppCardStyle,
    showAppNames: Boolean,
    onEvent: (HomeUiEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "style_header_density") {
            Text(
                text = "MODO DE DISEÑO Y DENSIDAD RESPONSIVA (ZERO PARTIAL CARDS):",
                color = CyberCyan,
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        items(items = AppLayoutMode.entries, key = { "style_mode_${it.name}" }) { mode ->
            val isSelected = currentLayoutMode == mode
            var isFocused by remember { mutableStateOf(false) }
            val shape = RoundedCornerShape(10.dp)

            val borderColor = when {
                isFocused -> CyberCyan
                isSelected -> CyberAmber
                else -> CyberCyan.copy(alpha = 0.25f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (isFocused) Color(0xFF14203B) else Color(0xFF0C1425))
                    .border(width = if (isFocused || isSelected) 2.dp else 1.dp, color = borderColor, shape = shape)
                    .onFocusChanged { isFocused = it.isFocused }
                    .tvClickable { onEvent(HomeUiEvent.SetAppLayoutMode(mode)) }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.title,
                            color = if (isFocused) CyberCyan else Color.White,
                            fontSize = 13.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = mode.description,
                            color = Color(0xFFA6C5E2),
                            fontSize = 10.sp,
                            fontFamily = ShareTechMonoFontFamily
                        )
                    }

                    Text(
                        text = if (isSelected) "● ACTIVO" else "○ ELEGIR",
                        color = if (isSelected) CyberAmber else CyberGrey,
                        fontSize = 11.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item(key = "style_toggle_names") {
            Spacer(modifier = Modifier.height(6.dp))
            SettingsActionItem(
                title = "MOSTRAR NOMBRES DE LAS APLICACIONES",
                subtitle = if (showAppNames) {
                    "[✓ VISIBLE] Muestra el nombre/título de cada app en su tarjeta"
                } else {
                    "[○ SOLO ICONOS] Modo minimalista: oculta los nombres y muestra solo el icono"
                },
                isHighlighted = showAppNames,
                onClick = { onEvent(HomeUiEvent.ToggleShowAppNames) }
            )
        }
    }
}