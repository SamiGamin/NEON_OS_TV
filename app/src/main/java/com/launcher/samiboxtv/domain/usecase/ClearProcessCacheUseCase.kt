package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.repository.SystemTelemetryRepository

/**
 * Caso de uso para limpiar la caché en memoria de procesos e iconos (Drawables)
 * al cerrar la sesión de telemetría, evitando fugas de memoria y contexto.
 */
class ClearProcessCacheUseCase(
    private val systemTelemetryRepository: SystemTelemetryRepository
) {
    operator fun invoke() {
        systemTelemetryRepository.clearProcessCache()
    }
}
