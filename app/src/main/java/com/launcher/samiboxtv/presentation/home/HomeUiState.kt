package com.launcher.samiboxtv.presentation.home

import androidx.compose.runtime.Immutable
import com.launcher.samiboxtv.core.base.UiState
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.IptvChannel
import com.launcher.samiboxtv.domain.model.MediaFile
import com.launcher.samiboxtv.domain.model.MediaType
import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.domain.model.ProcessInfo
import com.launcher.samiboxtv.domain.model.StorageDrive
import com.launcher.samiboxtv.domain.model.SystemMonitorTab
import com.launcher.samiboxtv.domain.model.SystemTelemetry
import com.launcher.samiboxtv.domain.model.UpdateInfo
import com.launcher.samiboxtv.domain.model.VirtualApps

/**
 * Estado inmutable de la pantalla principal del Launcher con categorías HUD,
 * catálogo de aplicaciones, telemetría de red, hardware y actualizaciones.
 */
@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredApps: List<AppItem> = emptyList(),
    val allApps: List<AppItem> = emptyList(),
    val hiddenApps: List<AppItem> = emptyList(),
    val allInstalledApps: List<AppItem> = emptyList(),
    val selectedAppForMenu: AppItem? = null,
    val movingAppPackageName: String? = null,
    val editingApp: AppItem? = null,
    val isAddDialogOpen: Boolean = false,
    val isSystemLogOpen: Boolean = false,
    val activeMonitorTab: SystemMonitorTab = SystemMonitorTab.RAM_PROCESSES,
    val runningProcesses: List<ProcessInfo> = emptyList(),
    val isCleaningRam: Boolean = false,
    val ramCleanMessage: String? = null,
    val isCheckingUpdates: Boolean = false,
    val updateInfo: UpdateInfo? = null,
    val updateCheckMessage: String? = null,
    val networkStatus: NetworkStatus = NetworkStatus(),
    val systemTelemetry: SystemTelemetry = SystemTelemetry(),
    val isSettingsOpen: Boolean = false,
    val activeSettingsSection: com.launcher.samiboxtv.domain.model.SettingsSection = com.launcher.samiboxtv.domain.model.SettingsSection.FAVORITES,
    val cardStyle: com.launcher.samiboxtv.domain.model.AppCardStyle = com.launcher.samiboxtv.domain.model.AppCardStyle.BANNER_16_9,
    val categories: List<String> = listOf("STREAMING", "GAMING", "APPS"),
    val appCategoryMap: Map<String, String> = emptyMap(),
    val showAppNames: Boolean = true,
    val isHudOverlayVisible: Boolean = false,
    val isMediaHubOpen: Boolean = false,
    val storageDrives: List<StorageDrive> = emptyList(),
    val selectedDrive: StorageDrive? = null,
    val mediaFilter: MediaType? = null,
    val mediaFilesList: List<MediaFile> = emptyList(),
    val isLoadingMedia: Boolean = false,
    val currentPlayingMedia: MediaFile? = null,
    val errorMessage: String? = null,

    val isDevModeActive: Boolean = false,
    val isLogServerRunning: Boolean = false,
    val logServerUrl: String = "",
    val showKeyDebugToast: Boolean = false,
    // IPTV
    val iptvChannels: List<IptvChannel> = emptyList(),
    val isIptvLoading: Boolean = false,
    val iptvStatusMessage: String? = null,
    val activeIptvSource: String? = null,
    val currentIptvUrl: String = VirtualApps.DEFAULT_IPTV_URL,
    val isIptvPlayerOpen: Boolean = false,
    val currentIptvIndex: Int = 0,
    val currentIptvChannel: IptvChannel? = null,
    val isIptvChannelListOpen: Boolean = false,
    val selectedIptvCategory: String = "TODOS",
    val favoriteIptvChannelUrls: Set<String> = emptySet()
) : UiState {
    // Compatibilidad para reordenamiento u otros consumidores
    val visibleApps: List<AppItem> get() = allApps
}
