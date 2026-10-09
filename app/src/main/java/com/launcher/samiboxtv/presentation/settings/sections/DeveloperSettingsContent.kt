package com.launcher.samiboxtv.presentation.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.launcher.samiboxtv.presentation.home.HomeUiEvent
import com.launcher.samiboxtv.presentation.home.HomeUiState
import com.launcher.samiboxtv.presentation.settings.components.SettingsActionItem

@Composable
fun DeveloperSettingsSection(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "dev_master") {
            SettingsActionItem(
                title = if (uiState.isDevModeActive) "MODO AVANZADO: [ACTIVADO]" else "MODO AVANZADO: [DESACTIVADO]",
                subtitle = "Habilita la captura profunda de eventos del sistema y control remoto",
                isHighlighted = uiState.isDevModeActive,
                onClick = { onEvent(HomeUiEvent.ToggleDevMode) }
            )
        }

        if (uiState.isDevModeActive) {
            item(key = "dev_server") {
                SettingsActionItem(
                    title = if (uiState.isLogServerRunning) "SERVIDOR DE RED: ACTIVO" else "INICIAR SERVIDOR DE LOGS EN RED",
                    subtitle = if (uiState.isLogServerRunning) "Entra en tu PC a: ${uiState.logServerUrl}" else "Transmite los logs de la TV por Wi-Fi al navegador de tu PC",
                    isHighlighted = uiState.isLogServerRunning,
                    onClick = { onEvent(HomeUiEvent.ToggleLogServer) }
                )
            }

            item(key = "dev_overlay") {
                SettingsActionItem(
                    title = if (uiState.showKeyDebugToast) "VISOR DE TECLAS OSD: [VISIBLE]" else "VISOR DE TECLAS OSD: [OCULTO]",
                    subtitle = "Muestra una alerta en pantalla cada vez que presionas un botón del control",
                    isHighlighted = uiState.showKeyDebugToast,
                    onClick = { onEvent(HomeUiEvent.ToggleKeyDebugToast) }
                )
            }

            item(key = "dev_clear_logs") {
                SettingsActionItem(
                    title = "BORRAR HISTORIAL DE LOGS",
                    subtitle = "Vacía el búfer de memoria de eventos registrados",
                    isHighlighted = false,
                    onClick = { onEvent(HomeUiEvent.ClearLogs) }
                )
            }
        }
    }
}