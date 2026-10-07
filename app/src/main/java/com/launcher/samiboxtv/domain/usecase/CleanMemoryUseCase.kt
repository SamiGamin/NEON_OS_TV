package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.CleanRamResult
import com.launcher.samiboxtv.domain.repository.SystemTelemetryRepository

/**
 * Caso de uso para limpiar procesos en segundo plano y liberar memoria RAM.
 */
class CleanMemoryUseCase(
    private val systemTelemetryRepository: SystemTelemetryRepository
) {
    suspend operator fun invoke(): CleanRamResult =
        systemTelemetryRepository.cleanBackgroundProcesses()
}
