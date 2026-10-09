package com.launcher.samiboxtv.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.data.repository.IptvRepository
import com.launcher.samiboxtv.domain.model.AppCardStyle
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.IptvChannel
import com.launcher.samiboxtv.domain.model.MediaType
import com.launcher.samiboxtv.domain.model.SettingsSection
import com.launcher.samiboxtv.domain.model.StorageDrive
import com.launcher.samiboxtv.domain.model.SystemMonitorTab
import com.launcher.samiboxtv.domain.model.VirtualApps
import com.launcher.samiboxtv.domain.repository.PreferencesRepository
import com.launcher.samiboxtv.domain.usecase.CheckUpdateUseCase
import com.launcher.samiboxtv.domain.usecase.CleanMemoryUseCase
import com.launcher.samiboxtv.domain.usecase.ClearProcessCacheUseCase
import com.launcher.samiboxtv.domain.usecase.GetInstalledAppsUseCase
import com.launcher.samiboxtv.domain.usecase.GetLauncherSettingsUseCase
import com.launcher.samiboxtv.domain.usecase.GetMediaFilesUseCase
import com.launcher.samiboxtv.domain.usecase.GetRunningProcessesUseCase
import com.launcher.samiboxtv.domain.usecase.GetStorageDrivesUseCase
import com.launcher.samiboxtv.domain.usecase.HideAppUseCase
import com.launcher.samiboxtv.domain.usecase.KillProcessUseCase
import com.launcher.samiboxtv.domain.usecase.LaunchAppUseCase
import com.launcher.samiboxtv.domain.usecase.ManageCategoriesUseCase
import com.launcher.samiboxtv.domain.usecase.MoveAppUseCase
import com.launcher.samiboxtv.domain.usecase.ObserveNetworkStatusUseCase
import com.launcher.samiboxtv.domain.usecase.ObserveSystemTelemetryUseCase
import com.launcher.samiboxtv.domain.usecase.SaveAppLayoutModeUseCase
import com.launcher.samiboxtv.domain.usecase.SaveCardStyleUseCase
import com.launcher.samiboxtv.domain.usecase.SaveShowAppNamesUseCase
import com.launcher.samiboxtv.domain.usecase.SetHiddenPackagesUseCase
import com.launcher.samiboxtv.domain.usecase.ToggleAppVisibilityUseCase
import com.launcher.samiboxtv.domain.usecase.ToggleFavoriteAppUseCase
import com.launcher.samiboxtv.domain.usecase.UnhideAppUseCase
import com.launcher.samiboxtv.presentation.theme.AppLayoutMode
import com.launcher.samiboxtv.util.DevLogManager
import com.launcher.samiboxtv.util.iptv.IptvPlaylistManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel principal siguiendo Clean Architecture y MVVM[cite: 19].
 * Administra el estado inmutable [HomeUiState] y procesa [HomeUiEvent][cite: 19].
 */
