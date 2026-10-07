package com.launcher.samiboxtv.data.repository

import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.data.datasource.SystemTelemetryDataSource
import com.launcher.samiboxtv.domain.model.SystemTelemetry
import com.launcher.samiboxtv.domain.repository.SystemTelemetryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

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
}
