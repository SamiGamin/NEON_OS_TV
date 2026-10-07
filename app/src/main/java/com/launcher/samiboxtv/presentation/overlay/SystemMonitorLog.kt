package com.launcher.samiboxtv.presentation.overlay

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.domain.model.SystemTelemetry
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberBg
import com.launcher.samiboxtv.presentation.theme.CyberCard
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

data class ScannedApp(
    val name: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val isBloatware: Boolean
)

fun scanAllApps(context: Context): List<ScannedApp> {
    val pm = context.packageManager
    val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

    val knownBloatware = listOf(
        "com.hkw.simplelauncher",
        "com.wolf.google.lm",
        "com.luancher.apps",
        "com.hcy.remoteAceess",
        "com.hcy.remoteAceessdesk",
        "com.www.intallapp",
        "com.abupdate.fota_demo_iot",
        "com.charon.rocketfly",
        "com.rockchip.devicetest",
        "com.rockchip.mediacenter",
        "com.rockchips.mediacenter",
        "com.hcy.firstbt",
        "com.android.mgstv",
        "com.android.smart.terminal",
        "com.android.inputmethod.pinyin",
        "com.android.browser",
        "com.quick.appstore",
        "com.rockchip.weather",
        "com.speedbooster.cleaner",
        "com.rockchip.launcher",
        "com.example.weather",
        "com.allwinnertech.miracast"
    )

    val scannedList = mutableListOf<ScannedApp>()

    for (appInfo in packages) {
        val pkg = appInfo.packageName
        val appName = pm.getApplicationLabel(appInfo).toString()
        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isBloatware = knownBloatware.contains(pkg)
        scannedList.add(ScannedApp(appName, pkg, isSystemApp, isBloatware))
    }

    val sorted = scannedList.sortedWith(
        compareBy({ !it.isBloatware }, { it.isSystemApp }, { it.name.lowercase() })
    )

    Log.d("SAMIBOX_SCANNER", "TOTAL APPS: ${sorted.size}")
    return sorted
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SystemMonitorLog(
    telemetry: SystemTelemetry,
    networkStatus: NetworkStatus,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scannedApps: List<ScannedApp> by remember { mutableStateOf(scanAllApps(context)) }
    val techFont = ShareTechMonoFontFamily

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .background(Color(0xFF070B16), RoundedCornerShape(16.dp))
                .border(1.5.dp, CyberCyan, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header del Dialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CENTRO DE TELEMETRÍA // DIAGNÓSTICO DEL SISTEMA",
                            color = CyberCyan,
                            fontSize = 18.sp,
                            fontFamily = techFont,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${telemetry.deviceModel} | ${telemetry.androidVersion} | Uptime: ${telemetry.uptime}",
                            color = CyberAmber,
                            fontSize = 12.sp,
                            fontFamily = techFont
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF141926),
                            focusedContainerColor = CyberCyan
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "✕ CERRAR",
                            fontFamily = techFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Panel Superior de Telemetría (3 Tarjetas HUD)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tarjeta 1: RAM
                    val ramColor = when {
                        telemetry.isLowMemory || telemetry.ramUsagePercentage > 85 -> CyberMagenta
                        telemetry.ramUsagePercentage > 70 -> CyberAmber
                        else -> CyberCyan
                    }
                    TelemetryHudBox(
                        title = "MEMORIA RAM",
                        value = "${telemetry.ramUsedMb} / ${telemetry.ramTotalMb} MB",
                        subValue = "Libre: ${telemetry.ramAvailableMb} MB (${100 - telemetry.ramUsagePercentage}%)",
                        status = "${telemetry.ramUsagePercentage}% EN USO",
                        statusColor = ramColor,
                        font = techFont,
                        modifier = Modifier.weight(1f)
                    )

                    // Tarjeta 2: Almacenamiento
                    TelemetryHudBox(
                        title = "ALMACENAMIENTO INTERNO",
                        value = "${telemetry.storageUsedGb} / ${telemetry.storageTotalGb} GB",
                        subValue = "Espacio libre: ${telemetry.storageFreeGb} GB",
                        status = "FLASH STORAGE OK",
                        statusColor = CyberCyan,
                        font = techFont,
                        modifier = Modifier.weight(1f)
                    )

                    // Tarjeta 3: Procesador & Red
                    TelemetryHudBox(
                        title = "HARDWARE & CONECTIVIDAD",
                        value = "CPU: ${telemetry.cpuUsagePercentage} | ${telemetry.cpuTemperature ?: "TEMP OK"}",
                        subValue = "Red: ${networkStatus.type} (${networkStatus.ipAddress ?: "Sin IP"})",
                        status = if (networkStatus.isConnected) "ONLINE" else "OFFLINE",
                        statusColor = if (networkStatus.isConnected) CyberCyan else CyberMagenta,
                        font = techFont,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Título de la lista de Apps
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APLICACIONES INSTALADAS Y PAQUETES (${scannedApps.size})",
                        color = CyberCyan,
                        fontFamily = techFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Haz clic en una app para gestionar o desinstalar",
                        color = CyberGrey,
                        fontFamily = techFont,
                        fontSize = 11.sp
                    )
                }

                // Lista de aplicaciones escaneadas
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = scannedApps, key = { it.packageName }) { app ->
                        AppLogItem(app = app, techFont = techFont, context = context)
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryHudBox(
    title: String,
    value: String,
    subValue: String,
    status: String,
    statusColor: Color,
    font: FontFamily,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0C1322))
            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = CyberGrey,
                    fontSize = 10.sp,
                    fontFamily = font,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = status,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontFamily = font,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = font,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subValue,
                color = Color(0xFF7E9BB8),
                fontSize = 11.sp,
                fontFamily = font
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppLogItem(
    app: ScannedApp,
    techFont: FontFamily,
    context: Context
) {
    val textColor = when {
        app.isBloatware -> CyberMagenta
        !app.isSystemApp -> CyberCyan
        else -> Color(0xFF00FF88)
    }

    val focusBorderColor = when {
        app.isBloatware -> CyberMagenta
        !app.isSystemApp -> CyberCyan
        else -> Color(0xFF00FF88)
    }

    val prefix = when {
        app.isBloatware -> "[BLOATWARE]"
        !app.isSystemApp -> "[USER APP] "
        else -> "[SYS APP]  "
    }

    Card(
        onClick = {
            try {
                val intent = Intent(Intent.ACTION_DELETE)
                intent.data = Uri.parse("package:${app.packageName}")
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        shape = CardDefaults.shape(RoundedCornerShape(6.dp)),
        colors = CardDefaults.colors(
            containerColor = CyberCard,
            focusedContainerColor = Color(0xFF1B2238)
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(1.5.dp, focusBorderColor),
                shape = RoundedCornerShape(6.dp)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .background(textColor, shape = RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$prefix ${app.name}  ->  ${app.packageName}",
                color = textColor,
                fontFamily = techFont,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
