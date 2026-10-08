package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.StorageDrive
import com.launcher.samiboxtv.domain.repository.MediaStorageRepository

/**
 * Caso de uso para obtener las unidades de almacenamiento disponibles (Interna y USBs).
 */
class GetStorageDrivesUseCase(
    private val repository: MediaStorageRepository
) {
    suspend operator fun invoke(): List<StorageDrive> = repository.getStorageDrives()
}
