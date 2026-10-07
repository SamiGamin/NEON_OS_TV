package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.repository.PreferencesRepository

/**
 * Caso de uso para establecer un conjunto de paquetes ocultos de forma masiva (Ocultar Todo / Mostrar Todo).
 */
class SetHiddenPackagesUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(packageNames: Set<String>) {
        preferencesRepository.setHiddenPackages(packageNames)
    }
}
