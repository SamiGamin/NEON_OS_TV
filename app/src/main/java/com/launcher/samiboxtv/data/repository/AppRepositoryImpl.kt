package com.launcher.samiboxtv.data.repository

import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.data.datasource.AppLocalDataSource
import com.launcher.samiboxtv.data.mapper.AppMapper
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.repository.AppRepository
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de aplicaciones utilizando AppLocalDataSource y Coroutines.
 */
class AppRepositoryImpl(
    private val localDataSource: AppLocalDataSource,
    private val dispatcherProvider: DispatcherProvider
) : AppRepository {

    override suspend fun getInstalledApps(): List<AppItem> = withContext(dispatcherProvider.io) {
        localDataSource.getInstalledApplications().map { AppMapper.toDomain(it) }
    }

    override fun launchApp(packageName: String): Result<Unit> {
        return localDataSource.launchApplication(packageName)
    }
}
