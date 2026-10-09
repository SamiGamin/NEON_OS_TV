package com.launcher.samiboxtv.util.adb

import android.content.Context
import android.content.Intent
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

    private fun getOrCreateKeyPair(): AdbKeyPair {
        if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
            DevLogManager.log("ADB", "Generando nuevo par de llaves RSA para autenticación")
            AdbKeyPair.generate(privateKeyFile, publicKeyFile) // Guarda las llaves en disco (retorna Unit)
        } else {
            DevLogManager.log("ADB", "Cargando llaves RSA existentes de almacenamiento local")
        }
        // Lee y retorna el objeto AdbKeyPair cargado en memoria
        return AdbKeyPair.read(privateKeyFile, publicKeyFile)
    }

    fun isAdbDebuggingEnabled(): Boolean {
        val enabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        DevLogManager.log("ADB", "Estado de depuración global en el sistema: ${if (enabled) "ACTIVA" else "APAGADA"}")
        return enabled
    }

    /**
     * Prueba si el puerto 5555 está escuchando antes de intentar el protocolo ADB
     */
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
        DevLogManager.log("ADB", "Iniciando intento de ejecución: '$command'")

        // Diagnóstico 1: Comprobar el socket
        DevLogManager.log("ADB", "Verificando disponibilidad de 127.0.0.1:5555...")
        if (!isPortOpen("127.0.0.1", 5555)) {
            DevLogManager.log("ADB", "FALLO: El puerto 5555 está cerrado. adbd no está escuchando por TCP.")
            return@withContext Result.failure(
                Exception("Puerto 5555 cerrado. En Opciones de desarrollador activa 'Depuración de red'.")
            )
        }
        DevLogManager.log("ADB", "Puerto 5555 abierto. Estableciendo handshake con Dadb...")

        try {
            val keyPair = getOrCreateKeyPair()
            Dadb.create("127.0.0.1", 5555, keyPair).use { dadb ->
                DevLogManager.log("ADB", "Conexión autenticada con éxito. Ejecutando comando shell...")
                val response = dadb.shell(command)
                DevLogManager.log("ADB", "Respuesta recibida: [exitCode=${response.exitCode}] output=${response.output.trim()}")

                if (response.exitCode == 0) {
                    Result.success(response.output)
                } else {
                    Result.failure(Exception("ADB (${response.exitCode}): ${response.output.trim()}"))
                }
            }
        } catch (e: ConnectException) {
            DevLogManager.log("ADB", "ConnectException: ${e.message}")
            Result.failure(Exception("Conexión rechazada en 127.0.0.1:5555."))
        } catch (e: SocketTimeoutException) {
            DevLogManager.log("ADB", "Timeout: Esperando confirmación de usuario en pantalla...")
            Result.failure(Exception("Acepta el cartel '¿Permitir depuración?' que apareció en la pantalla."))
        } catch (e: Exception) {
            DevLogManager.log("ADB", "Error no controlado: ${e.javaClass.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }

    fun detectAndSaveStockLauncher(): String {
        val saved = prefs.getString("stock_launcher_pkg", null)
        if (!saved.isNullOrBlank()) return saved

        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val currentPkg = resolveInfo?.activityInfo?.packageName

        val targetPackage = if (currentPkg != null && currentPkg != context.packageName) {
            currentPkg
        } else {
            "com.google.android.tvlauncher"
        }

        prefs.edit().putString("stock_launcher_pkg", targetPackage).apply()
        DevLogManager.log("ADB", "Launcher de fábrica detectado y guardado: $targetPackage")
        return targetPackage
    }

    suspend fun disableStockLauncher(): Result<String> {
        val stockPkg = detectAndSaveStockLauncher()
        DevLogManager.log("ADB", "Intentando inhabilitar paquete stock: $stockPkg")
        return executeShell("pm disable-user --user 0 $stockPkg")
    }

    suspend fun enableStockLauncher(): Result<String> {
        val stockPkg = prefs.getString("stock_launcher_pkg", "com.google.android.tvlauncher") ?: "com.google.android.tvlauncher"
        DevLogManager.log("ADB", "Intentando restaurar paquete stock: $stockPkg")
        return executeShell("pm enable $stockPkg")
    }

    fun isStockLauncherDisabled(): Boolean {
        val stockPkg = prefs.getString("stock_launcher_pkg", null) ?: return false
        return try {
            val appInfo = context.packageManager.getApplicationInfo(stockPkg, 0)
            !appInfo.enabled
        } catch (_: Exception) {
            false
        }
    }
}