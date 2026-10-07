package com.launcher.samiboxtv.services.monitor

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Servicio de diagnóstico y monitoreo de uso de memoria RAM para pruebas de rendimiento en TV Boxes.
 */
class MemoryUsageTester(context: Context) {
    private val tag = "MemoryUsageTester"
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var job: Job? = null

    fun startMonitoring(intervalMillis: Long = 10000) {
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                logMemoryUsage()
                delay(intervalMillis)
            }
        }
    }

    fun stopMonitoring() {
        job?.cancel()
        job = null
    }

    private fun logMemoryUsage() {
        Log.d(tag, "--- Memory Usage Report ---")

        // 1. Información General de Memoria
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        Log.d(tag, "Total RAM: ${memoryInfo.totalMem / (1024 * 1024)} MB")
        Log.d(tag, "Available RAM: ${memoryInfo.availMem / (1024 * 1024)} MB")
        Log.d(tag, "Low Memory Threshold: ${memoryInfo.threshold / (1024 * 1024)} MB")
        Log.d(tag, "Is Low Memory: ${memoryInfo.lowMemory}")

        // 2. Procesos en ejecución y su consumo
        val runningProcesses = activityManager.runningAppProcesses
        if (runningProcesses != null) {
            for (processInfo in runningProcesses) {
                val pids = intArrayOf(processInfo.pid)
                val processMemoryInfo = activityManager.getProcessMemoryInfo(pids)

                for (info in processMemoryInfo) {
                    val totalPss = info.totalPss / 1024 // En MB
                    Log.d(tag, "Process: ${processInfo.processName} (PID: ${processInfo.pid}) - RAM Usage: $totalPss MB")
                }
            }
        } else {
            Log.d(tag, "No se pudieron obtener los procesos en ejecución")
        }

        @Suppress("DEPRECATION")
        val runningServices = activityManager.getRunningServices(100)
        for (service in runningServices) {
            Log.d(tag, "Running Service: ${service.service.className} (Package: ${service.service.packageName})")
        }

        Log.d(tag, "---------------------------")
    }
}