class HomeViewModel(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val hideAppUseCase: HideAppUseCase,
    private val unhideAppUseCase: UnhideAppUseCase,
    private val moveAppUseCase: MoveAppUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    private val toggleAppVisibilityUseCase: ToggleAppVisibilityUseCase,
    private val toggleFavoriteAppUseCase: ToggleFavoriteAppUseCase,
    private val setHiddenPackagesUseCase: SetHiddenPackagesUseCase,
    private val observeNetworkStatusUseCase: ObserveNetworkStatusUseCase,
    private val observeSystemTelemetryUseCase: ObserveSystemTelemetryUseCase,
    private val checkUpdateUseCase: CheckUpdateUseCase,
    private val getRunningProcessesUseCase: GetRunningProcessesUseCase,
    private val cleanMemoryUseCase: CleanMemoryUseCase,
    private val killProcessUseCase: KillProcessUseCase,
    private val clearProcessCacheUseCase: ClearProcessCacheUseCase,
    private val getLauncherSettingsUseCase: GetLauncherSettingsUseCase,
    private val saveCardStyleUseCase: SaveCardStyleUseCase,
    private val saveAppLayoutModeUseCase: SaveAppLayoutModeUseCase,
    private val manageCategoriesUseCase: ManageCategoriesUseCase,
    private val saveShowAppNamesUseCase: SaveShowAppNamesUseCase,
    private val getStorageDrivesUseCase: GetStorageDrivesUseCase,
    private val getMediaFilesUseCase: GetMediaFilesUseCase,
    private val dispatcherProvider: DispatcherProvider,
    private val iptvRepository: IptvRepository = IptvRepository(),
    private val preferencesRepository: PreferencesRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        loadSettings()
        loadApps()
        loadFavoriteIptvChannels()
        observeNetwork()
        checkForUpdates(isManual = false)
        loadIptvFromUrl(VirtualApps.DEFAULT_IPTV_URL)

        val initialUrl = IptvPlaylistManager.getActivePlaylistUrl()
        loadIptvFromUrl(initialUrl)

        DevLogManager.onM3uUrlReceived = { url ->
            val added = IptvPlaylistManager.addPlaylist("Lista QR", url)
            loadIptvFromUrl(added.url, added.name)
        }
    }

    private fun getVirtualApps(): List<AppItem> {
        return listOf(
            AppItem(
                packageName = VirtualApps.PKG_IPTV,
                name = "CYBER IPTV",
                category = "STREAMING",
                isFavorite = false,
                isSystemApp = true
            ),
            AppItem(
                packageName = VirtualApps.PKG_MEDIA_HUB,
                name = "CYBER MEDIA HUB",
                category = "APPS",
                isFavorite = false,
                isSystemApp = true
            )
        )
    }

    private fun observeNetwork() {
        viewModelScope.launch(dispatcherProvider.main) {
            observeNetworkStatusUseCase()
                .flowOn(dispatcherProvider.io)
                .collect { networkStatus ->
                    _uiState.update { it.copy(networkStatus = networkStatus) }
                }
        }
    }

    private fun startTelemetrySession() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch(dispatcherProvider.main) {
            launch {
                observeSystemTelemetryUseCase(intervalMillis = 3000)
                    .flowOn(dispatcherProvider.io)
                    .collect { telemetry ->
                        _uiState.update { it.copy(systemTelemetry = telemetry) }
                    }
            }

            launch {
                while (isActive) {
                    val processes = withContext(dispatcherProvider.io) {
                        getRunningProcessesUseCase()
                    }
                    _uiState.update { it.copy(runningProcesses = processes) }
                    delay(3500)
                }
            }
        }
    }

    private fun stopTelemetrySession() {
        telemetryJob?.cancel()
        telemetryJob = null
        clearProcessCacheUseCase()
    }

    override fun onCleared() {
        super.onCleared()
        stopTelemetrySession()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.LaunchApp -> launchApp(event.app)
            is HomeUiEvent.OpenContextMenu -> openContextMenu(event.app)
            HomeUiEvent.CloseContextMenu -> closeContextMenu()
            is HomeUiEvent.StartReordering -> startReordering(event.app)
            is HomeUiEvent.StartMovingApp -> startReordering(event.app)
            is HomeUiEvent.MoveApp -> moveApp(event.packageName, event.direction)
            HomeUiEvent.ConfirmReorder, HomeUiEvent.FinishMovingApp -> confirmReorder()
            is HomeUiEvent.HideApp -> hideApp(event.app)
            is HomeUiEvent.UnhideApp -> unhideApp(event.app)
            is HomeUiEvent.ToggleAppVisibility -> toggleVisibility(event.app)
            is HomeUiEvent.ToggleAppFavorite -> toggleFavorite(event.app)
            HomeUiEvent.ShowAllApps -> showAllApps()
            HomeUiEvent.HideAllApps -> hideAllApps()
            HomeUiEvent.OpenAddDialog -> _uiState.update { it.copy(isAddDialogOpen = true) }
            HomeUiEvent.CloseAddDialog -> _uiState.update { it.copy(isAddDialogOpen = false) }
            HomeUiEvent.OpenSystemLog -> {
                _uiState.update { it.copy(isSystemLogOpen = true, activeMonitorTab = SystemMonitorTab.RAM_PROCESSES) }
                startTelemetrySession()
            }
            is HomeUiEvent.OpenSystemLogWithTab -> {
                _uiState.update { it.copy(isSystemLogOpen = true, activeMonitorTab = event.tab) }
                startTelemetrySession()
            }
            is HomeUiEvent.ChangeMonitorTab -> {
                _uiState.update { it.copy(activeMonitorTab = event.tab) }
                if (event.tab == SystemMonitorTab.RAM_PROCESSES) {
                    loadRunningProcesses()
                }
            }
            HomeUiEvent.CloseSystemLog -> {
                stopTelemetrySession()
                _uiState.update {
                    it.copy(
                        isSystemLogOpen = false,
                        ramCleanMessage = null,
                        runningProcesses = emptyList()
                    )
                }
            }
            HomeUiEvent.LoadRunningProcesses -> loadRunningProcesses()
            HomeUiEvent.CleanRam -> cleanRam()
            is HomeUiEvent.KillProcess -> killProcess(event.packageName)
            HomeUiEvent.RefreshApps -> loadApps()
            HomeUiEvent.CheckUpdates -> checkForUpdates(isManual = true)
            HomeUiEvent.DismissUpdateDialog -> _uiState.update { it.copy(updateInfo = null) }
            HomeUiEvent.OpenSettings -> _uiState.update { it.copy(isSettingsOpen = true) }
            HomeUiEvent.CloseSettings -> _uiState.update { it.copy(isSettingsOpen = false) }
            is HomeUiEvent.SelectSettingsSection -> _uiState.update { it.copy(activeSettingsSection = event.section) }
            is HomeUiEvent.ChangeCardStyle -> changeCardStyle(event.style)
            is HomeUiEvent.SetAppLayoutMode -> changeAppLayoutMode(event.mode)
            is HomeUiEvent.CreateCategory -> createCategory(event.name)
            is HomeUiEvent.RemoveCategory -> removeCategory(event.name)
            is HomeUiEvent.AssignCategory -> assignCategory(event.packageName, event.categoryName)
            HomeUiEvent.ToggleShowAppNames -> toggleShowAppNames()
            HomeUiEvent.ToggleHudOverlay -> _uiState.update { it.copy(isHudOverlayVisible = !it.isHudOverlayVisible) }
            HomeUiEvent.OpenMediaHub -> openMediaHub()
            HomeUiEvent.CloseMediaHub -> closeMediaHub()
            is HomeUiEvent.SelectStorageDrive -> selectDrive(event.drive)
            is HomeUiEvent.FilterMediaType -> filterMediaType(event.type)
            HomeUiEvent.RefreshMedia -> refreshMedia()
            is HomeUiEvent.PlayMedia -> _uiState.update { it.copy(currentPlayingMedia = event.file) }
            HomeUiEvent.CloseMediaPlayer -> _uiState.update { it.copy(currentPlayingMedia = null) }
            HomeUiEvent.PlayNextMedia -> playNextMedia()
            HomeUiEvent.PlayPreviousMedia -> playPreviousMedia()

            is HomeUiEvent.ToggleDevMode -> {
                val newDevState = !_uiState.value.isDevModeActive
                _uiState.update { it.copy(isDevModeActive = newDevState) }
                DevLogManager.log("DEV", "Modo desarrollador: ${if (newDevState) "ACTIVADO" else "DESACTIVADO"}")
            }

            is HomeUiEvent.ToggleLogServer -> {
                val currentlyRunning = _uiState.value.isLogServerRunning
                if (currentlyRunning) {
                    DevLogManager.stopServer()
                    _uiState.update { it.copy(isLogServerRunning = false, logServerUrl = "") }
                } else {
                    DevLogManager.startServer(viewModelScope)
                    val ip = DevLogManager.getLocalIpAddress()
                    val url = "http://$ip:${DevLogManager.SERVER_PORT}"
                    _uiState.update { it.copy(isLogServerRunning = true, logServerUrl = url) }
                }
            }

            is HomeUiEvent.ToggleKeyDebugToast -> {
                _uiState.update { it.copy(showKeyDebugToast = !it.showKeyDebugToast) }
            }

            is HomeUiEvent.ClearLogs -> {
                DevLogManager.log("SYSTEM", "Historial de logs reiniciado manualmente")
            }
            is HomeUiEvent.AddIptvPlaylist -> {
                val newPl = IptvPlaylistManager.addPlaylist(event.name, event.url)
                loadIptvFromUrl(newPl.url, newPl.name)
            }

            is HomeUiEvent.SelectIptvPlaylist -> {
                IptvPlaylistManager.setActivePlaylistUrl(event.playlist.url)
                loadIptvFromUrl(event.playlist.url, event.playlist.name)
            }

            is HomeUiEvent.DeleteIptvPlaylist -> {
                IptvPlaylistManager.deletePlaylist(event.playlistId)
                val activeUrl = IptvPlaylistManager.getActivePlaylistUrl()
                if (activeUrl != _uiState.value.currentIptvUrl) {
                    loadIptvFromUrl(activeUrl)
                }
            }

            is HomeUiEvent.LoadIptvFromUrl -> loadIptvFromUrl(event.url)

            is HomeUiEvent.LoadIptvFromFile -> {
                viewModelScope.launch(Dispatchers.IO) {
                    _uiState.update {
                        it.copy(
                            isIptvLoading = true,
                            iptvStatusMessage = "Procesando archivo local..."
                        )
                    }
                    try {
                        val channels = iptvRepository.loadFromFile(event.file)
                        _uiState.update {
                            it.copy(
                                isIptvLoading = false,
                                iptvChannels = channels,
                                currentIptvIndex = 0,
                                currentIptvChannel = channels.firstOrNull(),
                                activeIptvSource = "Archivo: ${event.file.name}",
                                iptvStatusMessage = "${channels.size} canales cargados desde archivo"
                            )
                        }
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                isIptvLoading = false,
                                iptvStatusMessage = "Error al leer archivo: ${e.message}"
                            )
                        }
                    }
                }
            }

            is HomeUiEvent.ClearIptvList -> {
                _uiState.update {
                    it.copy(
                        iptvChannels = emptyList(),
                        currentIptvIndex = 0,
                        currentIptvChannel = null,
                        activeIptvSource = null,
                        iptvStatusMessage = "Lista eliminada"
                    )
                }
            }

            HomeUiEvent.OpenLiveTv -> {
                val channels = _uiState.value.iptvChannels
                if (channels.isNotEmpty()) {
                    val safeIndex = _uiState.value.currentIptvIndex.coerceIn(0, channels.size - 1)
                    val activeChannel = channels[safeIndex]
                    _uiState.update {
                        it.copy(
                            isIptvPlayerOpen = true,
                            currentIptvIndex = safeIndex,
                            currentIptvChannel = activeChannel,
                            isIptvChannelListOpen = false
                        )
                    }
                }
            }

            HomeUiEvent.CloseLiveTv -> {
                _uiState.update {
                    it.copy(
                        isIptvPlayerOpen = false,
                        isIptvChannelListOpen = false
                    )
                }
            }

            HomeUiEvent.NextChannel -> {
                val channels = _uiState.value.iptvChannels
                if (channels.isNotEmpty()) {
                    val nextIndex = (_uiState.value.currentIptvIndex + 1) % channels.size
                    _uiState.update {
                        it.copy(
                            currentIptvIndex = nextIndex,
                            currentIptvChannel = channels[nextIndex],
                            isIptvOsdVisible = true
                        )
                    }
                }
            }

            HomeUiEvent.PreviousChannel -> {
                val channels = _uiState.value.iptvChannels
                if (channels.isNotEmpty()) {
                    val prevIndex = if (_uiState.value.currentIptvIndex - 1 < 0) channels.size - 1 else _uiState.value.currentIptvIndex - 1
                    _uiState.update {
                        it.copy(
                            currentIptvIndex = prevIndex,
                            currentIptvChannel = channels[prevIndex],
                            isIptvOsdVisible = true
                        )
                    }
                }
            }

            is HomeUiEvent.SelectChannel -> {
                val channels = _uiState.value.iptvChannels
                if (event.index in channels.indices) {
                    _uiState.update {
                        it.copy(
                            currentIptvIndex = event.index,
                            currentIptvChannel = channels[event.index],
                            isIptvChannelListOpen = false,
                            isIptvOsdVisible = true
                        )
                    }
                }
            }

            HomeUiEvent.ToggleChannelList -> {
                _uiState.update {
                    it.copy(isIptvChannelListOpen = !it.isIptvChannelListOpen)
                }
            }

            is HomeUiEvent.FilterIptvCategory -> {
                _uiState.update {
                    it.copy(selectedIptvCategory = event.category)
                }
            }

            is HomeUiEvent.ToggleIptvFavorite -> toggleIptvFavorite(event.channel)

            HomeUiEvent.ToggleCurrentIptvFavorite -> {
                val current = _uiState.value.currentIptvChannel
                    ?: _uiState.value.iptvChannels.getOrNull(_uiState.value.currentIptvIndex)
                current?.let {
                    toggleIptvFavorite(it)
                    _uiState.update { s -> s.copy(isIptvOsdVisible = true) }
                }
            }

            HomeUiEvent.ToggleIptvOsd -> {
                _uiState.update {
                    it.copy(isIptvOsdVisible = !it.isIptvOsdVisible)
                }
            }
        }
    }

    private fun loadIptvFromUrl(url: String, playlistName: String? = null) {
        IptvPlaylistManager.setActivePlaylistUrl(url)
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update {
                it.copy(
                    isIptvLoading = true,
                    currentIptvUrl = url,
                    iptvStatusMessage = "Descargando lista M3U..."
                )
            }
            try {
                val channels = withContext(dispatcherProvider.io) {
                    iptvRepository.loadFromUrl(url)
                }
                val isDefault = (url == VirtualApps.DEFAULT_IPTV_URL)
                val label = if (isDefault) "LISTA DE EJEMPLO" else "URL: ${url.take(35)}..."
                _uiState.update {
                    it.copy(
                        isIptvLoading = false,
                        iptvChannels = channels,
                        currentIptvIndex = 0,
                        currentIptvChannel = channels.firstOrNull(),
                        currentIptvUrl = url,
                        activeIptvSource = label,
                        iptvStatusMessage = "${channels.size} canales cargados correctamente"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isIptvLoading = false,
                        currentIptvUrl = url,
                        iptvStatusMessage = "Error al descargar: ${e.message ?: "Sin conexión"}"
                    )
                }
            }
        }
    }

    private fun loadFavoriteIptvChannels() {
        viewModelScope.launch(dispatcherProvider.main) {
            val favs = withContext(dispatcherProvider.io) {
                preferencesRepository?.getFavoriteIptvChannels() ?: emptySet()
            }
            _uiState.update { it.copy(favoriteIptvChannelUrls = favs) }
        }
    }

    private fun toggleIptvFavorite(channel: IptvChannel) {
        viewModelScope.launch(dispatcherProvider.main) {
            val currentFavs = _uiState.value.favoriteIptvChannelUrls.toMutableSet()
            if (currentFavs.contains(channel.streamUrl)) {
                currentFavs.remove(channel.streamUrl)
            } else {
                currentFavs.add(channel.streamUrl)
            }
            val newSet = currentFavs.toSet()
            _uiState.update { it.copy(favoriteIptvChannelUrls = newSet) }
            withContext(dispatcherProvider.io) {
                preferencesRepository?.saveFavoriteIptvChannels(newSet)
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch(dispatcherProvider.main) {
            val settings = withContext(dispatcherProvider.io) {
                getLauncherSettingsUseCase()
            }
            _uiState.update {
                it.copy(
                    cardStyle = settings.cardStyle,
                    appLayoutMode = settings.appLayoutMode,
                    categories = settings.categories,
                    appCategoryMap = settings.appCategoryMap,
                    showAppNames = settings.showAppNames
                )
            }
        }
    }

    private fun changeAppLayoutMode(mode: AppLayoutMode) {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(appLayoutMode = mode) }
            withContext(dispatcherProvider.io) {
                saveAppLayoutModeUseCase(mode)
            }
        }
    }

    private fun toggleShowAppNames() {
        val newValue = !_uiState.value.showAppNames
        _uiState.update { it.copy(showAppNames = newValue) }
        viewModelScope.launch(dispatcherProvider.main) {
            withContext(dispatcherProvider.io) {
                saveShowAppNamesUseCase(newValue)
            }
        }
    }

    private fun changeCardStyle(style: AppCardStyle) {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(cardStyle = style) }
            withContext(dispatcherProvider.io) {
                saveCardStyleUseCase(style)
            }
        }
    }

    private fun createCategory(name: String) {
        viewModelScope.launch(dispatcherProvider.main) {
            val formatted = name.trim().uppercase()
            if (formatted.isNotBlank()) {
                val success = withContext(dispatcherProvider.io) {
                    manageCategoriesUseCase.addCategory(formatted)
                }
                if (success) {
                    loadSettings()
                }
            }
        }
    }

    private fun removeCategory(name: String) {
        viewModelScope.launch(dispatcherProvider.main) {
            val success = withContext(dispatcherProvider.io) {
                manageCategoriesUseCase.removeCategory(name)
            }
            if (success) {
                loadSettings()
                loadApps()
            }
        }
    }

    private fun assignCategory(packageName: String, categoryName: String) {
        viewModelScope.launch(dispatcherProvider.main) {
            withContext(dispatcherProvider.io) {
                manageCategoriesUseCase.assignAppToCategory(packageName, categoryName)
            }
            loadSettings()
            loadApps()
        }
    }

    fun loadRunningProcesses() {
        viewModelScope.launch(dispatcherProvider.main) {
            val processes = withContext(dispatcherProvider.io) {
                getRunningProcessesUseCase()
            }
            _uiState.update { it.copy(runningProcesses = processes) }
        }
    }

    fun cleanRam() {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isCleaningRam = true, ramCleanMessage = "LIBERANDO MEMORIA RAM...") }
            val result = withContext(dispatcherProvider.io) {
                cleanMemoryUseCase()
            }
            val msg = if (result.freedMemoryMb > 0) {
                "¡RAM OPTIMIZADA! +${result.freedMemoryMb} MB LIBERADOS (${result.killedProcessesCount} PROCESOS FINALIZADOS)"
            } else {
                "¡RAM OPTIMIZADA! ${result.finalAvailableMb} MB LIBRES (${result.killedProcessesCount} PROCESOS DETENIDOS)"
            }
            val updatedProcesses = withContext(dispatcherProvider.io) {
                getRunningProcessesUseCase()
            }
            _uiState.update {
                it.copy(
                    isCleaningRam = false,
                    ramCleanMessage = msg,
                    runningProcesses = updatedProcesses
                )
            }
        }
    }

    fun killProcess(packageName: String) {
        viewModelScope.launch(dispatcherProvider.main) {
            withContext(dispatcherProvider.io) {
                killProcessUseCase(packageName)
            }
            val updatedProcesses = withContext(dispatcherProvider.io) {
                getRunningProcessesUseCase()
            }
            _uiState.update {
                it.copy(
                    ramCleanMessage = "PROCESO $packageName FINALIZADO",
                    runningProcesses = updatedProcesses
                )
            }
        }
    }

    private fun checkForUpdates(isManual: Boolean = false) {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isCheckingUpdates = true, updateCheckMessage = "> VERIFICANDO GITHUB RELEASES...") }
            val result = checkUpdateUseCase()
            result.onSuccess { info ->
                _uiState.update {
                    it.copy(
                        isCheckingUpdates = false,
                        isSettingsOpen = if (isManual) false else it.isSettingsOpen,
                        updateInfo = if (info.hasUpdate || isManual) info else null,
                        updateCheckMessage = if (!info.hasUpdate) "Launcher al día (v${info.currentVersion})" else "¡Nueva versión disponible v${info.latestVersion}!"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isCheckingUpdates = false,
                        updateCheckMessage = "Error al verificar: ${error.localizedMessage ?: "Fallo de conexión"}"
                    )
                }
            }
        }
    }

    private fun loadApps() {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val group = withContext(dispatcherProvider.io) {
                    getInstalledAppsUseCase()
                }

                val virtualApps = getVirtualApps()
                val combinedVisible = group.visibleApps + virtualApps
                val combinedAllInstalled = group.allInstalledApps + virtualApps
                val featured = combinedVisible.filter { it.isFavorite }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allApps = combinedVisible,
                        featuredApps = featured,
                        hiddenApps = group.hiddenApps,
                        allInstalledApps = combinedAllInstalled,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al cargar aplicaciones"
                    )
                }
            }
        }
    }

    private fun launchApp(app: AppItem) {
        when (app.packageName) {
            VirtualApps.PKG_MEDIA_HUB -> {
                openMediaHub()
            }
            VirtualApps.PKG_IPTV -> {
                if (_uiState.value.iptvChannels.isNotEmpty()) {
                    onEvent(HomeUiEvent.OpenLiveTv)
                } else {
                    _uiState.update {
                        it.copy(
                            isSettingsOpen = true,
                            activeSettingsSection = SettingsSection.IPTV
                        )
                    }
                }
            }
            else -> {
                val result = launchAppUseCase(app.packageName)
                if (result.isFailure) {
                    _uiState.update {
                        it.copy(errorMessage = "No se pudo iniciar ${app.name}")
                    }
                }
            }
        }
    }

    private fun openContextMenu(app: AppItem) {
        if (_uiState.value.editingApp == null) {
            _uiState.update { it.copy(selectedAppForMenu = app) }
        }
    }

    private fun closeContextMenu() {
        _uiState.update { it.copy(selectedAppForMenu = null) }
    }

    private fun startReordering(app: AppItem) {
        _uiState.update {
            it.copy(
                selectedAppForMenu = null,
                movingAppPackageName = app.packageName,
                editingApp = app
            )
        }
    }

    private fun confirmReorder() {
        val reorderedList = _uiState.value.allApps
        _uiState.update {
            it.copy(
                movingAppPackageName = null,
                editingApp = null
            )
        }
        viewModelScope.launch(dispatcherProvider.io) {
            moveAppUseCase.saveOrder(reorderedList.map { it.packageName })
        }
    }

    private fun moveApp(packageName: String, direction: Int) {
        val state = _uiState.value
        val currentList = state.allApps
        val targetApp = currentList.find { it.packageName == packageName } ?: return
        val catMap = state.appCategoryMap

        val sectionApps = if (targetApp.isFavorite) {
            currentList.filter { it.isFavorite }
        } else {
            val appCat = catMap[targetApp.packageName] ?: targetApp.category.ifBlank { "APPS" }
            currentList.filter { !it.isFavorite && (catMap[it.packageName] ?: it.category.ifBlank { "APPS" }).equals(appCat, ignoreCase = true) }
        }

        val localIndex = sectionApps.indexOfFirst { it.packageName == targetApp.packageName }
        if (localIndex == -1) return

        val targetLocalIndex = localIndex + direction
        if (targetLocalIndex !in sectionApps.indices) return

        val neighborApp = sectionApps[targetLocalIndex]

        val mutable = currentList.toMutableList()
        val currentIndex = mutable.indexOfFirst { it.packageName == targetApp.packageName }
        if (currentIndex == -1) return

        mutable.removeAt(currentIndex)
        val neighborIndex = mutable.indexOfFirst { it.packageName == neighborApp.packageName }
        if (neighborIndex == -1) return

        if (direction > 0) {
            mutable.add(neighborIndex + 1, targetApp)
        } else {
            mutable.add(neighborIndex, targetApp)
        }

        val updatedList = mutable.mapIndexed { idx, item -> item.copy(orderIndex = idx) }
        val featured = updatedList.filter { it.isFavorite }

        _uiState.update {
            it.copy(
                allApps = updatedList,
                featuredApps = featured,
                movingAppPackageName = packageName,
                editingApp = targetApp
            )
        }
    }

    private fun hideApp(app: AppItem) {
        viewModelScope.launch(dispatcherProvider.main) {
            withContext(dispatcherProvider.io) {
                hideAppUseCase(app.packageName)
            }
            closeContextMenu()
            loadApps()
        }
    }

    private fun unhideApp(app: AppItem) {
        viewModelScope.launch(dispatcherProvider.main) {
            withContext(dispatcherProvider.io) {
                unhideAppUseCase(app.packageName)
            }
            loadApps()
        }
    }

    private fun toggleVisibility(app: AppItem) {
        viewModelScope.launch(dispatcherProvider.main) {
            withContext(dispatcherProvider.io) {
                toggleAppVisibilityUseCase(app.packageName)
            }
            loadApps()
        }
    }

    private fun toggleFavorite(app: AppItem) {
        viewModelScope.launch(dispatcherProvider.main) {
            withContext(dispatcherProvider.io) {
                toggleFavoriteAppUseCase(app.packageName)
            }
            closeContextMenu()
            loadApps()
        }
    }

    private fun showAllApps() {
        viewModelScope.launch(dispatcherProvider.main) {
            withContext(dispatcherProvider.io) {
                setHiddenPackagesUseCase(emptySet())
            }
            loadApps()
        }
    }

    private fun hideAllApps() {
        viewModelScope.launch(dispatcherProvider.main) {
            val allPackages = _uiState.value.allInstalledApps.map { it.packageName }.toSet()
            withContext(dispatcherProvider.io) {
                setHiddenPackagesUseCase(allPackages)
            }
            loadApps()
        }
    }

    private fun openMediaHub() {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isMediaHubOpen = true, isLoadingMedia = true) }
            val drives = withContext(dispatcherProvider.io) {
                getStorageDrivesUseCase()
            }
            val initialDrive = drives.firstOrNull()
            _uiState.update {
                it.copy(
                    storageDrives = drives,
                    selectedDrive = initialDrive,
                    isLoadingMedia = initialDrive != null
                )
            }
            if (initialDrive != null) {
                loadMediaFiles(initialDrive, _uiState.value.mediaFilter)
            } else {
                _uiState.update { it.copy(isLoadingMedia = false, mediaFilesList = emptyList()) }
            }
        }
    }

    private fun closeMediaHub() {
        _uiState.update {
            it.copy(
                isMediaHubOpen = false,
                currentPlayingMedia = null,
                mediaFilesList = emptyList(),
                isLoadingMedia = false
            )
        }
    }

    private fun selectDrive(drive: StorageDrive) {
        _uiState.update { it.copy(selectedDrive = drive) }
        loadMediaFiles(drive, _uiState.value.mediaFilter)
    }

    private fun filterMediaType(type: MediaType?) {
        _uiState.update { it.copy(mediaFilter = type) }
        _uiState.value.selectedDrive?.let { drive ->
            loadMediaFiles(drive, type)
        }
    }

    private fun refreshMedia() {
        _uiState.value.selectedDrive?.let { drive ->
            loadMediaFiles(drive, _uiState.value.mediaFilter)
        }
    }

    private fun loadMediaFiles(drive: StorageDrive, filter: MediaType?) {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isLoadingMedia = true) }
            val files = withContext(dispatcherProvider.io) {
                getMediaFilesUseCase(drive, filter)
            }
            _uiState.update { it.copy(isLoadingMedia = false, mediaFilesList = files) }
        }
    }

    private fun playNextMedia() {
        val current = _uiState.value.currentPlayingMedia ?: return
        val list = _uiState.value.mediaFilesList
        val index = list.indexOfFirst { it.path == current.path }
        if (index != -1 && index < list.lastIndex) {
            _uiState.update { it.copy(currentPlayingMedia = list[index + 1]) }
        }
    }

    private fun playPreviousMedia() {
        val current = _uiState.value.currentPlayingMedia ?: return
        val list = _uiState.value.mediaFilesList
        val index = list.indexOfFirst { it.path == current.path }
        if (index > 0) {
            _uiState.update { it.copy(currentPlayingMedia = list[index - 1]) }
        }
    }
}