package com.launcher.samiboxtv.presentation.home

import com.launcher.samiboxtv.core.base.UiState
import com.launcher.samiboxtv.domain.model.AppItem

/**
 * Estado inmutable de la pantalla principal del Launcher con categorías HUD
 * y catálogo completo de visibilidad de aplicaciones.
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
    val errorMessage: String? = null
) : UiState {
    // Compatibilidad para reordenamiento u otros consumidores
    val visibleApps: List<AppItem> get() = allApps
}
