package com.launcher.samiboxtv.data.datasource

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import com.launcher.samiboxtv.domain.model.SystemTelemetry
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.io.File

/**
 * Fuente de datos para consultar el hardware, memoria RAM y métricas del sistema.
 */
interface SystemTelemetryDataSource {
    fun observeTelemetry(intervalMillis: Long = 3000): Flow<SystemTelemetry>
    fun getCurrentTelemetry(): SystemTelemetry
}

class SystemTelemetryDataSourceImpl(
    private val context: Context
) : SystemTelemetryDataSource {

    private val activityManager: ActivityManager? by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    }

    private data class CpuTicks(val idle: Long, val total: Long)
    private var lastCpuTicks: CpuTicks? = null

    override fun getCurrentTelemetry(): SystemTelemetry {
        // 1. Lectura de Memoria RAM
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val ramTotalMb = memoryInfo.totalMem / (1024L * 1024L)
        val ramAvailMb = memoryInfo.availMem / (1024L * 1024L)
        val ramUsedMb = (ramTotalMb - ramAvailMb).coerceAtLeast(0L)
        val ramUsagePct = if (ramTotalMb > 0) {
            ((ramUsedMb * 100) / ramTotalMb).toInt().coerceIn(0, 100)
        } else {
            0
        }

        // 2. Lectura de Almacenamiento Interno
        val (storUsedGb, storTotalGb, storFreeGb) = try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val total = stat.totalBytes / (1024L * 1024L * 1024L)
            val free = stat.availableBytes / (1024L * 1024L * 1024L)
            val used = (total - free).coerceAtLeast(0L)
            Triple(used, total, free)
        } catch (_: Exception) {
            Triple(0L, 0L, 0L)
        }

        // 3. Estimación de carga CPU y Temperatura
        val cpuPct = calculateCpuUsage()
        val cpuTemp = readCpuTemperature()

        // 4. Tiempo activo (Uptime)
        val uptimeStr = calculateUptime()

        // 5. Metadatos de Hardware y Android
        val model = "${Build.MANUFACTURER.uppercase()} ${Build.MODEL}"
        val androidVer = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        return SystemTelemetry(
            ramUsedMb = ramUsedMb,
            ramTotalMb = ramTotalMb,
            ramAvailableMb = ramAvailMb,
            ramUsagePercentage = ramUsagePct,
            isLowMemory = memoryInfo.lowMemory,
            storageUsedGb = storUsedGb,
            storageTotalGb = storTotalGb,
            storageFreeGb = storFreeGb,
            cpuUsagePercentage = cpuPct,
            cpuTemperature = cpuTemp,
            uptime = uptimeStr,
            deviceModel = model,
            androidVersion = androidVer
        )
    }

    override fun observeTelemetry(intervalMillis: Long): Flow<SystemTelemetry> = flow {
        while (currentCoroutineContext().isActive) {
            emit(getCurrentTelemetry())
            delay(intervalMillis)
        }
    }.distinctUntilChanged()

    private fun calculateUptime(): String {
        val ms = SystemClock.elapsedRealtime()
        val h = ms / 3_600_000
        val m = (ms % 3_600_000) / 60_000
        return "${h}h ${m}m"
    }

    private fun readCpuTemperature(): String? {
        val thermalFiles = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/devices/platform/rockchip-thermal/temp1_input",
            "/sys/class/thermal/thermal_zone1/temp"
        )
        for (path in thermalFiles) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val raw = file.readText().trim().toLongOrNull() ?: continue
                    val celsius = if (raw > 1000) raw / 1000 else raw
                    if (celsius in 10..120) {
                        return "$celsius°C"
                    }
                }
            } catch (_: Exception) {
                // Siguiente intento
            }
        }
        return null
    }

    private fun calculateCpuUsage(): String {
        val current = readCpuTicks() ?: return "N/D"
        val previous = lastCpuTicks
        lastCpuTicks = current
        if (previous == null) return "..."
        val deltaTotal = current.total - previous.total
        val deltaIdle = current.idle - previous.idle
        if (deltaTotal <= 0) return "0%"
        val pct = ((deltaTotal - deltaIdle) * 100 / deltaTotal).toInt().coerceIn(0, 100)
        return "$pct%"
    }

    private fun readCpuTicks(): CpuTicks? {
        return try {
            val file = File("/proc/stat")
            if (!file.exists()) return null
            val line = file.bufferedReader().readLine() ?: return null
            val parts = line.trim().split(Regex("\\s+")).drop(1).mapNotNull { it.toLongOrNull() }
            if (parts.size < 5) return null
            val idle = parts[3] + (parts.getOrNull(4) ?: 0L)
            val total = parts.sum()
            CpuTicks(idle, total)
        } catch (_: Exception) {
            null
        }
    }
}
