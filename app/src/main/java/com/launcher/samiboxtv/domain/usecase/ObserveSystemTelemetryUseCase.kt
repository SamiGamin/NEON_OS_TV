package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.SystemTelemetry
import com.launcher.samiboxtv.domain.repository.SystemTelemetryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para observar el flujo en tiempo real de telemetría del sistema (RAM, CPU, Uptime).
 */
class ObserveSystemTelemetryUseCase(
    private val systemTelemetryRepository: SystemTelemetryRepository
) {
    operator fun invoke(intervalMillis: Long = 3000): Flow<SystemTelemetry> =
        systemTelemetryRepository.observeTelemetry(intervalMillis)
}
