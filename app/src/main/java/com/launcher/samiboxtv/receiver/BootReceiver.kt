package com.launcher.samiboxtv.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.launcher.samiboxtv.MainActivity
import com.launcher.samiboxtv.util.DevLogManager

/**
 * Receptor de eventos de inicio del sistema (Cold Boot / Reinicio completo).
 * Garantiza que NEON OS TV sea lanzado automáticamente al encender o reiniciar el televisor,
 * evitando que Google TV Launcher se quede con el control de la pantalla tras un apagado total.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        DevLogManager.log("BOOT", "Broadcast recibido en inicio: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                this.action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_HOME)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
            try {
                context.startActivity(launchIntent)
            } catch (e: Exception) {
                DevLogManager.log("BOOT", "Error al iniciar en boot: ${e.message}")
            }
        }
    }
}
