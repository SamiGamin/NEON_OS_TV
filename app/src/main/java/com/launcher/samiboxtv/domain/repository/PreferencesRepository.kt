package com.launcher.samiboxtv.domain.repository

/**
 * Contrato de repositorio para gestionar las preferencias de orden y visibilidad del Launcher.
 */
interface PreferencesRepository {
    suspend fun getHiddenPackages(): Set<String>
    suspend fun hidePackage(packageName: String)
    suspend fun unhidePackage(packageName: String)
    suspend fun setHiddenPackages(packageNames: Set<String>)
    suspend fun toggleHidePackage(packageName: String): Boolean
    suspend fun getFavoritePackages(): Set<String>
    suspend fun toggleFavoritePackage(packageName: String): Boolean
    suspend fun getCustomOrder(): List<String>
    suspend fun saveCustomOrder(order: List<String>)
    suspend fun getCardStyle(): com.launcher.samiboxtv.domain.model.AppCardStyle
    suspend fun setCardStyle(style: com.launcher.samiboxtv.domain.model.AppCardStyle)
    suspend fun getCustomCategories(): List<String>
    suspend fun addCustomCategory(categoryName: String): Boolean
    suspend fun removeCustomCategory(categoryName: String): Boolean
    suspend fun getAppCategoryMap(): Map<String, String>
    suspend fun setAppCategory(packageName: String, categoryName: String)
    suspend fun getShowAppNames(): Boolean
    suspend fun setShowAppNames(show: Boolean)
}
