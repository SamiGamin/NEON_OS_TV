package com.launcher.samiboxtv.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.domain.model.MediaFile
import com.launcher.samiboxtv.domain.model.MediaType
import com.launcher.samiboxtv.domain.model.StorageDrive
import com.launcher.samiboxtv.domain.repository.MediaStorageRepository
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Implementación de [MediaStorageRepository] optimizada para Android TV.
 * Detecta unidades internas y USBs montadas, y escanea archivos sin sobrecargar memoria.
 */
class MediaStorageRepositoryImpl(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) : MediaStorageRepository {

    private val videoExtensions = setOf("mp4", "mkv", "avi", "ts", "mov", "wmv", "m4v", "webm", "3gp")
    private val audioExtensions = setOf("mp3", "flac", "wav", "m4a", "aac", "ogg", "opus", "wma")

    override suspend fun getStorageDrives(): List<StorageDrive> = withContext(dispatcherProvider.io) {
        val drives = mutableListOf<StorageDrive>()
        val seenPaths = mutableSetOf<String>()

        // 1. Almacenamiento Interno Principal
        val internalDir = Environment.getExternalStorageDirectory()
        if (internalDir != null && internalDir.exists() && internalDir.canRead()) {
            val freeGb = (internalDir.usableSpace / (1024L * 1024L * 1024L)).coerceAtLeast(0L)
            val totalGb = (internalDir.totalSpace / (1024L * 1024L * 1024L)).coerceAtLeast(1L)
            val internalDrive = StorageDrive(
                name = "MEMORIA INTERNA",
                path = internalDir,
                isUsb = false,
                freeSpaceGb = freeGb,
                totalSpaceGb = totalGb
            )
            drives.add(internalDrive)
            seenPaths.add(internalDir.canonicalPath)
        }

        // 2. Detección de Discos y Memorias USB montadas mediante ContextCompat
        val externalDirs = ContextCompat.getExternalFilesDirs(context, null)
        for (file in externalDirs) {
            if (file != null) {
                val usbRoot = findStorageRoot(file)
                if (usbRoot != null && usbRoot.exists() && usbRoot.canRead()) {
                    val canonical = usbRoot.canonicalPath
                    if (!seenPaths.contains(canonical) && !isInternalEmulated(usbRoot)) {
                        val freeGb = (usbRoot.usableSpace / (1024L * 1024L * 1024L)).coerceAtLeast(0L)
                        val totalGb = (usbRoot.totalSpace / (1024L * 1024L * 1024L)).coerceAtLeast(1L)
                        val name = "DISCO USB (${usbRoot.name})"
                        drives.add(
                            StorageDrive(
                                name = name,
                                path = usbRoot,
                                isUsb = true,
                                freeSpaceGb = freeGb,
                                totalSpaceGb = totalGb
                            )
                        )
                        seenPaths.add(canonical)
                    }
                }
            }
        }

        // 3. Fallback adicional para Android TV escaneando /storage directamente
        val storageBase = File("/storage")
        if (storageBase.exists() && storageBase.isDirectory) {
            val children = storageBase.listFiles() ?: emptyArray()
            for (child in children) {
                if (child.isDirectory && !child.name.equals("emulated", ignoreCase = true) &&
                    !child.name.equals("self", ignoreCase = true) && !child.name.startsWith(".")
                ) {
                    val canonical = child.canonicalPath
                    if (!seenPaths.contains(canonical) && child.canRead()) {
                        val freeGb = (child.usableSpace / (1024L * 1024L * 1024L)).coerceAtLeast(0L)
                        val totalGb = (child.totalSpace / (1024L * 1024L * 1024L)).coerceAtLeast(1L)
                        drives.add(
                            StorageDrive(
                                name = "DISCO USB (${child.name})",
                                path = child,
                                isUsb = true,
                                freeSpaceGb = freeGb,
                                totalSpaceGb = totalGb
                            )
                        )
                        seenPaths.add(canonical)
                    }
                }
            }
        }

        drives
    }

