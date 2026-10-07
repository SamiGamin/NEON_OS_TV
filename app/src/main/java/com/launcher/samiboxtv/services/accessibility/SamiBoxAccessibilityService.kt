package com.launcher.samiboxtv.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.launcher.samiboxtv.services.overlay.OverlayWindowManager

/**
 * Servicio de accesibilidad que intercepta teclas a nivel global
 * para controlar el System Info Overlay en cualquier pantalla o app.
 */
class SamiBoxAccessibilityService : AccessibilityService() {

    private lateinit var overlayManager: OverlayWindowManager

    override fun onServiceConnected() {
        overlayManager = OverlayWindowManager(applicationContext)
        instance = this
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_MENU -> {
                    if (overlayManager.canDrawOverlays()) {
                        overlayManager.toggle()
                    }
                    return true
                }
            }
        }
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) { /* Sin acción requerida */ }

    override fun onInterrupt() {
        overlayManager.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayManager.hide()
        instance = null
    }

    companion object {
        var instance: SamiBoxAccessibilityService? = null

        fun isRunning() = instance != null
    }
}
