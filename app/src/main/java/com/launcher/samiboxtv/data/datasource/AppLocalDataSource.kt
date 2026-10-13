package com.launcher.samiboxtv.data.datasource

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.launcher.samiboxtv.data.model.AppEntity
import com.launcher.samiboxtv.util.CategoryHelper

/**
 * Fuente de datos local encargada de consultar e interactuar con el PackageManager de Android.
 * Optimizado para Android TV: utiliza queryIntentActivities con categorías Leanback y Launcher
 * en lugar de iterar cientos de paquetes del sistema, e implementa caché en memoria.
 */
interface AppLocalDataSource {
    fun getInstalledApplications(forceRefresh: Boolean = false): List<AppEntity>
    fun invalidateCache()
    fun launchApplication(packageName: String): Result<Unit>
}

class AppLocalDataSourceImpl(
    private val context: Context
) : AppLocalDataSource {

    @Volatile
    private var cachedApps: List<AppEntity>? = null

    override fun invalidateCache() {
        cachedApps = null
    }

    override fun getInstalledApplications(forceRefresh: Boolean): List<AppEntity> {
        if (!forceRefresh) {
            cachedApps?.let { return it }
        }

        val packageManager = context.packageManager
        val apps = mutableListOf<AppEntity>()

        // 1. Obtener aplicaciones nativas de TV (Leanback)
        val tvIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val tvActivities = packageManager.queryIntentActivities(tvIntent, 0)

        // 2. Obtener aplicaciones estándar / móviles / sideloaded
        val regularIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val regularActivities = packageManager.queryIntentActivities(regularIntent, 0)

        // Combinar en mapa único por paquete, priorizando la entrada Leanback si existe
        val uniquePackages = linkedMapOf<String, ResolveInfo>()
        for (resolve in tvActivities) {
            val pkg = resolve.activityInfo.packageName
            if (pkg != context.packageName) {
                uniquePackages[pkg] = resolve
            }
        }
        for (resolve in regularActivities) {
            val pkg = resolve.activityInfo.packageName
            if (pkg != context.packageName && !uniquePackages.containsKey(pkg)) {
                uniquePackages[pkg] = resolve
            }
        }

        for ((packageName, resolve) in uniquePackages) {
            try {
                val appInfo = resolve.activityInfo.applicationInfo ?: packageManager.getApplicationInfo(packageName, 0)
                val name = resolve.loadLabel(packageManager).toString().ifBlank { packageName }
                val icon = resolve.loadIcon(packageManager)
                val banner = try {
                    resolve.activityInfo.loadBanner(packageManager)
                } catch (_: Exception) {
                    null
                }
                val activityName = resolve.activityInfo.name
                val category = CategoryHelper.detectCategory(appInfo, name, packageName)

                apps.add(
                    AppEntity(
                        name = name,
                        packageName = packageName,
                        activityName = activityName,
                        icon = icon,
                        banner = banner,
                        category = category
                    )
                )
            } catch (_: Exception) {
                // Si el paquete fue desinstalado en el proceso, ignorar
            }
        }

        val sorted = apps.sortedBy { it.name.lowercase() }
        cachedApps = sorted
        return sorted
    }

    override fun launchApplication(packageName: String): Result<Unit> {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(packageName)
            ?: packageManager.getLaunchIntentForPackage(packageName)
            ?: return Result.failure(Exception("No se encontró intent de lanzamiento para $packageName"))

        return runCatching {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
    }
}
