package com.launcher.samiboxtv.data.datasource

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import com.launcher.samiboxtv.domain.model.CleanRamResult
import com.launcher.samiboxtv.domain.model.ProcessInfo
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
    fun getRunningProcesses(): List<ProcessInfo>
    fun cleanBackgroundProcesses(): CleanRamResult
    fun killProcess(packageName: String): Boolean
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

    override fun getRunningProcesses(): List<ProcessInfo> {
        val am = activityManager ?: return emptyList()
        val pm = context.packageManager
        val running = am.runningAppProcesses ?: emptyList()
        val result = mutableListOf<ProcessInfo>()

        val pids = running.map { it.pid }.toIntArray()
        val memInfos = try {
            if (pids.isNotEmpty()) am.getProcessMemoryInfo(pids) else emptyArray()
        } catch (_: Exception) {
            emptyArray()
        }

        running.forEachIndexed { index, proc ->
            val pid = proc.pid
            val pName = proc.processName
            val primaryPkg = proc.pkgList?.firstOrNull() ?: pName

            var appName = pName
            var isSystem = false
            var icon: Drawable? = null

            try {
                val appInfo = pm.getApplicationInfo(primaryPkg, 0)
                appName = pm.getApplicationLabel(appInfo).toString()
                isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                icon = pm.getApplicationIcon(appInfo)
            } catch (_: Exception) {}

            val memMb = if (index < memInfos.size) {
                (memInfos[index].totalPss / 1024L).coerceAtLeast(1L)
            } else {
                1L
            }

            result.add(
                ProcessInfo(
                    pid = pid,
                    processName = pName,
                    appName = appName,
                    packageName = primaryPkg,
                    memoryUsageMb = memMb,
                    isSystemApp = isSystem,
                    icon = icon
                )
            )
        }

        // Si la lista de procesos activos no detecta apps debido a restricciones de nivel de API,
        // complementamos con apps de terceros instaladas
        if (result.isEmpty()) {
            try {
                val installed = pm.getInstalledApplications(0)
                for (app in installed) {
                    if ((app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && app.packageName != context.packageName) {
                        val appName = pm.getApplicationLabel(app).toString()
                        val icon = pm.getApplicationIcon(app)
                        result.add(
                            ProcessInfo(
                                pid = 0,
                                processName = app.processName ?: app.packageName,
                                appName = appName,
                                packageName = app.packageName,
                                memoryUsageMb = 24L,
                                isSystemApp = false,
                                icon = icon
                            )
                        )
                    }
                }
            } catch (_: Exception) {}
        }

        return result.sortedWith(compareBy({ it.isSystemApp }, { -it.memoryUsageMb }))
    }

    override fun cleanBackgroundProcesses(): CleanRamResult {
        val am = activityManager ?: return CleanRamResult(0, 0, 0, 0)
        val pm = context.packageManager

        val memBefore = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memBefore)
        val initialAvailMb = memBefore.availMem / (1024L * 1024L)

        val myPkg = context.packageName
        val packagesToKill = mutableSetOf<String>()

        val running = am.runningAppProcesses ?: emptyList()
        running.forEach { proc ->
            proc.pkgList?.forEach { pkg ->
                if (pkg != myPkg) {
                    try {
                        val appInfo = pm.getApplicationInfo(pkg, 0)
                        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        if (!isSystem) {
                            packagesToKill.add(pkg)
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        // Adicionalmente agregamos aplicaciones de usuario que puedan tener procesos en segundo plano
        try {
            val installed = pm.getInstalledApplications(0)
            for (app in installed) {
                if ((app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && app.packageName != myPkg) {
                    packagesToKill.add(app.packageName)
                }
            }
        } catch (_: Exception) {}

        var killedCount = 0
        packagesToKill.forEach { pkg ->
            try {
                am.killBackgroundProcesses(pkg)
                killedCount++
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Forzar recolección de basura
        System.gc()
        Runtime.getRuntime().gc()

        val memAfter = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memAfter)
        val finalAvailMb = memAfter.availMem / (1024L * 1024L)
        val freedMb = (finalAvailMb - initialAvailMb).coerceAtLeast(0L)

        return CleanRamResult(
            killedProcessesCount = killedCount,
            freedMemoryMb = freedMb,
            initialAvailableMb = initialAvailMb,
            finalAvailableMb = finalAvailMb
        )
    }

    override fun killProcess(packageName: String): Boolean {
        return try {
            activityManager?.killBackgroundProcesses(packageName)
            true
        } catch (_: Exception) {
            false
        }
    }
}
