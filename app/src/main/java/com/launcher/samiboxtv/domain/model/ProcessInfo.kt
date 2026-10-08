package com.launcher.samiboxtv.domain.model

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

/**
 * Representa la información de un proceso o aplicación en ejecución en memoria RAM.
 */
@Immutable
data class ProcessInfo(
    val pid: Int,
    val processName: String,
    val appName: String,
    val packageName: String,
    val memoryUsageMb: Long,
    val isSystemApp: Boolean,
    val icon: Drawable? = null
)
