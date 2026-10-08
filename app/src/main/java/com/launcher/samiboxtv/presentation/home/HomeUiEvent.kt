package com.launcher.samiboxtv.presentation.home

import com.launcher.samiboxtv.core.base.UiEvent
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.SystemMonitorTab

/**
 * Eventos e interacciones de usuario para la pantalla principal.
 */
sealed interface HomeUiEvent : UiEvent {
    data class LaunchApp(val app: AppItem) : HomeUiEvent
    data class OpenContextMenu(val app: AppItem) : HomeUiEvent
    object CloseContextMenu : HomeUiEvent
    data class StartReordering(val app: AppItem) : HomeUiEvent
    data class StartMovingApp(val app: AppItem) : HomeUiEvent
    data class MoveApp(val packageName: String, val direction: Int) : HomeUiEvent {
        constructor(app: AppItem, direction: Int) : this(app.packageName, direction)
    }
    data object ConfirmReorder : HomeUiEvent
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
    data class OpenSystemLogWithTab(val tab: SystemMonitorTab) : HomeUiEvent
    data class ChangeMonitorTab(val tab: SystemMonitorTab) : HomeUiEvent
    object CloseSystemLog : HomeUiEvent
    object LoadRunningProcesses : HomeUiEvent
    object CleanRam : HomeUiEvent
    data class KillProcess(val packageName: String) : HomeUiEvent
    object RefreshApps : HomeUiEvent
    object CheckUpdates : HomeUiEvent
    object DismissUpdateDialog : HomeUiEvent
    object OpenSettings : HomeUiEvent
    object CloseSettings : HomeUiEvent
    data class SelectSettingsSection(val section: com.launcher.samiboxtv.domain.model.SettingsSection) : HomeUiEvent
    data class ChangeCardStyle(val style: com.launcher.samiboxtv.domain.model.AppCardStyle) : HomeUiEvent
    data class CreateCategory(val name: String) : HomeUiEvent
    data class RemoveCategory(val name: String) : HomeUiEvent
    data class AssignCategory(val packageName: String, val categoryName: String) : HomeUiEvent
}
