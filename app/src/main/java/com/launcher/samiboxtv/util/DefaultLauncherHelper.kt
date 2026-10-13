package com.launcher.samiboxtv.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import com.launcher.samiboxtv.services.accessibility.SamiBoxAccessibilityService

/**
 * Utilidades avanzadas para forzar a SamiBox TV como Launcher predeterminado
 * en TV Box con Android 9, 10 y 11 donde ACTION_HOME_SETTINGS está bloqueado o ausente.
 */
object DefaultLauncherHelper {

    private const val TAG = "DefaultLauncherHelper"

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
     * Abre los ajustes generales del sistema probando las rutas conocidas de Android TV y TV Box.
     */
    fun openSystemSettings(context: Context) {
        SamiBoxAccessibilityService.pauseRedirect(15000L)
        val candidates = listOf(
            // 1. Lanzador directo del paquete de Ajustes de Google TV / TCL / Android TV
            context.packageManager.getLaunchIntentForPackage("com.android.tv.settings"),
            // 2. Actividad principal de Ajustes en Google TV / TCL
            Intent().apply {
                component = ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings")
            },
            // 3. Ajustes de Android TV alternativo
            Intent("android.settings.SETTINGS").apply {
                setPackage("com.android.tv.settings")
            },
            // 4. Ajustes de TV Box Amlogic / Genéricas
            Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName("com.droidlogic.tv.settings", "com.droidlogic.tv.settings.Settings")
            },
            // 5. Categoría Leanback Settings
            Intent(Intent.ACTION_MAIN).apply {
                addCategory("android.intent.category.LEANBACK_SETTINGS")
            },
            // 6. Ajustes de Rockchip TV
            Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName("com.rockchip.settings", "com.rockchip.settings.Settings")
            },
            // 7. Intent general de Android
            Intent(Settings.ACTION_SETTINGS),
            // 8. Ajustes estándar de AOSP / Teléfono
            Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName("com.android.settings", "com.android.settings.Settings")
            }
        )

        for (intent in candidates.filterNotNull()) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Log.d(TAG, "Ajustes abiertos exitosamente con: ${intent.component ?: intent.action}")
                return
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo abrir con ${intent.component ?: intent.action}: ${e.message}")
            }
        }

        Log.e(TAG, "Ninguna ruta de ajustes fue compatible con este dispositivo.")
    }

    /**
     * Abre los ajustes de accesibilidad. En Google TV (TCL), ACTION_ACCESSIBILITY_SETTINGS es bloqueado
     * por el fabricante y muestra "Ninguna aplicación puede procesar esta acción", por lo que abrimos
     * directamente la pantalla de ajustes de TV para acceder a la sección del sistema.
     */
    fun openAccessibilitySettings(context: Context) {
        SamiBoxAccessibilityService.pauseRedirect(15000L)
        val tvSettingsIntent = Intent().apply {
            component = ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(tvSettingsIntent)
        } catch (_: Exception) {
            openSystemSettings(context)
        }
    }

    /**
     * Abre los ajustes de aplicaciones predeterminadas o la lista de apps con fallback secuencial.
     */
    fun openDefaultAppsSettings(context: Context) {
        SamiBoxAccessibilityService.pauseRedirect(15000L)
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
            } catch (_: Exception) {}
        }

        // Fallback final a los ajustes generales si las pantallas específicas no existen
        openSystemSettings(context)
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
