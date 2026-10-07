package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.UpdateInfo
import com.launcher.samiboxtv.domain.repository.UpdateRepository

/**
 * Caso de uso para verificar si existe una nueva versión del Launcher en GitHub Releases.
 */
class CheckUpdateUseCase(
    private val repository: UpdateRepository
) {
    suspend operator fun invoke(): Result<UpdateInfo> = repository.checkForUpdates()
}
