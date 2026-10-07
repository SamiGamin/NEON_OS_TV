package com.launcher.samiboxtv.presentation.home

import com.launcher.samiboxtv.core.base.UiEvent
import com.launcher.samiboxtv.domain.model.AppItem

/**
 * Eventos e interacciones de usuario para la pantalla principal.
 */
sealed interface HomeUiEvent : UiEvent {
    data class LaunchApp(val app: AppItem) : HomeUiEvent
    data class OpenContextMenu(val app: AppItem) : HomeUiEvent
    object CloseContextMenu : HomeUiEvent
    data class StartMovingApp(val app: AppItem) : HomeUiEvent
    data class MoveApp(val app: AppItem, val direction: Int) : HomeUiEvent
    object FinishMovingApp : HomeUiEvent
    data class HideApp(val app: AppItem) : HomeUiEvent
    data class UnhideApp(val app: AppItem) : HomeUiEvent
    data class ToggleAppVisibility(val app: AppItem) : HomeUiEvent
    data class ToggleAppFavorite(val app: AppItem) : HomeUiEvent
    object ShowAllApps : HomeUiEvent
    object HideAllApps : HomeUiEvent
    object OpenAddDialog : HomeUiEvent
    object CloseAddDialog : HomeUiEvent
    object OpenSystemLog : HomeUiEvent
    object CloseSystemLog : HomeUiEvent
    object RefreshApps : HomeUiEvent
    object CheckUpdates : HomeUiEvent
    object DismissUpdateDialog : HomeUiEvent
}
