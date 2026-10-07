package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.repository.PreferencesRepository

/**
 * Caso de uso para ocultar una aplicación del Launcher.
 */
class HideAppUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(packageName: String) {
        preferencesRepository.hidePackage(packageName)
    }
}