    override suspend fun getMediaFiles(
        drive: StorageDrive,
        filterType: MediaType?
    ): List<MediaFile> = withContext(dispatcherProvider.io) {
        val results = mutableListOf<MediaFile>()
        val root = drive.path

        if (!root.exists() || !root.canRead()) {
            return@withContext emptyList()
        }

        // Si es memoria interna, escaneamos directorios típicos primero
        if (!drive.isUsb) {
            val standardFolders = listOf(
                File(root, Environment.DIRECTORY_MOVIES),
                File(root, Environment.DIRECTORY_DOWNLOADS),
                File(root, Environment.DIRECTORY_DCIM),
                File(root, Environment.DIRECTORY_MUSIC),
                File(root, Environment.DIRECTORY_PODCASTS),
                File(root, "Videos"),
                File(root, "Music")
            )

            for (folder in standardFolders) {
                if (folder.exists() && folder.isDirectory) {
                    scanDirectory(folder, filterType, results, maxDepth = 4, currentDepth = 0)
                }
            }
            // También escaneamos archivos sueltos en la raíz interna
            root.listFiles()?.filter { it.isFile }?.forEach { file ->
                classifyAndAdd(file, filterType, results)
            }
        } else {
            // Si es disco USB, escaneamos recursivamente desde la raíz del USB
            scanDirectory(root, filterType, results, maxDepth = 6, currentDepth = 0)
        }

        // Ordenar por fecha de modificación más reciente
        results.sortedByDescending { it.dateModified }
    }

    private fun scanDirectory(
        directory: File,
        filterType: MediaType?,
        results: MutableList<MediaFile>,
        maxDepth: Int,
        currentDepth: Int
    ) {
        if (currentDepth > maxDepth || !directory.canRead()) return

        val files = directory.listFiles() ?: return
        for (file in files) {
            if (file.name.startsWith(".") || file.name.equals("Android", ignoreCase = true)) {
                continue
            }
            if (file.isDirectory) {
                scanDirectory(file, filterType, results, maxDepth, currentDepth + 1)
            } else if (file.isFile) {
                classifyAndAdd(file, filterType, results)
            }
        }
    }

    private fun classifyAndAdd(
        file: File,
        filterType: MediaType?,
        results: MutableList<MediaFile>
    ) {
        val ext = file.extension.lowercase(Locale.ROOT)
        val isVideo = videoExtensions.contains(ext)
        val isAudio = audioExtensions.contains(ext)

        if (!isVideo && !isAudio) return

        val mediaType = if (isVideo) MediaType.VIDEO else MediaType.AUDIO
        if (filterType != null && filterType != mediaType) return

        val sizeMb = (file.length() / (1024L * 1024L)).coerceAtLeast(0L)
        results.add(
            MediaFile(
                name = file.name,
                path = file.absolutePath,
                uri = Uri.fromFile(file),
                sizeMb = sizeMb,
                type = mediaType,
                durationMs = null,
                dateModified = file.lastModified()
            )
        )
    }

    private fun findStorageRoot(file: File): File? {
        val path = file.absolutePath
        val storagePrefix = "/storage/"
        val index = path.indexOf(storagePrefix)
        if (index != -1) {
            val after = path.substring(index + storagePrefix.length)
            val slashIndex = after.indexOf('/')
            val rootName = if (slashIndex != -1) after.substring(0, slashIndex) else after
            if (rootName.isNotBlank() && !rootName.equals("emulated", ignoreCase = true)) {
                return File("/storage/$rootName")
            }
        }
        return null
    }

    private fun isInternalEmulated(file: File): Boolean {
        val path = file.absolutePath
        return path.contains("/storage/emulated", ignoreCase = true) ||
                path.contains("/data/user", ignoreCase = true)
    }
}
