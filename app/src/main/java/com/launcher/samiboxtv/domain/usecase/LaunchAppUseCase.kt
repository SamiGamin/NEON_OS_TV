package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.repository.AppRepository

/**
 * Caso de uso para lanzar una aplicación por su nombre de paquete.
 */
class LaunchAppUseCase(
    private val appRepository: AppRepository
) {
    operator fun invoke(packageName: String): Result<Unit> {
        return appRepository.launchApp(packageName)
    }
}
