package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.MediaFile
import com.launcher.samiboxtv.domain.model.MediaType
import com.launcher.samiboxtv.domain.model.StorageDrive
import com.launcher.samiboxtv.domain.repository.MediaStorageRepository

/**
 * Caso de uso para obtener los archivos multimedia de una unidad de almacenamiento dada.
 */
class GetMediaFilesUseCase(
    private val repository: MediaStorageRepository
) {
    suspend operator fun invoke(
        drive: StorageDrive,
        filterType: MediaType? = null
    ): List<MediaFile> = repository.getMediaFiles(drive, filterType)
}
