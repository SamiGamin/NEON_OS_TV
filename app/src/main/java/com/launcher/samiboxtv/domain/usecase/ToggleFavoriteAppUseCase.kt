package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.repository.PreferencesRepository

/**
 * Caso de uso para alternar si una aplicación es destacada (favorita).
 */
class ToggleFavoriteAppUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(packageName: String): Boolean {
        return preferencesRepository.toggleFavoritePackage(packageName)
    }
}
