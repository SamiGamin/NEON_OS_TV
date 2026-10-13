package com.launcher.samiboxtv.util.adb

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import com.launcher.samiboxtv.util.DevLogManager
import dadb.AdbKeyPair
import dadb.Dadb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

class AdbLauncherManager(private val context: Context) {

    private val privateKeyFile = File(context.filesDir, "adb_key")
    private val publicKeyFile = File(context.filesDir, "adb_key.pub")
    private val prefs = context.getSharedPreferences("adb_launcher_prefs", Context.MODE_PRIVATE)

    // Lista de paquetes de launchers de fábrica (Google TV, Android TV y cajas genéricas)
    private val knownStockLaunchers = listOf(
        "com.google.android.apps.tv.launcherx", // Google TV (TCL, Sony, Chromecast, Hisense)
        "com.google.android.tvlauncher",        // Android TV estándar
        "com.google.android.leanbacklauncher",  // Android TV AOSP / Mi Box
        "com.droidlogic.tvlauncher",            // TV Box Amlogic
        "com.android.launcher3"                 // AOSP TV Launcher genérico
    )

    private fun getOrCreateKeyPair(): AdbKeyPair {
        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            DevLogManager.log("ADB", "Generando nuevo par de llaves RSA para autenticación")
            AdbKeyPair.generate(privateKeyFile, publicKeyFile)
        }
        return AdbKeyPair.read(privateKeyFile, publicKeyFile)
    }

    fun isAdbDebuggingEnabled(): Boolean {
        return Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
    }

    private fun isPortOpen(host: String = "127.0.0.1", port: Int = 5555, timeoutMs: Int = 1200): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun executeShell(command: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isPortOpen("127.0.0.1", 5555)) {
            return@withContext Result.failure(
                Exception("Puerto 5555 cerrado. En Opciones de desarrollador activa 'Depuración por red/USB'.")
            )
        }

        try {
            val keyPair = getOrCreateKeyPair()
            Dadb.create("127.0.0.1", 5555, keyPair).use { dadb ->
                val response = dadb.shell(command)
                DevLogManager.log("ADB", "Cmd: '$command' -> [code=${response.exitCode}] ${response.output.trim()}")

                if (response.exitCode == 0) {
                    Result.success(response.output.trim())
                } else {
                    Result.failure(Exception("ADB (${response.exitCode}): ${response.output.trim()}"))
                }
            }
        } catch (e: ConnectException) {
            Result.failure(Exception("Conexión rechazada en 127.0.0.1:5555."))
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Acepta el cartel '¿Permitir depuración?' en la pantalla de la TV."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca cuáles de los launchers de fábrica existen realmente en este televisor
     */
    private fun findInstalledStockLaunchers(): List<String> {
        val installed = mutableListOf<String>()
        for (pkg in knownStockLaunchers) {
            try {
                context.packageManager.getPackageInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS)
                installed.add(pkg)
            } catch (_: Exception) {}
        }
        return installed
    }

    suspend fun disableStockLauncher(): Result<String> = withContext(Dispatchers.IO) {
        val targets = findInstalledStockLaunchers().ifEmpty { knownStockLaunchers }
        var disabledAny = false
        var lastError = ""

        // 1. Fijar NEON OS TV como actividad Home principal del sistema
        executeShell("cmd package set-home-activity ${context.packageName}/.MainActivity")

        // 2. Desactivar el launcher nativo instalado (ej. launcherx en Google TV)
        for (pkg in targets) {
            val res = executeShell("pm disable-user --user 0 $pkg")
            if (res.isSuccess) {
                disabledAny = true
                prefs.edit().putString("stock_launcher_pkg", pkg).apply()
            } else {
                lastError = res.exceptionOrNull()?.message ?: ""
            }
        }

        if (disabledAny) {
            Result.success("Launcher de fábrica desactivado")
        } else {
            Result.failure(Exception(lastError.ifBlank { "No se encontró el paquete del launcher nativo" }))
        }
    }

    suspend fun enableStockLauncher(): Result<String> = withContext(Dispatchers.IO) {
        val targets = findInstalledStockLaunchers().ifEmpty { knownStockLaunchers }
        var enabledAny = false
        var lastError = ""

        for (pkg in targets) {
            val res = executeShell("pm enable $pkg")
            if (res.isSuccess) {
                enabledAny = true
            } else {
                lastError = res.exceptionOrNull()?.message ?: ""
            }
        }

        if (enabledAny) {
            Result.success("Launcher original restaurado")
        } else {
            Result.failure(Exception(lastError.ifBlank { "No se pudo restaurar el launcher" }))
        }
    }

    fun isStockLauncherDisabled(): Boolean {
        val installed = findInstalledStockLaunchers()
        if (installed.isEmpty()) return false

        return installed.any { pkg ->
            try {
                val appInfo = context.packageManager.getApplicationInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS)
                !appInfo.enabled
            } catch (_: Exception) {
                false
            }
        }
    }
}