package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.repository.PreferencesRepository

class ManageCategoriesUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend fun addCategory(name: String): Boolean {
        return preferencesRepository.addCustomCategory(name)
    }

    suspend fun removeCategory(name: String): Boolean {
        return preferencesRepository.removeCustomCategory(name)
    }

    suspend fun assignAppToCategory(packageName: String, categoryName: String) {
        preferencesRepository.setAppCategory(packageName, categoryName)
    }
}
