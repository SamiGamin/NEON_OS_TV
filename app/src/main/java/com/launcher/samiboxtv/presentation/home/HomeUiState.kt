package com.launcher.samiboxtv.presentation.home

import com.launcher.samiboxtv.core.base.UiState
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.domain.model.SystemTelemetry
import com.launcher.samiboxtv.domain.model.UpdateInfo

/**
 * Estado inmutable de la pantalla principal del Launcher con categorías HUD,
 * catálogo de aplicaciones, telemetría de red, hardware y actualizaciones.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredApps: List<AppItem> = emptyList(),
    val allApps: List<AppItem> = emptyList(),
    val hiddenApps: List<AppItem> = emptyList(),
    val allInstalledApps: List<AppItem> = emptyList(),
    val selectedAppForMenu: AppItem? = null,
    val editingApp: AppItem? = null,
    val isAddDialogOpen: Boolean = false,
    val isSystemLogOpen: Boolean = false,
    val isCheckingUpdates: Boolean = false,
    val updateInfo: UpdateInfo? = null,
    val updateCheckMessage: String? = null,
    val networkStatus: NetworkStatus = NetworkStatus(),
    val systemTelemetry: SystemTelemetry = SystemTelemetry(),
    val errorMessage: String? = null
) : UiState {
    // Compatibilidad para reordenamiento u otros consumidores
    val visibleApps: List<AppItem> get() = allApps
}
