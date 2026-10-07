package com.launcher.samiboxtv.presentation.overlay

import android.app.ActivityManager
import android.content.Context
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.view.Choreographer
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Composable
fun SystemInfoOverlay(visible: Boolean) {
    val context = LocalContext.current
    val techFont = ShareTechMonoFontFamily

    var ramUsed by remember { mutableStateOf(0L) }
    var ramTotal by remember { mutableStateOf(1L) }
    var storUsed by remember { mutableStateOf(0L) }
    var storTotal by remember { mutableStateOf(1L) }
    var storExt by remember { mutableStateOf("...") }
    var downKb by remember { mutableStateOf(0L) }
    var upKb by remember { mutableStateOf(0L) }
    var cpuTemp by remember { mutableStateOf("...") }
    var cpuPct by remember { mutableStateOf("...") }
    var ipAddr by remember { mutableStateOf("...") }
    var uptime by remember { mutableStateOf("...") }
    var refreshHz by remember { mutableStateOf("...") }
    var fps by remember { mutableStateOf(0) }
    var prevRx by remember { mutableStateOf(0L) }
    var prevTx by remember { mutableStateOf(0L) }

    val frameCount = remember { AtomicInteger(0) }
    val lastFpsTime = remember { AtomicLong(System.nanoTime()) }

    DisposableEffect(visible) {
        if (!visible) return@DisposableEffect onDispose {}
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount.incrementAndGet()
                val now = System.nanoTime()
                val elapsed = now - lastFpsTime.get()
                if (elapsed >= 1_000_000_000L) {
                    fps = (frameCount.getAndSet(0) * 1_000_000_000L / elapsed).toInt()
                    lastFpsTime.set(now)
                }
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(callback)
        onDispose { Choreographer.getInstance().removeFrameCallback(callback) }
    }

    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        refreshHz = getRefreshRate(context)
        while (true) {
            val (ru, rt) = getRamInfo(context)
            ramUsed = ru
            ramTotal = rt

            val (su, st) = getStorageInternal()
            storUsed = su
            storTotal = st
            storExt = getStorageExternal()

            val (dn, up, newBytes) = getNetworkSpeed(prevRx, prevTx)
            downKb = dn
            upKb = up
            prevRx = newBytes.first
            prevTx = newBytes.second

            cpuTemp = readCpuTemp()
            cpuPct = readCpuUsageDelta()
            ipAddr = getIpAddress(context)
            uptime = getUptime()
            delay(1500)
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 30.dp, end = 30.dp)
                    .background(Color(0xEE0B0D13), RoundedCornerShape(8.dp))
                    .border(1.5.dp, CyberCyan, RoundedCornerShape(8.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "> SYSTEM MONITOR",
                    color = CyberCyan,
                    fontFamily = techFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))

                val fpsColor = when {
                    fps < 24 -> CyberMagenta
                    fps < 50 -> CyberAmber
                    else -> CyberCyan
                }
                OverlayRow("FPS", "$fps fps", fpsColor, techFont)
                OverlayRow("CPU", cpuPct, CyberAmber, techFont)

                val ramPct = if (ramTotal > 0) (ramUsed * 100 / ramTotal).toInt() else 0
                OverlayRow(
                    label = "RAM",
                    value = "$ramUsed/$ramTotal MB ($ramPct%)",
                    valueColor = when {
                        ramPct > 85 -> CyberMagenta
                        ramPct > 65 -> CyberAmber
                        else -> CyberCyan
                    },
                    font = techFont
                )

                OverlayRow(
                    label = "TEMP",
                    value = cpuTemp,
                    valueColor = if (cpuTemp.startsWith("N")) Color.Gray else CyberAmber,
                    font = techFont
                )

                OverlayRow("SCREEN", refreshHz, Color.LightGray, techFont)
                OverlayRow("INT", "$storUsed/$storTotal GB", CyberCyan, techFont)
                OverlayRow("EXT", storExt, Color.LightGray, techFont)
                OverlayRow("IP", ipAddr, CyberCyan, techFont)
                OverlayRow("DOWN", "$downKb KB/s", CyberCyan, techFont)
                OverlayRow("UP", "$upKb KB/s", CyberAmber, techFont)
                OverlayRow("UPTIME", uptime, Color.LightGray, techFont)

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "pulsa MENU para abrir/cerrar",
                    color = CyberGrey,
                    fontFamily = techFont,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun OverlayRow(label: String, value: String, valueColor: Color, font: FontFamily) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "$label:",
            color = CyberGrey,
            fontFamily = font,
            fontSize = 12.sp,
            modifier = Modifier.width(64.dp)
        )
        Text(
            text = value,
            color = valueColor,
            fontFamily = font,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun getRamInfo(context: Context): Pair<Long, Long> {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val info = ActivityManager.MemoryInfo()
    am.getMemoryInfo(info)
    val usedMb = (info.totalMem - info.availMem) / (1024 * 1024)
    val totalMb = info.totalMem / (1024 * 1024)
    return Pair(usedMb, totalMb)
}

private fun getStorageInternal(): Pair<Long, Long> {
    val stat = StatFs(Environment.getDataDirectory().path)
    val total = stat.totalBytes / (1024 * 1024 * 1024)
    val free = stat.availableBytes / (1024 * 1024 * 1024)
    return Pair(total - free, total)
}

private fun getStorageExternal(): String {
    return try {
        val ext = Environment.getExternalStorageDirectory()
        val stat = StatFs(ext.path)
        val free = stat.availableBytes / (1024 * 1024)
        val total = stat.totalBytes / (1024 * 1024)
        "${total - free} MB / $total MB"
    } catch (_: Exception) {
        "No disponible"
    }
}

private fun getNetworkSpeed(prevRx: Long, prevTx: Long): Triple<Long, Long, Pair<Long, Long>> {
    val rx = TrafficStats.getTotalRxBytes()
    val tx = TrafficStats.getTotalTxBytes()
    val downKb = if (prevRx > 0 && rx > prevRx) (rx - prevRx) / 1024 else 0L
    val upKb = if (prevTx > 0 && tx > prevTx) (tx - prevTx) / 1024 else 0L
    return Triple(downKb, upKb, Pair(rx, tx))
}

private fun readCpuTemp(): String {
    return try {
        val temp = java.io.File("/sys/class/thermal/thermal_zone0/temp").readText().trim().toLong()
        val celsius = if (temp > 1000) temp / 1000 else temp
        "$celsius °C"
    } catch (_: Exception) {
        try {
            val temp = java.io.File("/sys/devices/platform/rockchip-thermal/temp1_input").readText().trim().toLong()
            "${temp / 1000} °C"
        } catch (_: Exception) {
            "N/D"
        }
    }
}

private data class CpuTicks(val idle: Long, val total: Long)

private fun readCpuTicks(): CpuTicks? {
    return try {
        val line = java.io.File("/proc/stat").bufferedReader().readLine() ?: return null
        val parts = line.trim().split(Regex("\\s+")).drop(1).map { it.toLong() }
        if (parts.size < 8) return null
        val idle = parts[3] + parts[4]
        val total = parts.sum()
        CpuTicks(idle, total)
    } catch (_: Exception) {
        null
    }
}

private var lastCpuTicks: CpuTicks? = null

private fun readCpuUsageDelta(): String {
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

@Suppress("DEPRECATION")
private fun getIpAddress(context: Context): String {
    return try {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip = wm.connectionInfo.ipAddress
        if (ip == 0) "Sin WiFi" else
            "${ip and 0xff}.${ip shr 8 and 0xff}.${ip shr 16 and 0xff}.${ip shr 24 and 0xff}"
    } catch (_: Exception) {
        "N/D"
    }
}

private fun getUptime(): String {
    val ms = SystemClock.elapsedRealtime()
    val h = ms / 3_600_000
    val m = (ms % 3_600_000) / 60_000
    return "${h}h ${m}m"
}

@Suppress("DEPRECATION")
private fun getRefreshRate(context: Context): String {
    return try {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val hz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display.refreshRate
        } else {
            wm.defaultDisplay.refreshRate
        }
        "${hz.toInt()} Hz"
    } catch (_: Exception) {
        "N/D"
    }
}
