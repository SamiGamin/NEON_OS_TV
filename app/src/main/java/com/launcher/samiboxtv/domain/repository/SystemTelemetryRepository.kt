package com.launcher.samiboxtv.domain.repository

import com.launcher.samiboxtv.domain.model.CleanRamResult
import com.launcher.samiboxtv.domain.model.ProcessInfo
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

    /**
     * Obtiene los procesos y apps actualmente cargados en memoria RAM.
     */
    suspend fun getRunningProcesses(): List<ProcessInfo>

    /**
     * Limpia procesos en segundo plano y libera memoria RAM.
     */
    suspend fun cleanBackgroundProcesses(): CleanRamResult

    /**
     * Finaliza los procesos en segundo plano de un paquete específico.
     */
    suspend fun killProcess(packageName: String): Boolean

    /**
     * Limpia la caché en memoria de metadatos e iconos (Drawables) de procesos para evitar fugas de contexto.
     */
    fun clearProcessCache()
}
