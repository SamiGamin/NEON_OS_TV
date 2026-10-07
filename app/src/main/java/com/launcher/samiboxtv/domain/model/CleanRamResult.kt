package com.launcher.samiboxtv.domain.model

/**
 * Resultado de una operación de limpieza y liberación de memoria RAM.
 */
data class CleanRamResult(
    val killedProcessesCount: Int,
    val freedMemoryMb: Long,
    val initialAvailableMb: Long,
    val finalAvailableMb: Long
)
