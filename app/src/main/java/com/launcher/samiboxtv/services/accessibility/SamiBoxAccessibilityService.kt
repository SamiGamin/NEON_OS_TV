package com.launcher.samiboxtv.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.launcher.samiboxtv.MainActivity

/**
 * Servicio de accesibilidad para Android TV / Google TV.
 * Intercepta la tecla HOME en alta prioridad de hardware tanto en control remoto
 * como en teclado físico (KEYCODE_HOME, KEYCODE_EXPLORER, KEYCODE_META_LEFT, etc.),
 * y vigila en tiempo real las ventanas activas para interceptar launcherx de Google TV
 * redirigiendo al instante a NEON OS TV.
 */
class SamiBoxAccessibilityService : AccessibilityService() {

    private val knownStockLaunchers = setOf(
        "com.google.android.apps.tv.launcherx",   // Google TV (TCL, Sony, Chromecast, Hisense)
        "com.google.android.tvlauncher",          // Android TV estándar
        "com.google.android.leanbacklauncher",    // Android TV AOSP / Mi Box
        "com.tcl.tv.launcher",                    // TCL Launcher nativo
        "com.tcl.waterfall.overseas",             // TCL Smart TV
        "com.droidlogic.tvlauncher",              // Amlogic Launcher
        "com.droidlogic.launcher",                // Amlogic
        "com.android.launcher3",                  // AOSP Launcher 3
        "com.android.launcher2",                  // AOSP Launcher 2
        "com.android.launcher"                    // AOSP Launcher
    )

    private var stockLaunchers: Set<String> = knownStockLaunchers
    private var lastHomeRedirectTime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        try {
            val info = serviceInfo ?: AccessibilityServiceInfo()
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED
            info.flags = info.flags or
                    AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            serviceInfo = info
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        val found = mutableSetOf<String>()
        found.addAll(knownStockLaunchers)
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolves = packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_ALL)
            for (resolve in resolves) {
                val pkg = resolve.activityInfo.packageName
                if (!isExcludedPackage(pkg) && !resolve.activityInfo.name.contains("FallbackHome", ignoreCase = true)) {
                    found.add(pkg)
                }
            }
        } catch (_: Exception) {}
        stockLaunchers = found
    }

    /**
     * Comprueba si la tecla pulsada corresponde a la casita (Home) en control remoto o teclado físico.
     */
    private fun isHomeKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_HOME ||
                keyCode == KeyEvent.KEYCODE_EXPLORER ||
                keyCode == KeyEvent.KEYCODE_META_LEFT ||
                keyCode == KeyEvent.KEYCODE_META_RIGHT
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event != null && isHomeKey(event.keyCode)) {
            // Si el usuario presiona la Casita (Home) en cualquier momento (Down o Up)
            if (event.action == KeyEvent.ACTION_DOWN) {
                bringNeonToFront()
            }
            // Consumimos el evento por completo para que Android y Google TV no abran launcherx
            return true
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventType = event.eventType
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            val pkg = event.packageName?.toString() ?: return

            // Proteger siempre Ajustes y utilidades críticas del sistema
            if (isExcludedPackage(pkg)) return

            // Si la redirección fue pausada temporalmente (ej. abriendo ajustes)
            if (System.currentTimeMillis() < pauseRedirectUntil) return

            // Si Google TV o cualquier launcher de fábrica intenta abrirse al frente
            if (stockLaunchers.contains(pkg) || pkg == "com.google.android.apps.tv.launcherx") {
                val now = System.currentTimeMillis()
                if (now - lastHomeRedirectTime > 150L) {
                    lastHomeRedirectTime = now
                    bringNeonToFront()
                }
            }
        }
    }

    private fun bringNeonToFront() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_HOME)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
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
