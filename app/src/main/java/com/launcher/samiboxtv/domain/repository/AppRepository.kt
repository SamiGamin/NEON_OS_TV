package com.launcher.samiboxtv.domain.repository

import com.launcher.samiboxtv.domain.model.AppItem

/**
 * Contrato de repositorio para interactuar con las aplicaciones instaladas en el sistema.
 */
interface AppRepository {
    suspend fun getInstalledApps(): List<AppItem>
    fun launchApp(packageName: String): Result<Unit>
}
