package com.launcher.samiboxtv.domain.repository

import com.launcher.samiboxtv.domain.model.MediaFile
import com.launcher.samiboxtv.domain.model.MediaType
import com.launcher.samiboxtv.domain.model.StorageDrive

/**
 * Contrato de repositorio para detección de almacenamiento USB/local y lectura de archivos multimedia.
 */
interface MediaStorageRepository {
    suspend fun getStorageDrives(): List<StorageDrive>
    suspend fun getMediaFiles(drive: StorageDrive, filterType: MediaType? = null): List<MediaFile>
}
