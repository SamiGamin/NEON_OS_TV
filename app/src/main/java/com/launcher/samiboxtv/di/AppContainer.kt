package com.launcher.samiboxtv.di

import android.content.Context
import com.launcher.samiboxtv.core.dispatcher.DefaultDispatcherProvider
import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.data.datasource.AppLocalDataSource
import com.launcher.samiboxtv.data.datasource.AppLocalDataSourceImpl
import com.launcher.samiboxtv.data.datasource.PreferencesDataSource
import com.launcher.samiboxtv.data.datasource.PreferencesDataSourceImpl
import com.launcher.samiboxtv.data.repository.AppRepositoryImpl
import com.launcher.samiboxtv.data.repository.PreferencesRepositoryImpl
import com.launcher.samiboxtv.domain.repository.AppRepository
import com.launcher.samiboxtv.domain.repository.PreferencesRepository
import com.launcher.samiboxtv.domain.usecase.GetInstalledAppsUseCase
import com.launcher.samiboxtv.domain.usecase.HideAppUseCase
import com.launcher.samiboxtv.domain.usecase.LaunchAppUseCase
import com.launcher.samiboxtv.domain.usecase.MoveAppUseCase
import com.launcher.samiboxtv.domain.usecase.SetHiddenPackagesUseCase
import com.launcher.samiboxtv.domain.usecase.ToggleAppVisibilityUseCase
import com.launcher.samiboxtv.data.datasource.NetworkDataSource
import com.launcher.samiboxtv.data.datasource.NetworkDataSourceImpl
import com.launcher.samiboxtv.data.datasource.SystemTelemetryDataSource
import com.launcher.samiboxtv.data.datasource.SystemTelemetryDataSourceImpl
import com.launcher.samiboxtv.data.repository.NetworkRepositoryImpl
import com.launcher.samiboxtv.data.repository.SystemTelemetryRepositoryImpl
import com.launcher.samiboxtv.data.repository.UpdateRepositoryImpl
import com.launcher.samiboxtv.domain.repository.NetworkRepository
import com.launcher.samiboxtv.domain.repository.SystemTelemetryRepository
import com.launcher.samiboxtv.domain.repository.UpdateRepository
import com.launcher.samiboxtv.domain.usecase.CheckUpdateUseCase
import com.launcher.samiboxtv.domain.usecase.CleanMemoryUseCase
import com.launcher.samiboxtv.domain.usecase.GetRunningProcessesUseCase
import com.launcher.samiboxtv.domain.usecase.KillProcessUseCase
import com.launcher.samiboxtv.domain.usecase.ObserveNetworkStatusUseCase
import com.launcher.samiboxtv.domain.usecase.ObserveSystemTelemetryUseCase
import com.launcher.samiboxtv.domain.usecase.ToggleFavoriteAppUseCase
import com.launcher.samiboxtv.domain.usecase.UnhideAppUseCase

/**
 * Contenedor de Inyección de Dependencias (DI Container) de la aplicación.
 * Provee instancias de Repositorios, DataSources, Dispatchers y Casos de Uso.
 */
