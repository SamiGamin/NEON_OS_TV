package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.repository.PreferencesRepository

/**
 * Caso de uso para reordenar las aplicaciones visibles en la cuadrícula del Launcher.
 */
class MoveAppUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(
        currentList: List<AppItem>,
        appToMove: AppItem,
        direction: Int
    ): List<AppItem> {
        val mutable = currentList.toMutableList()
        val index = mutable.indexOfFirst { it.packageName == appToMove.packageName }
        if (index == -1) return currentList

        val newIndex = index + direction
        if (newIndex in 0 until mutable.size) {
            mutable.removeAt(index)
            mutable.add(newIndex, appToMove)

            val updatedOrder = mutable.map { it.packageName }
            preferencesRepository.saveCustomOrder(updatedOrder)
            return mutable.mapIndexed { idx, item -> item.copy(orderIndex = idx) }
        }

        return currentList
    }
}
