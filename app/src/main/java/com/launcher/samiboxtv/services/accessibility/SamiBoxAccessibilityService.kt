package com.launcher.samiboxtv.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.launcher.samiboxtv.MainActivity

/**
 * Servicio de accesibilidad para Android TV / Google TV.
 * Intercepta la tecla KEYCODE_HOME en alta prioridad de hardware antes de que
 * el sistema operativo o Google TV la reciban, consumiendo el evento por completo (return true)
 * para eliminar el retardo o parpadeo del launcher original.
 */
class SamiBoxAccessibilityService : AccessibilityService() {

    private var stockLaunchers: Set<String> = emptySet()
    private var lastHomeRedirectTime: Long = 0L

    override fun onServiceConnected() {
        instance = this
        refreshStockLaunchers()
    }

    private fun isExcludedPackage(pkg: String): Boolean {
        if (pkg == packageName || pkg == "com.launcher.samiboxtv") return true
        val lower = pkg.lowercase()
        return lower.contains("settings") ||
                lower.contains("setupwizard") ||
                lower.contains("packageinstaller") ||
                lower.contains("systemui") ||
                lower.contains("permissioncontroller")
    }

    /**
     * Identifica los paquetes de launchers instalados en el dispositivo para vigilancia de respaldo.
     */
    private fun refreshStockLaunchers() {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolves = packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_ALL)
            stockLaunchers = resolves
                .filter { resolve ->
                    val pkg = resolve.activityInfo.packageName
                    val actName = resolve.activityInfo.name
                    !isExcludedPackage(pkg) && !actName.contains("FallbackHome", ignoreCase = true)
                }
                .map { it.activityInfo.packageName }
                .toSet()
        } catch (_: Exception) {
            stockLaunchers = emptySet()
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event != null && event.keyCode == KeyEvent.KEYCODE_HOME) {
            // Si el usuario presiona la Casita (Home) en cualquier momento (Down o Up)
            if (event.action == KeyEvent.ACTION_DOWN) {
                bringNeonToFront()
            }
            // Consumimos el evento por completo (tanto DOWN como UP) para que Android
            // y Google TV jamás se enteren de que se presionó Home, eliminando el parpadeo.
            return true
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return

            // Proteger siempre Ajustes y utilidades críticas del sistema
            if (isExcludedPackage(pkg)) return

            // Si la redirección fue pausada temporalmente (ej. abriendo ajustes)
            if (System.currentTimeMillis() < pauseRedirectUntil) return

            // Si un launcher de fábrica antiguo intenta abrirse de fondo
            if (stockLaunchers.contains(pkg)) {
                val now = System.currentTimeMillis()
                if (now - lastHomeRedirectTime > 350L) {
                    lastHomeRedirectTime = now
                    bringNeonToFront()
                }
            }
        }
    }

    private fun bringNeonToFront() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
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
        private var pauseRedirectUntil: Long = 0L

        fun isRunning() = instance != null

        fun pauseRedirect(durationMs: Long = 10000L) {
            pauseRedirectUntil = System.currentTimeMillis() + durationMs
        }

        fun stopService() {
            instance?.disableSelf()
            instance = null
        }
    }
}
