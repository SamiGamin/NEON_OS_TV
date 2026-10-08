package com.launcher.samiboxtv.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.launcher.samiboxtv.MainActivity

/**
 * Servicio de accesibilidad para Android TV que cumple una función crítica:
 * Anclaje agresivo del botón HOME: Intercepta KEYCODE_HOME y vigila cambios de ventana
 * para redirigir a SamiBox TV si un Launcher de fábrica intenta tomar el control.
 */
class SamiBoxAccessibilityService : AccessibilityService() {

    private var stockLaunchers: Set<String> = emptySet()
    private var lastHomeRedirectTime: Long = 0L

    override fun onServiceConnected() {
        instance = this
        refreshStockLaunchers()
    }

    /**
     * Identifica los paquetes de launchers instalados en el dispositivo para vigilancia.
     */
    private fun refreshStockLaunchers() {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolves = packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_ALL)
            stockLaunchers = resolves
                .map { it.activityInfo.packageName }
                .filter { it != packageName && it != "com.launcher.samiboxtv" }
                .toSet()
        } catch (_: Exception) {
            stockLaunchers = emptySet()
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_HOME -> {
                    // Interceptar agresivamente la pulsación del botón HOME
                    launchSamiBoxHome()
                    return true
                }
            }
        }
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return

            // Si el launcher de fábrica intenta abrirse, redirigir a SamiBox TV
            if (stockLaunchers.contains(pkg)) {
                val now = System.currentTimeMillis()
                if (now - lastHomeRedirectTime > 350L) {
                    lastHomeRedirectTime = now
                    launchSamiBoxHome()
                }
            }
        }
    }

    private fun launchSamiBoxHome() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    companion object {
        var instance: SamiBoxAccessibilityService? = null

        fun isRunning() = instance != null
    }
}
