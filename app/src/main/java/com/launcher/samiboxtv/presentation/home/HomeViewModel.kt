package com.launcher.samiboxtv.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.domain.model.AppCardStyle
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.SystemMonitorTab
import com.launcher.samiboxtv.domain.usecase.CheckUpdateUseCase
import com.launcher.samiboxtv.domain.usecase.CleanMemoryUseCase
import com.launcher.samiboxtv.domain.usecase.ClearProcessCacheUseCase
import com.launcher.samiboxtv.domain.usecase.GetInstalledAppsUseCase
import com.launcher.samiboxtv.domain.usecase.GetLauncherSettingsUseCase
import com.launcher.samiboxtv.domain.usecase.GetRunningProcessesUseCase
import com.launcher.samiboxtv.domain.usecase.HideAppUseCase
import com.launcher.samiboxtv.domain.usecase.KillProcessUseCase
import com.launcher.samiboxtv.domain.usecase.LaunchAppUseCase
import com.launcher.samiboxtv.domain.usecase.ManageCategoriesUseCase
import com.launcher.samiboxtv.domain.usecase.MoveAppUseCase
import com.launcher.samiboxtv.domain.usecase.ObserveNetworkStatusUseCase
import com.launcher.samiboxtv.domain.usecase.ObserveSystemTelemetryUseCase
import com.launcher.samiboxtv.domain.usecase.SaveCardStyleUseCase
import com.launcher.samiboxtv.domain.usecase.SetHiddenPackagesUseCase
import com.launcher.samiboxtv.domain.usecase.ToggleAppVisibilityUseCase
import com.launcher.samiboxtv.domain.usecase.ToggleFavoriteAppUseCase
import com.launcher.samiboxtv.domain.usecase.UnhideAppUseCase
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
 * ViewModel principal siguiendo Clean Architecture y MVVM.
 * Administra el estado inmutable [HomeUiState] y procesa [HomeUiEvent].
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
    private val manageCategoriesUseCase: ManageCategoriesUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        loadSettings()
        loadApps()
        observeNetwork()
        checkForUpdates(isManual = false)
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
            // 1. Recolección de telemetría de hardware (RAM, CPU, Uptime) cada 3.0s
            launch {
                observeSystemTelemetryUseCase(intervalMillis = 3000)
                    .flowOn(dispatcherProvider.io)
                    .collect { telemetry ->
                        _uiState.update { it.copy(systemTelemetry = telemetry) }
                    }
            }

            // 2. Refresco periódico de procesos en RAM cada 3.5 segundos bajo Dispatchers.IO
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
            is HomeUiEvent.CreateCategory -> createCategory(event.name)
            is HomeUiEvent.RemoveCategory -> removeCategory(event.name)
            is HomeUiEvent.AssignCategory -> assignCategory(event.packageName, event.categoryName)
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
                    categories = settings.categories,
                    appCategoryMap = settings.appCategoryMap
                )
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
                val featured = group.visibleApps
                    .filter { it.isFavorite || it.bannerDrawable != null }
                    .ifEmpty { group.visibleApps.take(6) }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allApps = group.visibleApps,
                        featuredApps = featured,
                        hiddenApps = group.hiddenApps,
                        allInstalledApps = group.allInstalledApps,
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
        val result = launchAppUseCase(app.packageName)
        if (result.isFailure) {
            _uiState.update {
                it.copy(errorMessage = "No se pudo iniciar ${app.name}")
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
        val featured = updatedList
            .filter { it.isFavorite || it.bannerDrawable != null }
            .ifEmpty { updatedList.take(6) }

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
}
