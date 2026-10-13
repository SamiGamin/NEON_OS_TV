package com.launcher.samiboxtv.util.adb

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import com.launcher.samiboxtv.MainActivity
import com.launcher.samiboxtv.services.accessibility.SamiBoxAccessibilityService
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

    private val googleTvPackage = "com.google.android.apps.tv.launcherx"

    private val genericStockLaunchers = listOf(
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

    fun isGoogleTv(): Boolean {
        return try {
            context.packageManager.getPackageInfo(googleTvPackage, PackageManager.MATCH_DISABLED_COMPONENTS)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun disableStockLauncher(): Result<String> = withContext(Dispatchers.IO) {
        // 1. Fijar NEON OS TV como actividad Home principal del sistema por ADB
        executeShell("cmd package set-home-activity ${context.packageName}/${MainActivity::class.java.name}")

        // 2. Comprobar si es Google TV (TCL, Sony, Chromecast, Hisense)
        if (isGoogleTv()) {
            // ¡EN GOOGLE TV DESHABILITAR LAUNCHERX CAUSA PANTALLA NEGRA Y CONGELAMIENTO!
            // Para evitar la pantalla negra, nos aseguramos de que launcherx esté HABILITADO:
            executeShell("pm enable $googleTvPackage")

            // Y activamos el Servicio de Accesibilidad de alta prioridad automáticamente por ADB:
            val serviceName = "${context.packageName}/${SamiBoxAccessibilityService::class.java.name}"
            executeShell("settings put secure enabled_accessibility_services $serviceName")
            executeShell("settings put secure accessibility_enabled 1")

            prefs.edit().putBoolean("google_tv_mode", true).apply()
            return@withContext Result.success(
                "¡Google TV fijado sin parpadeo! (Accesibilidad activada por ADB, pantalla negra prevenida)."
            )
        }

        // 3. Si es TV Box genérica o Android TV estándar (no Google TV):
        var disabledAny = false
        var lastError = ""
        for (pkg in genericStockLaunchers) {
            try {
                context.packageManager.getPackageInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS)
                val res = executeShell("pm disable-user --user 0 $pkg")
                if (res.isSuccess) {
                    disabledAny = true
                    prefs.edit().putString("stock_launcher_pkg", pkg).apply()
                } else {
                    lastError = res.exceptionOrNull()?.message ?: ""
                }
            } catch (_: Exception) {}
        }

        if (disabledAny) {
            Result.success("Launcher de fábrica desactivado por ADB")
        } else {
            Result.success("Home fijado como predeterminado con 'set-home-activity'")
        }
    }

    suspend fun enableStockLauncher(): Result<String> = withContext(Dispatchers.IO) {
        // Re-habilitar todos los launchers posibles para descongelar y restaurar la pantalla
        val allLaunchers = listOf(googleTvPackage) + genericStockLaunchers
        var restoredAny = false

        for (pkg in allLaunchers) {
            val res = executeShell("pm enable $pkg")
            if (res.isSuccess) {
                restoredAny = true
            }
        }

        prefs.edit().remove("stock_launcher_pkg").remove("google_tv_mode").apply()

        if (restoredAny) {
            Result.success("Launcher original restaurado y pantalla normalizada")
        } else {
            Result.failure(Exception("No se pudo restaurar el launcher original"))
        }
    }

    fun isStockLauncherDisabled(): Boolean {
        if (isGoogleTv()) {
            return SamiBoxAccessibilityService.isRunning()
        }
        for (pkg in genericStockLaunchers) {
            try {
                val appInfo = context.packageManager.getApplicationInfo(pkg, PackageManager.MATCH_DISABLED_COMPONENTS)
                if (!appInfo.enabled) return true
            } catch (_: Exception) {}
        }
        return false
    }
}