package com.launcher.samiboxtv.presentation.home

import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.rememberAsyncImagePainter
import com.launcher.samiboxtv.BuildConfig
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.domain.model.NetworkType
import com.launcher.samiboxtv.domain.model.SystemTelemetry
import com.launcher.samiboxtv.presentation.components.AddAppDialog
import com.launcher.samiboxtv.presentation.components.AppContextMenu
import com.launcher.samiboxtv.presentation.components.UpdateDialog
import com.launcher.samiboxtv.presentation.overlay.SystemMonitorLog
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )

    uiState.selectedAppForMenu?.let { app ->
        AppContextMenu(
            appItem = app,
            onDismiss = { viewModel.onEvent(HomeUiEvent.CloseContextMenu) },
            onMove = { viewModel.onEvent(HomeUiEvent.StartMovingApp(app)) },
            onHide = { viewModel.onEvent(HomeUiEvent.HideApp(app)) },
            onToggleFavorite = { viewModel.onEvent(HomeUiEvent.ToggleAppFavorite(app)) }
        )
    }

    if (uiState.isAddDialogOpen) {
        AddAppDialog(
            allInstalledApps = uiState.allInstalledApps,
            onToggleVisibility = { viewModel.onEvent(HomeUiEvent.ToggleAppVisibility(it)) },
            onShowAll = { viewModel.onEvent(HomeUiEvent.ShowAllApps) },
            onHideAll = { viewModel.onEvent(HomeUiEvent.HideAllApps) },
            onDismiss = { viewModel.onEvent(HomeUiEvent.CloseAddDialog) }
        )
    }

    if (uiState.isSystemLogOpen) {
        SystemMonitorLog(
            telemetry = uiState.systemTelemetry,
            networkStatus = uiState.networkStatus,
            activeTab = uiState.activeMonitorTab,
            runningProcesses = uiState.runningProcesses,
            isCleaningRam = uiState.isCleaningRam,
            ramCleanMessage = uiState.ramCleanMessage,
            onSelectTab = { viewModel.onEvent(HomeUiEvent.ChangeMonitorTab(it)) },
            onCleanRam = { viewModel.onEvent(HomeUiEvent.CleanRam) },
            onRefreshProcesses = { viewModel.onEvent(HomeUiEvent.LoadRunningProcesses) },
            onKillProcess = { viewModel.onEvent(HomeUiEvent.KillProcess(it)) },
            onDismiss = { viewModel.onEvent(HomeUiEvent.CloseSystemLog) }
        )
    }

    uiState.updateInfo?.let { updateInfo ->
        UpdateDialog(
            updateInfo = updateInfo,
            onDismiss = { viewModel.onEvent(HomeUiEvent.DismissUpdateDialog) }
        )
    }
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060913)),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. ZONA SUPERIOR HUD (Reloj, Terminal de Estado e Iconografía)
        item {
            CyberHudHeader(
                networkStatus = uiState.networkStatus,
                systemTelemetry = uiState.systemTelemetry,
                isCheckingUpdates = uiState.isCheckingUpdates,
                updateCheckMessage = uiState.updateCheckMessage,
                onCheckUpdates = { onEvent(HomeUiEvent.CheckUpdates) },
                onOpenLog = { onEvent(HomeUiEvent.OpenSystemLog) }
            )
        }

        // 2. SECCIÓN DESTACADAS (FEATURED APPLICATIONS)
        item {
            val featured = if (uiState.featuredApps.isNotEmpty()) {
                uiState.featuredApps
            } else {
                uiState.allApps.take(6)
            }

            Column {
                SectionTitle(title = "FEATURED APPLICATIONS")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items = featured, key = { it.packageName }) { app ->
                        TvCyberBannerCard(
                            appItem = app,
                            onClick = { onEvent(HomeUiEvent.LaunchApp(app)) },
                            onLongClick = { onEvent(HomeUiEvent.OpenContextMenu(app)) },
                            modifier = Modifier.width(220.dp)
                        )
                    }
                }
            }
        }

        // 3. SECCIÓN BIBLIOTECA (APPS & GAMES)
        item {
            Column {
                SectionTitle(title = "APPS & GAMES")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(items = uiState.allApps, key = { it.packageName }) { app ->
                        TvCyberBannerCard(
                            appItem = app,
                            onClick = { onEvent(HomeUiEvent.LaunchApp(app)) },
                            onLongClick = { onEvent(HomeUiEvent.OpenContextMenu(app)) },
                            modifier = Modifier.width(180.dp)
                        )
                    }

                    // Botón "Añadir Aplicación" integrado al final de la lista
                    item {
                        AddAppCyberCard(
                            onClick = { onEvent(HomeUiEvent.OpenAddDialog) },
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = CyberCyan.copy(alpha = 0.85f),
        fontFamily = ShareTechMonoFontFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CyberHudHeader(
    networkStatus: NetworkStatus,
    systemTelemetry: SystemTelemetry,
    isCheckingUpdates: Boolean = false,
    updateCheckMessage: String? = null,
    onCheckUpdates: () -> Unit = {},
    onOpenLog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bloque Izquierdo: Reloj + Telemetría de red
        Column {
            ClockView()
            Spacer(modifier = Modifier.height(4.dp))
            NetworkIndicator(networkStatus = networkStatus)
        }

        // Bloque Central: Widget Terminal HUD interactivo sin doble borde inset
        TelemetryHudCard(
            networkStatus = networkStatus,
            systemTelemetry = systemTelemetry,
            isCheckingUpdates = isCheckingUpdates,
            updateCheckMessage = updateCheckMessage,
            onCheckUpdates = onCheckUpdates,
            onOpenLog = onOpenLog
        )

        // Bloque Derecho: Branding
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "CYBERNET",
                color = CyberCyan,
                fontSize = 20.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Text(
                text = "HUD INTERFACE v${BuildConfig.VERSION_NAME}",
                color = CyberAmber,
                fontSize = 10.sp,
                fontFamily = ShareTechMonoFontFamily
            )
        }
    }
}

@Composable
fun TelemetryHudCard(
    networkStatus: NetworkStatus,
    systemTelemetry: SystemTelemetry,
    isCheckingUpdates: Boolean = false,
    updateCheckMessage: String? = null,
    onCheckUpdates: () -> Unit = {},
    onOpenLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(10.dp)
    val borderColor = if (isFocused) CyberCyan else CyberCyan.copy(alpha = 0.35f)
    val borderWidth = if (isFocused) 2.dp else 1.dp
    val containerBg = if (isFocused) Color(0xFF0F182E) else Color(0xFF09101F)
    val ramColor = when {
        systemTelemetry.isLowMemory || systemTelemetry.ramUsagePercentage > 85 -> CyberMagenta
        systemTelemetry.ramUsagePercentage > 70 -> CyberAmber
        else -> CyberCyan
    }

    Box(
        modifier = modifier
            .width(460.dp)
            .height(100.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clip(shape)
            .background(containerBg)
            .border(width = borderWidth, color = borderColor, shape = shape)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp &&
                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                     keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                     keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    onOpenLog()
                    true
                } else false
            }
            .clickable { onOpenLog() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYS HUD // v${BuildConfig.VERSION_NAME}",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isFocused) "PRESS [OK]: GESTOR RAM & PROCESOS" else "[${getRamAsciiBar(systemTelemetry.ramUsagePercentage)}]",
                    color = if (isFocused) CyberAmber else CyberCyan.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "> RAM: ${systemTelemetry.ramUsedMb}/${systemTelemetry.ramTotalMb} MB (${systemTelemetry.ramUsagePercentage}%)",
                    color = ramColor,
                    fontSize = 11.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "LIBRE: ${systemTelemetry.ramAvailableMb}MB",
                    color = Color(0xFF7E9BB8),
                    fontSize = 10.sp,
                    fontFamily = ShareTechMonoFontFamily
                )
            }
            if (isCheckingUpdates) {
                Text(
                    text = "> CHECKING REPO... [BUSCANDO ACTUALIZACIONES]",
                    color = CyberAmber,
                    fontSize = 10.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            } else if (!updateCheckMessage.isNullOrBlank()) {
                Text(
                    text = "> $updateCheckMessage",
                    color = if (updateCheckMessage.startsWith("Error")) CyberMagenta else CyberCyan,
                    fontSize = 10.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "> CPU: ${systemTelemetry.cpuUsagePercentage} | TEMP: ${systemTelemetry.cpuTemperature ?: "NORMAL"} | UP: ${systemTelemetry.uptime}",
                    color = Color(0xFFA6C5E2),
                    fontSize = 10.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    lineHeight = 13.sp
                )
            }
            Text(
                text = "> ALM: ${systemTelemetry.storageUsedGb}/${systemTelemetry.storageTotalGb} GB | SYSTEM: JETPACK COMPOSE TV",
                color = Color(0xFF5E7B98),
                fontSize = 10.sp,
                fontFamily = ShareTechMonoFontFamily
            )
        }
    }
}

