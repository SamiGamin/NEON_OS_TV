package com.launcher.samiboxtv.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.launcher.samiboxtv.presentation.home.HomeViewModel

/**
 * Factory para instanciar ViewModels inyectando los casos de uso correspondientes.
 */
class ViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                getInstalledAppsUseCase = appContainer.getInstalledAppsUseCase,
                hideAppUseCase = appContainer.hideAppUseCase,
                unhideAppUseCase = appContainer.unhideAppUseCase,
                moveAppUseCase = appContainer.moveAppUseCase,
                launchAppUseCase = appContainer.launchAppUseCase,
                toggleAppVisibilityUseCase = appContainer.toggleAppVisibilityUseCase,
                toggleFavoriteAppUseCase = appContainer.toggleFavoriteAppUseCase,
                setHiddenPackagesUseCase = appContainer.setHiddenPackagesUseCase,
                observeNetworkStatusUseCase = appContainer.observeNetworkStatusUseCase,
                observeSystemTelemetryUseCase = appContainer.observeSystemTelemetryUseCase,
                checkUpdateUseCase = appContainer.checkUpdateUseCase,
                getRunningProcessesUseCase = appContainer.getRunningProcessesUseCase,
                cleanMemoryUseCase = appContainer.cleanMemoryUseCase,
                killProcessUseCase = appContainer.killProcessUseCase,
                getLauncherSettingsUseCase = appContainer.getLauncherSettingsUseCase,
                saveCardStyleUseCase = appContainer.saveCardStyleUseCase,
                manageCategoriesUseCase = appContainer.manageCategoriesUseCase,
                dispatcherProvider = appContainer.dispatcherProvider
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
