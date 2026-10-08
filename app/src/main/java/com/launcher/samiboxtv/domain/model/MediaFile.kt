package com.launcher.samiboxtv.domain.model

import android.net.Uri

/**
 * Representa un archivo de video o audio local o en unidad USB.
 */
data class MediaFile(
    val name: String,
    val path: String,
    val uri: Uri,
    val sizeMb: Long,
    val type: MediaType,
    val durationMs: Long? = null,
    val dateModified: Long = 0L
)
