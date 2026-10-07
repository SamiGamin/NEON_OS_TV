package com.launcher.samiboxtv.domain.repository

import com.launcher.samiboxtv.domain.model.UpdateInfo

/**
 * Contrato de repositorio para consultar nuevas versiones en GitHub Releases.
 */
interface UpdateRepository {
    suspend fun checkForUpdates(): Result<UpdateInfo>
}
