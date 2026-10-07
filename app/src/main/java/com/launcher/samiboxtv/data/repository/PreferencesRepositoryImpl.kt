package com.launcher.samiboxtv.data.repository

import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.data.datasource.PreferencesDataSource
import com.launcher.samiboxtv.domain.repository.PreferencesRepository
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de preferencias utilizando PreferencesDataSource.
 */
class PreferencesRepositoryImpl(
    private val preferencesDataSource: PreferencesDataSource,
    private val dispatcherProvider: DispatcherProvider
) : PreferencesRepository {

    override suspend fun getHiddenPackages(): Set<String> = withContext(dispatcherProvider.io) {
        preferencesDataSource.getHiddenApps()
    }

    override suspend fun hidePackage(packageName: String) = withContext(dispatcherProvider.io) {
        preferencesDataSource.addHiddenApp(packageName)
    }

    override suspend fun unhidePackage(packageName: String) = withContext(dispatcherProvider.io) {
        preferencesDataSource.removeHiddenApp(packageName)
    }

    override suspend fun getCustomOrder(): List<String> = withContext(dispatcherProvider.io) {
        preferencesDataSource.getCustomOrder()
    }

    override suspend fun saveCustomOrder(order: List<String>) = withContext(dispatcherProvider.io) {
        preferencesDataSource.saveCustomOrder(order)
    }
}
