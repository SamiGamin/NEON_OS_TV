package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.ProcessInfo
import com.launcher.samiboxtv.domain.repository.SystemTelemetryRepository

/**
 * Caso de uso para obtener los procesos y aplicaciones activas en memoria RAM.
 */
class GetRunningProcessesUseCase(
    private val systemTelemetryRepository: SystemTelemetryRepository
) {
    suspend operator fun invoke(): List<ProcessInfo> =
        systemTelemetryRepository.getRunningProcesses()
}
