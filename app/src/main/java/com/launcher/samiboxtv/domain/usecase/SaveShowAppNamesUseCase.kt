package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.repository.PreferencesRepository

/**
 * Caso de uso para guardar la preferencia de visualización de nombres/etiquetas de aplicaciones en el Home.
 */
class SaveShowAppNamesUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(show: Boolean) {
        preferencesRepository.setShowAppNames(show)
    }
}