@Composable
fun NetworkIndicator(
    networkStatus: NetworkStatus,
    modifier: Modifier = Modifier
) {
    val (iconRes, label, color) = when (networkStatus.type) {
        NetworkType.ETHERNET -> Triple(
            R.drawable.ic_ethernet,
            "ETHERNET (LAN)",
            CyberCyan
        )
        NetworkType.WIFI -> Triple(
            R.drawable.ic_wifi,
            "WI-FI (ONLINE)",
            CyberCyan
        )
        NetworkType.CELLULAR -> Triple(
            R.drawable.ic_wifi,
            "MÓVIL (ONLINE)",
            CyberCyan
        )
        NetworkType.DISCONNECTED, NetworkType.UNKNOWN -> Triple(
            R.drawable.ic_network_disconnected,
            "DESCONECTADO",
            CyberMagenta
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Estado de Red",
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = label,
            color = color,
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        if (!networkStatus.ipAddress.isNullOrBlank() && networkStatus.isConnected) {
            Text(
                text = "| ${networkStatus.ipAddress}",
                color = CyberCyan.copy(alpha = 0.6f),
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 11.sp
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvCyberBannerCard(
    appItem: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = Color(0xFF0C1322),
            focusedContainerColor = Color(0xFF142038)
        ),
        scale = CardDefaults.scale(
            scale = 1.0f,
            focusedScale = 1.06f
        ),
        border = CardDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ),
            focusedBorder = Border(
                border = BorderStroke(2.dp, CyberCyan),
                shape = RoundedCornerShape(8.dp)
            ),
            pressedBorder = Border(
                border = BorderStroke(2.dp, CyberMagenta),
                shape = RoundedCornerShape(8.dp)
            )
        ),
        modifier = modifier
            .aspectRatio(16f / 9f)
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp &&
                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                     keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                     keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    onClick()
                    true
                } else false
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (appItem.bannerDrawable != null) {
                // Banner nativo 16:9 de Android TV
                Image(
                    painter = rememberAsyncImagePainter(model = appItem.bannerDrawable),
                    contentDescription = appItem.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Generador visual Cyberpunk cuando la app solo provee icono móvil 1:1
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF1A2744), Color(0xFF0A0F1D))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = appItem.iconDrawable),
                        contentDescription = appItem.name,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            // Gradiente HUD inferior para legibilidad del título
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xDD040711)),
                            startY = 60f
                        )
                    )
            )

            // Etiqueta del nombre de la App
            Text(
                text = appItem.name.uppercase(),
                color = if (isFocused) CyberCyan else Color.White,
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .then(if (isFocused) Modifier.basicMarquee() else Modifier)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AddAppCyberCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = Color(0xFF090F1B),
            focusedContainerColor = Color(0xFF161F2E)
        ),
        scale = CardDefaults.scale(
            scale = 1.0f,
            focusedScale = 1.06f
        ),
        border = CardDefaults.border(
            border = Border(
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ),
            focusedBorder = Border(
                border = BorderStroke(2.dp, CyberAmber),
                shape = RoundedCornerShape(8.dp)
            ),
            pressedBorder = Border(
                border = BorderStroke(2.dp, CyberAmber),
                shape = RoundedCornerShape(8.dp)
            )
        ),
        modifier = modifier
            .aspectRatio(16f / 9f)
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp &&
                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                     keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                     keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    onClick()
                    true
                } else false
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = rememberAsyncImagePainter(model = R.drawable.ic_add_retro),
                    contentDescription = "Añadir",
                    colorFilter = ColorFilter.tint(if (isFocused) CyberAmber else CyberGrey),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "+ GESTIONAR APPS",
                    color = if (isFocused) CyberAmber else CyberGrey,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ClockView() {
    var timeText by remember { mutableStateOf(getFormattedTime()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            timeText = getFormattedTime()
        }
    }
    Text(
        text = timeText,
        color = CyberAmber,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = ShareTechMonoFontFamily
    )
}

fun getFormattedTime(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("America/Bogota")
    return sdf.format(Date())
}

fun getRamAsciiBar(pct: Int): String {
    val totalBars = 6
    val filled = (pct * totalBars / 100).coerceIn(0, totalBars)
    val empty = totalBars - filled
    return "█".repeat(filled) + "░".repeat(empty)
}
