package com.launcher.samiboxtv.presentation.overlay

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import coil.compose.rememberAsyncImagePainter
import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.domain.model.ProcessInfo
import com.launcher.samiboxtv.domain.model.SystemMonitorTab
import com.launcher.samiboxtv.domain.model.SystemTelemetry
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCard
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import com.launcher.samiboxtv.util.AppUsagePermissionHelper

data class ScannedApp(
    val name: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val isBloatware: Boolean
)

suspend fun scanAllApps(context: Context): List<ScannedApp> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val packages = try {
        pm.getInstalledApplications(0)
    } catch (_: Exception) {
        emptyList()
    }

    val knownBloatware = setOf(
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

    val scannedList = ArrayList<ScannedApp>(packages.size)

    for (appInfo in packages) {
        val pkg = appInfo.packageName
        val appName = try {
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            pkg
        }
        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val isBloatware = knownBloatware.contains(pkg)
        scannedList.add(ScannedApp(appName, pkg, isSystemApp, isBloatware))
    }

    scannedList.sortedWith(
        compareBy({ !it.isBloatware }, { it.isSystemApp }, { it.name.lowercase() })
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SystemMonitorLog(
    telemetry: SystemTelemetry,
    networkStatus: NetworkStatus,
    activeTab: SystemMonitorTab = SystemMonitorTab.RAM_PROCESSES,
    runningProcesses: List<ProcessInfo> = emptyList(),
    isCleaningRam: Boolean = false,
    ramCleanMessage: String? = null,
    onSelectTab: (SystemMonitorTab) -> Unit = {},
    onCleanRam: () -> Unit = {},
    onRefreshProcesses: () -> Unit = {},
    onKillProcess: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var scannedApps by remember { mutableStateOf<List<ScannedApp>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(false) }
    var showOnlyUserApps by remember { mutableStateOf(false) }
    var selectedProcessForAction by remember { mutableStateOf<ProcessInfo?>(null) }
    val techFont = ShareTechMonoFontFamily

    val hasUsageStatsPermission = remember(activeTab) {
        AppUsagePermissionHelper.hasUsageStatsPermission(context)
    }

    LaunchedEffect(activeTab) {
        if (activeTab == SystemMonitorTab.INSTALLED_APPS && scannedApps.isEmpty()) {
            isLoadingApps = true
            scannedApps = scanAllApps(context)
            isLoadingApps = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.94f)
                .background(Color(0xFF070B16), RoundedCornerShape(16.dp))
                .border(1.5.dp, CyberCyan, RoundedCornerShape(16.dp))
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Header del Dialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CENTRO DE TELEMETRÍA // GESTOR DE PROCESOS & RAM",
                            color = CyberCyan,
                            fontSize = 18.sp,
                            fontFamily = techFont,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${telemetry.deviceModel} | ${telemetry.androidVersion} | UPTIME: ${telemetry.uptime}",
                            color = CyberAmber,
                            fontSize = 11.sp,
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

                // 2. Panel Superior de Telemetría (3 Tarjetas HUD con interactividad en RAM)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tarjeta 1: RAM (Interactiva - al hacer clic enfoca la lista de procesos)
                    val ramColor = when {
                        telemetry.isLowMemory || telemetry.ramUsagePercentage > 85 -> CyberMagenta
                        telemetry.ramUsagePercentage > 70 -> CyberAmber
                        else -> CyberCyan
                    }
                    TelemetryHudBox(
                        title = "MEMORIA RAM // CLIC: PROCESOS",
                        value = "${telemetry.ramUsedMb} / ${telemetry.ramTotalMb} MB",
                        subValue = "Libre: ${telemetry.ramAvailableMb} MB (${100 - telemetry.ramUsagePercentage}%)",
                        status = "${telemetry.ramUsagePercentage}% EN USO",
                        statusColor = ramColor,
                        font = techFont,
                        isSelected = activeTab == SystemMonitorTab.RAM_PROCESSES,
                        onClick = { onSelectTab(SystemMonitorTab.RAM_PROCESSES) },
                        modifier = Modifier.weight(1f)
                    )

                    // Tarjeta 2: Almacenamiento
                    TelemetryHudBox(
                        title = "ALMACENAMIENTO INTERNO",
                        value = "${telemetry.storageUsedGb} / ${telemetry.storageTotalGb} GB",
                        subValue = "Libre: ${telemetry.storageFreeGb} GB",
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

                // 3. Barra de Control: Pestañas + Botón de Limpieza de RAM
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selector de Pestañas
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onSelectTab(SystemMonitorTab.RAM_PROCESSES) },
                            colors = ButtonDefaults.colors(
                                containerColor = if (activeTab == SystemMonitorTab.RAM_PROCESSES) Color(0xFF132238) else Color(0xFF0E131F),
                                focusedContainerColor = CyberCyan
                            ),
                            border = ButtonDefaults.border(
                                border = Border(
                                    border = BorderStroke(1.dp, if (activeTab == SystemMonitorTab.RAM_PROCESSES) CyberCyan else Color(0x33486581)),
                                    shape = RoundedCornerShape(6.dp)
                                ),
                                focusedBorder = Border(
                                    border = BorderStroke(1.5.dp, CyberCyan),
                                    shape = RoundedCornerShape(6.dp)
                                )
                            ),
                            shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = "⚡ PROCESOS EN RAM (${runningProcesses.size})",
                                fontFamily = techFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == SystemMonitorTab.RAM_PROCESSES) CyberCyan else Color.White
                            )
                        }

                        Button(
                            onClick = { onSelectTab(SystemMonitorTab.INSTALLED_APPS) },
                            colors = ButtonDefaults.colors(
                                containerColor = if (activeTab == SystemMonitorTab.INSTALLED_APPS) Color(0xFF132238) else Color(0xFF0E131F),
                                focusedContainerColor = CyberCyan
                            ),
                            border = ButtonDefaults.border(
                                border = Border(
                                    border = BorderStroke(1.dp, if (activeTab == SystemMonitorTab.INSTALLED_APPS) CyberCyan else Color(0x33486581)),
                                    shape = RoundedCornerShape(6.dp)
                                ),
                                focusedBorder = Border(
                                    border = BorderStroke(1.5.dp, CyberCyan),
                                    shape = RoundedCornerShape(6.dp)
                                )
                            ),
                            shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = if (scannedApps.isEmpty()) "📦 TODAS LAS APPS" else "📦 TODAS LAS APPS (${scannedApps.size})",
                                fontFamily = techFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == SystemMonitorTab.INSTALLED_APPS) CyberCyan else Color.White
                            )
                        }
                    }

                    // Botones de acción directa
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onRefreshProcesses,
                            colors = ButtonDefaults.colors(
                                containerColor = Color(0xFF121B2A),
                                focusedContainerColor = CyberCyan
                            ),
                            border = ButtonDefaults.border(
                                border = Border(
                                    border = BorderStroke(1.dp, Color(0x33486581)),
                                    shape = RoundedCornerShape(6.dp)
                                )
                            ),
                            shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = "🔄 ACTUALIZAR",
                                fontFamily = techFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onCleanRam,
                            enabled = !isCleaningRam,
                            colors = ButtonDefaults.colors(
                                containerColor = Color(0xFF2A1B0A),
                                focusedContainerColor = CyberAmber
                            ),
                            border = ButtonDefaults.border(
                                border = Border(
                                    border = BorderStroke(1.5.dp, CyberAmber),
                                    shape = RoundedCornerShape(6.dp)
                                ),
                                focusedBorder = Border(
                                    border = BorderStroke(2.dp, CyberAmber),
                                    shape = RoundedCornerShape(6.dp)
                                )
                            ),
                            shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = if (isCleaningRam) "⚡ LIMPIANDO RAM..." else "⚡ LIMPIAR PROCESOS (LIBERAR RAM)",
                                fontFamily = techFont,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCleaningRam) CyberGrey else CyberAmber
                            )
                        }
                    }
                }

                // 4. Banner de Notificación tras Limpiar RAM
                if (!ramCleanMessage.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF09281C))
                            .border(1.dp, Color(0xFF00FF88), RoundedCornerShape(6.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "> $ramCleanMessage",
                            color = Color(0xFF00FF88),
                            fontFamily = techFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 5. Lista de Contenido (Procesos en RAM o Aplicaciones Instaladas)
                if (activeTab == SystemMonitorTab.RAM_PROCESSES) {
                    val filteredProcesses = if (showOnlyUserApps) {
                        runningProcesses.filter { !it.isSystemApp }
                    } else {
                        runningProcesses
                    }
                    val totalMbShown = filteredProcesses.sumOf { it.memoryUsageMb }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONSUMO REPORTADO: $totalMbShown MB // ${filteredProcesses.size} PROCESOS EN EJECUCIÓN",
                            color = CyberCyan.copy(alpha = 0.9f),
                            fontFamily = techFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { showOnlyUserApps = !showOnlyUserApps },
                            colors = ButtonDefaults.colors(
                                containerColor = if (showOnlyUserApps) Color(0xFF1E2838) else Color(0xFF0F1522),
                                focusedContainerColor = CyberCyan
                            ),
                            border = ButtonDefaults.border(
                                border = Border(
                                    border = BorderStroke(1.dp, if (showOnlyUserApps) CyberAmber else Color(0x33486581)),
                                    shape = RoundedCornerShape(4.dp)
                                )
                            ),
                            shape = ButtonDefaults.shape(RoundedCornerShape(4.dp))
                        ) {
                            Text(
                                text = if (showOnlyUserApps) "[ FILTRO: SOLO APPS USUARIO ]" else "[ MOSTRAR: TODOS LOS PROCESOS ]",
                                fontFamily = techFont,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showOnlyUserApps) CyberAmber else Color(0xFFA6C5E2)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!hasUsageStatsPermission) {
                            item {
                                Card(
                                    onClick = { AppUsagePermissionHelper.requestUsageStatsPermission(context) },
                                    shape = CardDefaults.shape(RoundedCornerShape(6.dp)),
                                    colors = CardDefaults.colors(
                                        containerColor = Color(0xFF26180B),
                                        focusedContainerColor = Color(0xFF3D2611)
                                    ),
                                    border = CardDefaults.border(
                                        border = Border(
                                            border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.7f)),
                                            shape = RoundedCornerShape(6.dp)
                                        ),
                                        focusedBorder = Border(
                                            border = BorderStroke(2.dp, CyberAmber),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⚠ ACCESO A DATOS DE USO REQUERIDO (ANDROID 10/11)  >  Clic aquí para autorizar en Ajustes y listar apps en segundo plano.",
                                            color = CyberAmber,
                                            fontFamily = techFont,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredProcesses.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (showOnlyUserApps) "NO HAY APLICACIONES DE USUARIO EN SEGUNDO PLANO" else "NO SE DETECTARON PROCESOS ACTIVOS EN MEMORIA",
                                        color = CyberGrey,
                                        fontFamily = techFont,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        } else {
                            items(items = filteredProcesses, key = { "${it.packageName}_${it.pid}" }) { proc ->
                                ProcessLogItem(
                                    process = proc,
                                    techFont = techFont,
                                    onKill = { selectedProcessForAction = proc }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "APLICACIONES INSTALADAS Y PAQUETES // CLIC PARA DESINSTALAR",
                        color = CyberCyan.copy(alpha = 0.8f),
                        fontFamily = techFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (isLoadingApps) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "> ESCANEANDO PAQUETES DEL SISTEMA...",
                                color = CyberCyan,
                                fontFamily = techFont,
                                fontSize = 13.sp
                            )
                        }
                    } else {
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
    }

    if (selectedProcessForAction != null) {
        val proc = selectedProcessForAction!!
        Dialog(
            onDismissRequest = { selectedProcessForAction = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .background(Color(0xFF0D1424), RoundedCornerShape(12.dp))
                    .border(1.5.dp, CyberCyan, RoundedCornerShape(12.dp))
                    .padding(22.dp)
            ) {
                Column {
                    Text(
                        text = "GESTIÓN DE APLICACIÓN // ${proc.appName}",
                        fontFamily = techFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${proc.packageName}  //  ${proc.memoryUsageMb} MB RAM ESTIMADO",
                        fontFamily = techFont,
                        fontSize = 11.sp,
                        color = CyberGrey
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onKillProcess(proc.packageName)
                            selectedProcessForAction = null
                        },
                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF16253B),
                            focusedContainerColor = CyberCyan
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = "⚡ LIBERAR EN SEGUNDO PLANO (RAM)",
                            fontFamily = techFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            AppUsagePermissionHelper.openAppForceStopSettings(context, proc.packageName)
                            selectedProcessForAction = null
                        },
                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF2E1924),
                            focusedContainerColor = CyberMagenta
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = "⚙ FORZAR DETENCIÓN (AJUSTES ANDROID)",
                            fontFamily = techFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CyberMagenta
                        )
                    }

                    Button(
                        onClick = { selectedProcessForAction = null },
                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp)),
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF141926),
                            focusedContainerColor = CyberGrey
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✕ CANCELAR",
                            fontFamily = techFont,
                            fontSize = 11.sp
                        )
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
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) CyberCyan else CyberCyan.copy(alpha = 0.3f)
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val bg = if (isSelected) Color(0xFF131F38) else Color(0xFF0C1322)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .then(
                if (onClick != null) {
                    Modifier
                        .focusable()
                        .clickable { onClick() }
                } else Modifier
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = if (isSelected) CyberCyan else CyberGrey,
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
fun ProcessLogItem(
    process: ProcessInfo,
    techFont: FontFamily,
    onKill: () -> Unit
) {
    Card(
        onClick = { if (!process.isSystemApp) onKill() },
        shape = CardDefaults.shape(RoundedCornerShape(6.dp)),
        colors = CardDefaults.colors(
            containerColor = CyberCard,
            focusedContainerColor = Color(0xFF1B2238)
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(1.5.dp, if (process.isSystemApp) CyberGrey else CyberAmber),
                shape = RoundedCornerShape(6.dp)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(if (process.isSystemApp) CyberGrey else CyberCyan, shape = RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                if (process.icon != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = process.icon),
                        contentDescription = process.appName,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column {
                    Text(
                        text = process.appName,
                        color = if (process.isSystemApp) Color(0xFFA6C5E2) else CyberCyan,
                        fontFamily = techFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${process.packageName} // PID: ${if (process.pid > 0) process.pid else "BG"}",
                        color = CyberGrey,
                        fontFamily = techFont,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (process.isSystemApp) "[SISTEMA]" else "[APP USUARIO]",
                    color = if (process.isSystemApp) CyberGrey else CyberAmber,
                    fontFamily = techFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0A1828))
                        .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${process.memoryUsageMb} MB RAM",
                        color = CyberCyan,
                        fontFamily = techFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (!process.isSystemApp) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "DETENER [OK]",
                        color = CyberMagenta,
                        fontFamily = techFont,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
