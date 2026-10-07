package com.launcher.samiboxtv.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.usecase.GetInstalledAppsUseCase
import com.launcher.samiboxtv.domain.usecase.HideAppUseCase
import com.launcher.samiboxtv.domain.usecase.LaunchAppUseCase
import com.launcher.samiboxtv.domain.usecase.MoveAppUseCase
import com.launcher.samiboxtv.domain.usecase.UnhideAppUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadApps()
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
            HomeUiEvent.OpenAddDialog -> _uiState.update { it.copy(isAddDialogOpen = true) }
            HomeUiEvent.CloseAddDialog -> _uiState.update { it.copy(isAddDialogOpen = false) }
            HomeUiEvent.OpenSystemLog -> _uiState.update { it.copy(isSystemLogOpen = true) }
            HomeUiEvent.CloseSystemLog -> _uiState.update { it.copy(isSystemLogOpen = false) }
            HomeUiEvent.RefreshApps -> loadApps()
        }
    }

    private fun loadApps() {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val group = withContext(dispatcherProvider.io) {
                    getInstalledAppsUseCase()
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        visibleApps = group.visibleApps,
                        hiddenApps = group.hiddenApps,
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
            val currentList = _uiState.value.visibleApps
            val updatedList = withContext(dispatcherProvider.io) {
                moveAppUseCase(currentList, app, direction)
            }
            _uiState.update { it.copy(visibleApps = updatedList) }
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
}
