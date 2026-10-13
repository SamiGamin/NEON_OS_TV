package com.launcher.samiboxtv.util

import android.content.Context

/**
 * Abre los ajustes del sistema delegando a DefaultLauncherHelper con fallback secuencial completo.
 */
fun openTvSystemSettings(context: Context) {
    DefaultLauncherHelper.openSystemSettings(context)
}