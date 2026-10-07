package com.launcher.samiboxtv.domain.repository

import com.launcher.samiboxtv.domain.model.SystemTelemetry
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de repositorio para monitorear la telemetría del sistema (RAM, CPU, Almacenamiento).
 */
interface SystemTelemetryRepository {
    /**
     * Emite actualizaciones periódicas con el estado de hardware y memoria RAM.
     */
    fun observeTelemetry(intervalMillis: Long = 3000): Flow<SystemTelemetry>

    /**
     * Obtiene una lectura sincrónica puntual del estado actual de hardware.
     */
    fun getCurrentTelemetry(): SystemTelemetry
}
