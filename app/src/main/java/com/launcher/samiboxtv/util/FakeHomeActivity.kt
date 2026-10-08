package com.launcher.samiboxtv.util

import android.app.Activity
import android.os.Bundle

/**
 * Actividad secundaria temporal utilizada para invalidar la preferencia
 * de Launcher predeterminado guardada por el sistema Android y forzar
 * la aparición del diálogo nativo del sistema ("Completar acción con...").
 */
class FakeHomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
