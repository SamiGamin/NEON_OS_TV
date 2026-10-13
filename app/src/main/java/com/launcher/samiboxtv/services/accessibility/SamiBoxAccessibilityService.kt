package com.launcher.samiboxtv.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.launcher.samiboxtv.MainActivity
import com.launcher.samiboxtv.util.TvRemoteKeyCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Servicio de accesibilidad para Android TV / Google TV.
 * Intercepta la tecla HOME en alta prioridad de hardware tanto en control remoto
 * como en teclado físico (KEYCODE_HOME, KEYCODE_EXPLORER, KEYCODE_META_LEFT, etc.),
 * y vigila en tiempo real las ventanas activas para interceptar launcherx de Google TV
 * redirigiendo al instante a NEON OS TV sin parpadeo (FLAG_ACTIVITY_NO_ANIMATION).
 */
class SamiBoxAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        try {
            // Forzar el filtro de teclas físicas y ventanas interactivas en tiempo de ejecución
            serviceInfo = (serviceInfo ?: AccessibilityServiceInfo()).apply {
                flags = flags or
                        AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                        AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                        AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
                eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
                notificationTimeout = 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        refreshStockLaunchers()

        // En encendido/reinicio en frío (Cold Boot):
        // Google TV intenta ganar el foco en los primeros 1-4 segundos del arranque.
        // Nos aseguramos de traer NEON OS TV al frente de forma secuencial:
        serviceScope.launch {
            bringNeonToFrontInstant()
            delay(1200)
            bringNeonToFrontInstant()
            delay(2500)
            bringNeonToFrontInstant()
        }
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

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        val keyCode = event?.keyCode ?: return false

        // Interceptar el botón de la casita (HOME / TECLADO FÍSICO) ANTES de que Google TV lo reciba
        if (TvRemoteKeyCodes.isHomeKey(keyCode)) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                bringNeonToFrontInstant()
            }
            // Devolver TRUE tanto en DOWN como en UP bloquea a Google TV por completo sin parpadeo
            return true
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return

            // Proteger siempre Ajustes y utilidades críticas del sistema
            if (isExcludedPackage(pkg)) return

            // Si la redirección fue pausada temporalmente (ej. abriendo ajustes)
            if (System.currentTimeMillis() < pauseRedirectUntil) return

            // Si Google TV o cualquier launcher de fábrica intenta abrirse al frente
            if (stockLaunchers.contains(pkg) ||
                pkg == "com.google.android.apps.tv.launcherx" ||
                pkg == "com.google.android.tvlauncher" ||
                pkg == "com.google.android.leanbacklauncher"
            ) {
                bringNeonToFrontInstant()
            }
        }
    }

    private fun bringNeonToFrontInstant() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_HOME)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION // Elimina la animación de transición para cero parpadeo
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
        serviceScope.cancel()
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
