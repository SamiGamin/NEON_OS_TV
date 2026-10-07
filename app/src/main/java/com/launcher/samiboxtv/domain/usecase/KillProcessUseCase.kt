package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.repository.SystemTelemetryRepository

/**
 * Caso de uso para detener los procesos en segundo plano de una app específica.
 */
class KillProcessUseCase(
    private val systemTelemetryRepository: SystemTelemetryRepository
) {
    suspend operator fun invoke(packageName: String): Boolean =
        systemTelemetryRepository.killProcess(packageName)
}
