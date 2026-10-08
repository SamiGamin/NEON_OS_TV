package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.repository.PreferencesRepository

/**
 * Caso de uso para reordenar las aplicaciones visibles dentro de su fila contextual (Favoritos o Categoría).
 */
class MoveAppUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(
        currentList: List<AppItem>,
        appToMove: AppItem,
        direction: Int,
        categoryMap: Map<String, String>? = null
    ): List<AppItem> {
        if (direction == 0 || currentList.isEmpty()) return currentList

        val catMap = categoryMap ?: preferencesRepository.getAppCategoryMap()

        // 1. Filtrar la sublista de apps de la misma sección que el usuario está viendo
        val sectionApps = if (appToMove.isFavorite) {
            currentList.filter { it.isFavorite }
        } else {
            val appCat = catMap[appToMove.packageName] ?: appToMove.category.ifBlank { "APPS" }
            currentList.filter { !it.isFavorite && (catMap[it.packageName] ?: it.category.ifBlank { "APPS" }).equals(appCat, ignoreCase = true) }
        }

        val localIndex = sectionApps.indexOfFirst { it.packageName == appToMove.packageName }
        if (localIndex == -1) return currentList

        val targetLocalIndex = localIndex + direction
        if (targetLocalIndex !in sectionApps.indices) {
            // Ya se encuentra en el extremo de su sección
            return currentList
        }

        val neighborApp = sectionApps[targetLocalIndex]

        // 2. En la lista global, reubicar appToMove relativamente a neighborApp
        val mutable = currentList.toMutableList()
        val globalIndexCurrent = mutable.indexOfFirst { it.packageName == appToMove.packageName }
        if (globalIndexCurrent == -1) return currentList

        mutable.removeAt(globalIndexCurrent)
        val insertIndex = mutable.indexOfFirst { it.packageName == neighborApp.packageName }
        if (insertIndex == -1) return currentList

        if (direction > 0) {
            mutable.add(insertIndex + 1, appToMove)
        } else {
            mutable.add(insertIndex, appToMove)
        }

        val updatedOrder = mutable.map { it.packageName }
        preferencesRepository.saveCustomOrder(updatedOrder)
        return mutable.mapIndexed { idx, item -> item.copy(orderIndex = idx) }
    }

    suspend fun saveOrder(order: List<String>) {
        preferencesRepository.saveCustomOrder(order)
    }
}
