package com.launcher.samiboxtv.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object AppActionsHelper {

    /**
     * Abre los ajustes nativos de Android de la app (para forzar detención, borrar datos o permisos)
     */
    fun openAppSettings(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Lanza el diálogo del sistema de Android para desinstalar la app
     */
    fun launchUninstallApp(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}