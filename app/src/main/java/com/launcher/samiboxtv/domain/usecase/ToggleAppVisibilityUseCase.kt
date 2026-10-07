package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.repository.PreferencesRepository

/**
 * Caso de uso para alternar la visibilidad de una aplicación (Ocultar/Mostrar).
 */
class ToggleAppVisibilityUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(packageName: String): Boolean {
        return preferencesRepository.toggleHidePackage(packageName)
    }
}
