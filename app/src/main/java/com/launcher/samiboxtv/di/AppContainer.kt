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
import com.launcher.samiboxtv.domain.usecase.UnhideAppUseCase

/**
 * Contenedor de Inyección de Dependencias (DI Container) de la aplicación.
 * Provee instancias de Repositorios, DataSources, Dispatchers y Casos de Uso.
 */
interface AppContainer {
    val dispatcherProvider: DispatcherProvider
    val appRepository: AppRepository
    val preferencesRepository: PreferencesRepository
    val getInstalledAppsUseCase: GetInstalledAppsUseCase
    val hideAppUseCase: HideAppUseCase
    val unhideAppUseCase: UnhideAppUseCase
    val moveAppUseCase: MoveAppUseCase
    val launchAppUseCase: LaunchAppUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
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
}
