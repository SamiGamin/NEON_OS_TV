package com.launcher.samiboxtv.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.launcher.samiboxtv.BuildConfig
import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.domain.model.UpdateInfo
import com.launcher.samiboxtv.domain.repository.UpdateRepository
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateRepositoryImpl(
    private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) : UpdateRepository {

    override suspend fun checkForUpdates(): Result<UpdateInfo> = withContext(dispatcherProvider.io) {
        runCatching {
            val currentVersion = getCurrentVersionName()
            val owner = BuildConfig.GITHUB_OWNER.ifBlank { "SamiGamin" }
            val repo = BuildConfig.GITHUB_REPO.ifBlank { "SamiBoxTV" }
            val endpoint = "https://api.github.com/repos/$owner/$repo/releases/latest"

            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                if (BuildConfig.GITHUB_TOKEN.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer ${BuildConfig.GITHUB_TOKEN}")
                }
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                connectTimeout = 8000
                readTimeout = 8000
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorStream = connection.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
                throw IllegalStateException("GitHub API respondió con código $responseCode: $errorStream")
            }

            val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            val json = JSONObject(responseText)

            val rawTagName = json.getString("tag_name")
            val remoteVersion = rawTagName.removePrefix("v").trim()
            val releaseName = json.optString("name", "Nueva actualización v$remoteVersion")
            val body = json.optString("body", "Sin notas de versión")

            // Buscar asset .apk si existe
            var apkUrl: String? = null
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetName = asset.optString("name", "")
                    if (assetName.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url").ifBlank {
                            asset.optString("url")
                        }
                        break
                    }
                }
            }

            val hasUpdate = isRemoteNewer(currentVersion, remoteVersion)

            UpdateInfo(
                currentVersion = currentVersion,
                latestVersion = remoteVersion,
                hasUpdate = hasUpdate,
                releaseName = releaseName,
                releaseNotes = body,
                apkDownloadUrl = apkUrl
            )
        }
    }

    private fun getCurrentVersionName(): String {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    private fun isRemoteNewer(current: String, remote: String): Boolean {
        val currParts = current.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
        val remParts = remote.split(".").map { it.filter { c -> c.isDigit() }.toIntOrNull() ?: 0 }
        val length = maxOf(currParts.size, remParts.size)

        for (i in 0 until length) {
            val c = currParts.getOrElse(i) { 0 }
            val r = remParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