interface AppContainer {
    val dispatcherProvider: DispatcherProvider
    val appRepository: AppRepository
    val preferencesRepository: PreferencesRepository
    val networkRepository: NetworkRepository
    val systemTelemetryRepository: SystemTelemetryRepository
    val updateRepository: UpdateRepository
    val getInstalledAppsUseCase: GetInstalledAppsUseCase
    val hideAppUseCase: HideAppUseCase
    val unhideAppUseCase: UnhideAppUseCase
    val moveAppUseCase: MoveAppUseCase
    val launchAppUseCase: LaunchAppUseCase
    val toggleAppVisibilityUseCase: ToggleAppVisibilityUseCase
    val toggleFavoriteAppUseCase: ToggleFavoriteAppUseCase
    val setHiddenPackagesUseCase: SetHiddenPackagesUseCase
    val observeNetworkStatusUseCase: ObserveNetworkStatusUseCase
    val observeSystemTelemetryUseCase: ObserveSystemTelemetryUseCase
    val checkUpdateUseCase: CheckUpdateUseCase
    val getRunningProcessesUseCase: GetRunningProcessesUseCase
    val cleanMemoryUseCase: CleanMemoryUseCase
    val killProcessUseCase: KillProcessUseCase
    val getLauncherSettingsUseCase: com.launcher.samiboxtv.domain.usecase.GetLauncherSettingsUseCase
    val saveCardStyleUseCase: com.launcher.samiboxtv.domain.usecase.SaveCardStyleUseCase
    val manageCategoriesUseCase: com.launcher.samiboxtv.domain.usecase.ManageCategoriesUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }

    private val networkDataSource: NetworkDataSource by lazy {
        NetworkDataSourceImpl(context)
    }

    override val networkRepository: NetworkRepository by lazy {
        NetworkRepositoryImpl(
            networkDataSource = networkDataSource,
            dispatcherProvider = dispatcherProvider
        )
    }

    override val observeNetworkStatusUseCase: ObserveNetworkStatusUseCase by lazy {
        ObserveNetworkStatusUseCase(networkRepository = networkRepository)
    }

    private val systemTelemetryDataSource: SystemTelemetryDataSource by lazy {
        SystemTelemetryDataSourceImpl(context)
    }

    override val systemTelemetryRepository: SystemTelemetryRepository by lazy {
        SystemTelemetryRepositoryImpl(
            systemTelemetryDataSource = systemTelemetryDataSource,
            dispatcherProvider = dispatcherProvider
        )
    }

    override val observeSystemTelemetryUseCase: ObserveSystemTelemetryUseCase by lazy {
        ObserveSystemTelemetryUseCase(systemTelemetryRepository = systemTelemetryRepository)
    }

    override val getRunningProcessesUseCase: GetRunningProcessesUseCase by lazy {
        GetRunningProcessesUseCase(systemTelemetryRepository = systemTelemetryRepository)
    }

    override val cleanMemoryUseCase: CleanMemoryUseCase by lazy {
        CleanMemoryUseCase(systemTelemetryRepository = systemTelemetryRepository)
    }

    override val killProcessUseCase: KillProcessUseCase by lazy {
        KillProcessUseCase(systemTelemetryRepository = systemTelemetryRepository)
    }

    override val updateRepository: UpdateRepository by lazy {
        UpdateRepositoryImpl(
            context = context,
            dispatcherProvider = dispatcherProvider
        )
    }

    override val checkUpdateUseCase: CheckUpdateUseCase by lazy {
        CheckUpdateUseCase(repository = updateRepository)
    }

    private val appLocalDataSource: AppLocalDataSource by lazy {
        AppLocalDataSourceImpl(context)
    }

    private val preferencesDataSource: PreferencesDataSource by lazy {
        PreferencesDataSourceImpl(context)
    }

    override val appRepository: AppRepository by lazy {
        AppRepositoryImpl(
            localDataSource = appLocalDataSource,
            dispatcherProvider = dispatcherProvider
        )
    }

    override val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(
            preferencesDataSource = preferencesDataSource,
            dispatcherProvider = dispatcherProvider
        )
    }

    override val getInstalledAppsUseCase: GetInstalledAppsUseCase by lazy {
        GetInstalledAppsUseCase(
            appRepository = appRepository,
            preferencesRepository = preferencesRepository
        )
    }

    override val hideAppUseCase: HideAppUseCase by lazy {
        HideAppUseCase(preferencesRepository = preferencesRepository)
    }

    override val unhideAppUseCase: UnhideAppUseCase by lazy {
        UnhideAppUseCase(preferencesRepository = preferencesRepository)
    }

    override val moveAppUseCase: MoveAppUseCase by lazy {
        MoveAppUseCase(preferencesRepository = preferencesRepository)
    }

    override val launchAppUseCase: LaunchAppUseCase by lazy {
        LaunchAppUseCase(appRepository = appRepository)
    }

    override val toggleAppVisibilityUseCase: ToggleAppVisibilityUseCase by lazy {
        ToggleAppVisibilityUseCase(preferencesRepository = preferencesRepository)
    }

    override val toggleFavoriteAppUseCase: ToggleFavoriteAppUseCase by lazy {
        ToggleFavoriteAppUseCase(preferencesRepository = preferencesRepository)
    }

    override val setHiddenPackagesUseCase: SetHiddenPackagesUseCase by lazy {
        SetHiddenPackagesUseCase(preferencesRepository = preferencesRepository)
    }

    override val getLauncherSettingsUseCase: com.launcher.samiboxtv.domain.usecase.GetLauncherSettingsUseCase by lazy {
        com.launcher.samiboxtv.domain.usecase.GetLauncherSettingsUseCase(preferencesRepository = preferencesRepository)
    }

    override val saveCardStyleUseCase: com.launcher.samiboxtv.domain.usecase.SaveCardStyleUseCase by lazy {
        com.launcher.samiboxtv.domain.usecase.SaveCardStyleUseCase(preferencesRepository = preferencesRepository)
    }

    override val manageCategoriesUseCase: com.launcher.samiboxtv.domain.usecase.ManageCategoriesUseCase by lazy {
        com.launcher.samiboxtv.domain.usecase.ManageCategoriesUseCase(preferencesRepository = preferencesRepository)
    }
}
