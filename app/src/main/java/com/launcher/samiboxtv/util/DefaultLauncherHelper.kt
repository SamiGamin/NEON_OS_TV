package com.launcher.samiboxtv.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import com.launcher.samiboxtv.services.accessibility.SamiBoxAccessibilityService

/**
 * Utilidades avanzadas para forzar a SamiBox TV como Launcher predeterminado
 * en TV Box con Android 9, 10 y 11 donde ACTION_HOME_SETTINGS está bloqueado o ausente.
 */
object DefaultLauncherHelper {

    /**
     * Comprueba si SamiBox TV ya es el launcher predeterminado del sistema.
     */
    fun isDefaultLauncher(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == context.packageName
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Técnica de reseteo forzado de preferencias de Launcher.
     * Al habilitar momentáneamente FakeHomeActivity, Android descarta internamente
     * la asociación por defecto del fabricante y se ve forzado a mostrar
     * el diálogo de selección del sistema ("Completar acción con...").
     */
    fun resetAndPromptDefaultLauncher(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            val fakeComponent = ComponentName(context, FakeHomeActivity::class.java)

            // 1. Habilitar componente secundario para invalidar caché de Home en PackageManager
            pm.setComponentEnabledSetting(
                fakeComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // 2. Disparar intent de Home para abrir el selector nativo ResolverActivity
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(homeIntent)

            // 3. Deshabilitar de nuevo el componente secundario
            pm.setComponentEnabledSetting(
                fakeComponent,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Verifica si el servicio de accesibilidad SamiBoxAccessibilityService está activo.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        if (SamiBoxAccessibilityService.isRunning()) return true
        return try {
            val expectedServiceName = "${context.packageName}/${SamiBoxAccessibilityService::class.java.name}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedServiceName, ignoreCase = true) ||
                    componentName.contains(context.packageName)
                ) {
                    return true
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Abre directamente la pantalla de Accesibilidad en Ajustes de la TV.
     */
    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    /**
     * Intenta abrir la pantalla de Aplicaciones Predeterminadas en Android.
     */
    fun openDefaultAppsSettings(context: Context) {
        val intents = listOf(
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_HOME_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Siguiente intento de fallback
            }
        }
    }

    /**
     * Obtiene el listado de paquetes de launchers del sistema instalados
     * (excluyendo a SamiBox TV).
     */
    fun getStockLaunchers(context: Context): Set<String> {
        return try {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolves = context.packageManager.queryIntentActivities(
                homeIntent,
                PackageManager.MATCH_ALL
            )
            resolves
                .map { it.activityInfo.packageName }
                .filter { it != context.packageName && it != "com.launcher.samiboxtv" }
                .toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }
}
