package com.launcher.samiboxtv.data.datasource

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.launcher.samiboxtv.data.model.AppEntity

/**
 * Fuente de datos local encargada de consultar e interactuar con el PackageManager de Android.
 */
interface AppLocalDataSource {
    fun getInstalledApplications(): List<AppEntity>
    fun launchApplication(packageName: String): Result<Unit>
}

class AppLocalDataSourceImpl(
    private val context: Context
) : AppLocalDataSource {

    override fun getInstalledApplications(): List<AppEntity> {
        val packageManager = context.packageManager
        val apps = mutableListOf<AppEntity>()
        val packages = packageManager.getInstalledPackages(0)

        for (packageInfo in packages) {
            val packageName = packageInfo.packageName

            // Excluir nuestro propio launcher para no abrirse a sí mismo
            if (packageName == context.packageName) continue

            // Verificar si el paquete tiene un Intent de inicio
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                try {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    val name = packageManager.getApplicationLabel(appInfo).toString()
                    val icon = packageManager.getApplicationIcon(appInfo)
                    val banner = try {
                        packageManager.getApplicationBanner(appInfo)
                    } catch (_: Exception) {
                        null
                    }
                    val activityName = launchIntent.component?.className ?: ""
                    val category = com.launcher.samiboxtv.util.CategoryHelper.detectCategory(appInfo, name, packageName)

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
                } catch (_: PackageManager.NameNotFoundException) {
                    // Ignorar paquete si dejó de existir durante la iteración
                }
            }
        }

        return apps.sortedBy { it.name.lowercase() }
    }

    override fun launchApplication(packageName: String): Result<Unit> {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: return Result.failure(Exception("No se encontró intent de lanzamiento para $packageName"))

        return runCatching {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
    }
}
