package com.launcher.samiboxtv.data.repository

import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.data.datasource.PreferencesDataSource
import com.launcher.samiboxtv.domain.repository.PreferencesRepository
import com.launcher.samiboxtv.presentation.theme.AppLayoutMode
import kotlinx.coroutines.withContext

/**
 * Implementación del repositorio de preferencias utilizando PreferencesDataSource.
 */
class PreferencesRepositoryImpl(
    private val preferencesDataSource: PreferencesDataSource,
    private val dispatcherProvider: DispatcherProvider
) : PreferencesRepository {

    override suspend fun getHiddenPackages(): Set<String> = withContext(dispatcherProvider.io) {
        preferencesDataSource.getHiddenApps()
    }

    override suspend fun hidePackage(packageName: String) = withContext(dispatcherProvider.io) {
        preferencesDataSource.addHiddenApp(packageName)
    }

    override suspend fun unhidePackage(packageName: String) = withContext(dispatcherProvider.io) {
        preferencesDataSource.removeHiddenApp(packageName)
    }

    override suspend fun setHiddenPackages(packageNames: Set<String>) = withContext(dispatcherProvider.io) {
        preferencesDataSource.setHiddenApps(packageNames)
    }

    override suspend fun toggleHidePackage(packageName: String): Boolean = withContext(dispatcherProvider.io) {
        preferencesDataSource.toggleHiddenApp(packageName)
    }

    override suspend fun getFavoritePackages(): Set<String> = withContext(dispatcherProvider.io) {
        preferencesDataSource.getFavoriteApps()
    }

    override suspend fun toggleFavoritePackage(packageName: String): Boolean = withContext(dispatcherProvider.io) {
        preferencesDataSource.toggleFavoriteApp(packageName)
    }

    override suspend fun getCustomOrder(): List<String> = withContext(dispatcherProvider.io) {
        preferencesDataSource.getCustomOrder()
    }

    override suspend fun saveCustomOrder(order: List<String>) = withContext(dispatcherProvider.io) {
        preferencesDataSource.saveCustomOrder(order)
    }

    override suspend fun getCardStyle(): com.launcher.samiboxtv.domain.model.AppCardStyle = withContext(dispatcherProvider.io) {
        com.launcher.samiboxtv.domain.model.AppCardStyle.fromName(preferencesDataSource.getCardStyle())
    }

    override suspend fun setCardStyle(style: com.launcher.samiboxtv.domain.model.AppCardStyle) = withContext(dispatcherProvider.io) {
        preferencesDataSource.setCardStyle(style.name)
    }

    override suspend fun getCustomCategories(): List<String> = withContext(dispatcherProvider.io) {
        preferencesDataSource.getCustomCategories()
    }

    override suspend fun addCustomCategory(categoryName: String): Boolean = withContext(dispatcherProvider.io) {
        preferencesDataSource.addCustomCategory(categoryName)
    }

    override suspend fun removeCustomCategory(categoryName: String): Boolean = withContext(dispatcherProvider.io) {
        preferencesDataSource.removeCustomCategory(categoryName)
    }

    override suspend fun getAppCategoryMap(): Map<String, String> = withContext(dispatcherProvider.io) {
        preferencesDataSource.getAppCategoryMap()
    }

    override suspend fun setAppCategory(packageName: String, categoryName: String) = withContext(dispatcherProvider.io) {
        preferencesDataSource.setAppCategory(packageName, categoryName)
    }

    override suspend fun getShowAppNames(): Boolean = withContext(dispatcherProvider.io) {
        preferencesDataSource.getShowAppNames()
    }

    override suspend fun setShowAppNames(show: Boolean) = withContext(dispatcherProvider.io) {
        preferencesDataSource.setShowAppNames(show)
    }

    override suspend fun getFavoriteIptvChannels(): Set<String> = withContext(dispatcherProvider.io) {
        preferencesDataSource.getFavoriteIptvChannels()
    }

    override suspend fun saveFavoriteIptvChannels(channels: Set<String>) = withContext(dispatcherProvider.io) {
        preferencesDataSource.saveFavoriteIptvChannels(channels)
    }

    override suspend fun getAppLayoutMode(): AppLayoutMode = withContext(dispatcherProvider.io) {
        AppLayoutMode.fromName(preferencesDataSource.getAppLayoutMode())
    }

    override suspend fun setAppLayoutMode(mode: AppLayoutMode) = withContext(dispatcherProvider.io) {
        preferencesDataSource.setAppLayoutMode(mode.name)
    }
}
