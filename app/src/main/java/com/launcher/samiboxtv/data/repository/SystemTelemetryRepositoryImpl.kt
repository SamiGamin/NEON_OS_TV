package com.launcher.samiboxtv.data.repository

import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.data.datasource.SystemTelemetryDataSource
import com.launcher.samiboxtv.domain.model.CleanRamResult
import com.launcher.samiboxtv.domain.model.ProcessInfo
import com.launcher.samiboxtv.domain.model.SystemTelemetry
import com.launcher.samiboxtv.domain.repository.SystemTelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de telemetría del sistema.
 */
class SystemTelemetryRepositoryImpl(
    private val systemTelemetryDataSource: SystemTelemetryDataSource,
    private val dispatcherProvider: DispatcherProvider
) : SystemTelemetryRepository {

    override fun observeTelemetry(intervalMillis: Long): Flow<SystemTelemetry> {
        return systemTelemetryDataSource.observeTelemetry(intervalMillis)
            .flowOn(dispatcherProvider.io)
    }

    override fun getCurrentTelemetry(): SystemTelemetry {
        return systemTelemetryDataSource.getCurrentTelemetry()
    }

    override suspend fun getRunningProcesses(): List<ProcessInfo> =
        withContext(dispatcherProvider.io) {
            systemTelemetryDataSource.getRunningProcesses()
        }

    override suspend fun cleanBackgroundProcesses(): CleanRamResult =
        withContext(dispatcherProvider.io) {
            systemTelemetryDataSource.cleanBackgroundProcesses()
        }

    override suspend fun killProcess(packageName: String): Boolean =
        withContext(dispatcherProvider.io) {
            systemTelemetryDataSource.killProcess(packageName)
        }

    override fun clearProcessCache() {
        systemTelemetryDataSource.clearProcessCache()
    }
}
