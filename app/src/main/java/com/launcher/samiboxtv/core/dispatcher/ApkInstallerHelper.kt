package com.launcher.samiboxtv.core.dispatcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object ApkInstallerHelper {

    /**
     * Descarga el APK siguiendo manualmente todas las redirecciones HTTP (301, 302, 303, 307, 308)
     * típicas de GitHub Releases hacia AWS S3 y reportando el progreso de 0.0f a 1.0f.
     */
    suspend fun downloadApk(
        downloadUrl: String,
        outputFile: File,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        var currentUrl = downloadUrl
        var redirectCount = 0
        val maxRedirects = 7

        try {
            if (outputFile.exists()) {
                outputFile.delete()
            }
            outputFile.parentFile?.mkdirs()

            var connection: HttpURLConnection
            while (true) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20000
                    readTimeout = 25000
                    instanceFollowRedirects = false
                    setRequestProperty("User-Agent", "SamiBoxTV-Launcher-OTA/1.0")
                    setRequestProperty("Accept", "*/*")
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                    responseCode == 307 || responseCode == 308
                ) {
                    val location = connection.getHeaderField("Location")
                        ?: throw IOException("Redirección HTTP $responseCode sin cabecera Location")
                    connection.disconnect()
                    currentUrl = location
                    redirectCount++
                    if (redirectCount > maxRedirects) {
                        throw IOException("Demasiadas redirecciones consecutivas ($redirectCount)")
                    }
                    continue
                }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    connection.disconnect()
                    throw IOException("El servidor respondió con código HTTP $responseCode")
                }
                break
            }

            val fileLength = connection.contentLengthLong.takeIf { it > 0 }
                ?: connection.contentLength.toLong()

            connection.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var total: Long = 0
                    var count: Int

                    while (input.read(buffer).also { count = it } != -1) {
                        total += count
                        if (fileLength > 0) {
                            onProgress(total.toFloat() / fileLength.toFloat())
                        }
                        output.write(buffer, 0, count)
                    }
                    output.flush()
                }
            }
            connection.disconnect()

            // Un APK válido de Android TV pesa al menos 1 MB
            outputFile.exists() && outputFile.length() > 500_000L
        } catch (e: Exception) {
            e.printStackTrace()
            if (outputFile.exists()) {
                outputFile.delete()
            }
            false
        }
    }

    /**
     * Verifica si la aplicación tiene permiso para solicitar instalaciones (Android 8.0+).
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.packageManager.canRequestPackageInstalls()
            } catch (_: Exception) {
                true
            }
        } else {
            true
        }
    }

    /**
     * Abre los ajustes del sistema para permitir orígenes desconocidos a SamiBox TV.
     */
    fun openUnknownSourcesSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                val fallback = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            }
        }
    }

    /**
     * Lanza el diálogo nativo de instalación de paquetes de Android.
     * Retorna true si el instalador fue lanzado con éxito, o false si se requiere conceder permisos
     * o si el APK no es válido.
     */
    fun launchInstallApk(context: Context, apkFile: File): Boolean {
        return try {
            if (!apkFile.exists() || apkFile.length() < 100_000L) {
                return false
            }

            // Asegurar permisos de lectura en el sistema de archivos
            apkFile.setReadable(true, false)

            // 1. En Android 8.0+, validar si el usuario autorizó fuentes desconocidas
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !canRequestPackageInstalls(context)) {
                openUnknownSourcesSettings(context)
                return false
            }

            // 2. Obtener Content URI vía FileProvider
            val authority = "${context.packageName}.provider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)

            // 3. Preparar Intent de instalación nativa
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            }

            // 4. Conceder permisos explícitos de URI a los instaladores del sistema
            val resolveInfos = context.packageManager.queryIntentActivities(
                installIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            for (resolve in resolveInfos) {
                val pkg = resolve.activityInfo.packageName
                context.grantUriPermission(pkg, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}