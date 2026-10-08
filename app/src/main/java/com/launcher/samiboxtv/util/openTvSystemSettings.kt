package com.launcher.samiboxtv.util

import android.content.Context
import android.content.Intent
import android.provider.Settings

fun openTvSystemSettings(context: Context) {
    val pm = context.packageManager

    // Intento 1: Categoría oficial de Ajustes Leanback (Android TV / Google TV)
    val leanbackIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory("android.intent.category.LEANBACK_SETTINGS")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (leanbackIntent.resolveActivity(pm) != null) {
        context.startActivity(leanbackIntent)
        return
    }

    // Intento 2: Paquete explícito de ajustes de Android TV
    val tvSettingsIntent = Intent().apply {
        setClassName("com.android.tv.settings", "com.android.tv.settings.MainSettings")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(tvSettingsIntent)
        return
    } catch (_: Exception) {}

    // Intento 3: Intent estándar ACTION_SETTINGS
    try {
        val standardIntent = Intent(Settings.ACTION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(standardIntent)
        return
    } catch (_: Exception) {}

    // Intento 4 (Fallback para TV Boxes genéricos Rockchip / Amlogic):
    val fallbackPackages = listOf(
        "com.rockchip.settings",
        "com.android.settings"
    )
    for (pkg in fallbackPackages) {
        val launchIntent = pm.getLaunchIntentForPackage(pkg)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return
        }
    }
}