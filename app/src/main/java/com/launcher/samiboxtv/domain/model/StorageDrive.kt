package com.launcher.samiboxtv.domain.model

import java.io.File

/**
 * Representa una unidad de almacenamiento accesible (Memoria Interna o Disco USB).
 */
data class StorageDrive(
    val name: String,
    val path: File,
    val isUsb: Boolean,
    val freeSpaceGb: Long,
    val totalSpaceGb: Long
)
