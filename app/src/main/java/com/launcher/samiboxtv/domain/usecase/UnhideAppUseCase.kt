package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.repository.PreferencesRepository

/**
 * Caso de uso para restaurar/desocultar una aplicación en el Launcher.
 */
class UnhideAppUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(packageName: String) {
        preferencesRepository.unhidePackage(packageName)
    }
}
