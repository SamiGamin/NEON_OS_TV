package com.launcher.samiboxtv.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.SystemMonitorTab
import com.launcher.samiboxtv.domain.usecase.CheckUpdateUseCase
import com.launcher.samiboxtv.domain.usecase.CleanMemoryUseCase
import com.launcher.samiboxtv.domain.usecase.GetInstalledAppsUseCase
import com.launcher.samiboxtv.domain.usecase.GetRunningProcessesUseCase
import com.launcher.samiboxtv.domain.usecase.HideAppUseCase
import com.launcher.samiboxtv.domain.usecase.KillProcessUseCase
import com.launcher.samiboxtv.domain.usecase.LaunchAppUseCase
import com.launcher.samiboxtv.domain.usecase.MoveAppUseCase
import com.launcher.samiboxtv.domain.usecase.ObserveNetworkStatusUseCase
import com.launcher.samiboxtv.domain.usecase.ObserveSystemTelemetryUseCase
import com.launcher.samiboxtv.domain.usecase.SetHiddenPackagesUseCase
import com.launcher.samiboxtv.domain.usecase.ToggleAppVisibilityUseCase
import com.launcher.samiboxtv.domain.usecase.ToggleFavoriteAppUseCase
import com.launcher.samiboxtv.domain.usecase.UnhideAppUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
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
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadApps()
        observeNetwork()
        observeTelemetry()
        checkForUpdates()
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

    private fun observeTelemetry() {
        viewModelScope.launch(dispatcherProvider.main) {
            observeSystemTelemetryUseCase(intervalMillis = 3000)
                .flowOn(dispatcherProvider.io)
                .collect { telemetry ->
                    _uiState.update { it.copy(systemTelemetry = telemetry) }
                }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.LaunchApp -> launchApp(event.app)
            is HomeUiEvent.OpenContextMenu -> openContextMenu(event.app)
            HomeUiEvent.CloseContextMenu -> closeContextMenu()
            is HomeUiEvent.StartMovingApp -> startMovingApp(event.app)
            is HomeUiEvent.MoveApp -> moveApp(event.app, event.direction)
            HomeUiEvent.FinishMovingApp -> finishMovingApp()
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
                loadRunningProcesses()
            }
            is HomeUiEvent.OpenSystemLogWithTab -> {
                _uiState.update { it.copy(isSystemLogOpen = true, activeMonitorTab = event.tab) }
                if (event.tab == SystemMonitorTab.RAM_PROCESSES) {
                    loadRunningProcesses()
                }
            }
            is HomeUiEvent.ChangeMonitorTab -> {
                _uiState.update { it.copy(activeMonitorTab = event.tab) }
                if (event.tab == SystemMonitorTab.RAM_PROCESSES) {
                    loadRunningProcesses()
                }
            }
            HomeUiEvent.CloseSystemLog -> _uiState.update { it.copy(isSystemLogOpen = false, ramCleanMessage = null) }
            HomeUiEvent.LoadRunningProcesses -> loadRunningProcesses()
            HomeUiEvent.CleanRam -> cleanRam()
            is HomeUiEvent.KillProcess -> killProcess(event.packageName)
            HomeUiEvent.RefreshApps -> loadApps()
            HomeUiEvent.CheckUpdates -> checkForUpdates()
            HomeUiEvent.DismissUpdateDialog -> _uiState.update { it.copy(updateInfo = null) }
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

    private fun checkForUpdates() {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isCheckingUpdates = true, updateCheckMessage = "> VERIFICANDO GITHUB RELEASES...") }
            val result = checkUpdateUseCase()
            result.onSuccess { info ->
                _uiState.update {
                    it.copy(
                        isCheckingUpdates = false,
                        updateInfo = if (info.hasUpdate) info else null,
                        updateCheckMessage = if (!info.hasUpdate) "Launcher actualizado (v${info.currentVersion})" else "¡Nueva versión disponible v${info.latestVersion}!"
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

    private fun startMovingApp(app: AppItem) {
        _uiState.update {
            it.copy(
                selectedAppForMenu = null,
                editingApp = app
            )
        }
    }

    private fun finishMovingApp() {
        _uiState.update { it.copy(editingApp = null) }
    }

    private fun moveApp(app: AppItem, direction: Int) {
        viewModelScope.launch(dispatcherProvider.main) {
            val currentList = _uiState.value.allApps
            val updatedList = withContext(dispatcherProvider.io) {
                moveAppUseCase(currentList, app, direction)
            }
            val featured = updatedList
                .filter { it.isFavorite || it.bannerDrawable != null }
                .ifEmpty { updatedList.take(6) }

            _uiState.update {
                it.copy(
                    allApps = updatedList,
                    featuredApps = featured
                )
            }
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
