package com.launcher.samiboxtv.presentation.settings.sections

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.presentation.home.HomeUiEvent
import com.launcher.samiboxtv.presentation.home.HomeUiState
import com.launcher.samiboxtv.presentation.settings.components.SettingsActionItem
import com.launcher.samiboxtv.util.DefaultLauncherHelper
import com.launcher.samiboxtv.util.openTvSystemSettings

@Composable
fun SystemSettingsSection(
    uiState: HomeUiState,
    context: Context,
    onEvent: (HomeUiEvent) -> Unit,
    onOpenDefaultLauncherDialog: () -> Unit
) {
    val isAggressiveActive = remember { DefaultLauncherHelper.isAccessibilityServiceEnabled(context) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "sys_app_visibility") {
            SettingsActionItem(
                title = "GESTOR DE VISIBILIDAD DE APPS",
                subtitle = "Configurar qué aplicaciones instaladas se muestran u ocultan en el Launcher",
                isHighlighted = false,
                iconRes = R.drawable.ic_visibility,
                onClick = {
                    onEvent(HomeUiEvent.CloseSettings)
                    onEvent(HomeUiEvent.OpenAddDialog)
                }
            )
        }

        item(key = "sys_default_launcher") {
            SettingsActionItem(
                title = "ESTABLECER COMO LAUNCHER PREDETERMINADO",
                subtitle = if (isAggressiveActive) {
                    "[✓ ANCLAJE AGRESIVO ACTIVO] Clic para ver opciones del botón Home"
                } else {
                    "Forzar selector de Android o activar anclaje agresivo para TV Box"
                },
                isHighlighted = isAggressiveActive,
                iconRes = R.drawable.ic_home,
                onClick = onOpenDefaultLauncherDialog
            )
        }

        item(key = "sys_android_settings") {
            SettingsActionItem(
                title = "AJUSTES GENERALES DEL SISTEMA ANDROID",
                subtitle = "Abrir el panel de configuración de red, pantalla y bluetooth de la TV",
                isHighlighted = false,
                iconRes = R.drawable.ic_settings,
                onClick = {
                    onEvent(HomeUiEvent.CloseSettings)
                    openTvSystemSettings(context)
                }
            )
        }

        item(key = "sys_telemetry") {
            SettingsActionItem(
                title = "TELEMETRÍA Y LIMPIADOR DE RAM",
                subtitle = "Inspeccionar procesos de hardware y forzar liberación de memoria física",
                isHighlighted = false,
                iconRes = R.drawable.ic_bolt,
                onClick = {
                    onEvent(HomeUiEvent.CloseSettings)
                    onEvent(HomeUiEvent.OpenSystemLog)
                }
            )
        }

        item(key = "sys_updates") {
            SettingsActionItem(
                title = if (uiState.isCheckingUpdates) "BUSCANDO ACTUALIZACIONES..." else "BUSCAR ACTUALIZACIONES DE SAMIBOX TV",
                subtitle = when {
                    uiState.isCheckingUpdates -> "> Conectando con GitHub Releases y comprobando versión..."
                    !uiState.updateCheckMessage.isNullOrBlank() -> uiState.updateCheckMessage
                    else -> "Verificar si hay una nueva versión disponible en GitHub Releases"
                },
                isHighlighted = uiState.isCheckingUpdates,
                enabled = !uiState.isCheckingUpdates,
                iconRes = R.drawable.ic_system_update,
                onClick = { onEvent(HomeUiEvent.CheckUpdates) }
            )
        }
    }
}