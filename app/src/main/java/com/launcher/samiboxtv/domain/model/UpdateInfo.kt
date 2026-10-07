package com.launcher.samiboxtv.domain.model

/**
 * Representa la información de actualización remota de la aplicación obtenida desde GitHub Releases.
 *
 * @property currentVersion Versión instalada actualmente en el TV Box (e.g. "1.0.1").
 * @property latestVersion Versión más reciente publicada en el repositorio (e.g. "1.0.2").
 * @property hasUpdate Indica si la versión remota es superior a la instalada.
 * @property releaseName Título o nombre de la release publicada.
 * @property releaseNotes Descripción / changelog con los cambios de la versión.
 * @property apkDownloadUrl URL del archivo APK para su descarga.
 */
data class UpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val hasUpdate: Boolean,
    val releaseName: String,
    val releaseNotes: String,
    val apkDownloadUrl: String?
)
