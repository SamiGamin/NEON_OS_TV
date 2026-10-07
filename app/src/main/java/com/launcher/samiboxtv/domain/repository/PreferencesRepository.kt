package com.launcher.samiboxtv.domain.repository

/**
 * Contrato de repositorio para gestionar las preferencias de orden y visibilidad del Launcher.
 */
interface PreferencesRepository {
    suspend fun getHiddenPackages(): Set<String>
    suspend fun hidePackage(packageName: String)
    suspend fun unhidePackage(packageName: String)
    suspend fun getCustomOrder(): List<String>
    suspend fun saveCustomOrder(order: List<String>)
}
