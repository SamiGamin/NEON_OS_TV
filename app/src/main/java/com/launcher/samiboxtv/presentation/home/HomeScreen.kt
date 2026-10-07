package com.launcher.samiboxtv.presentation.home

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.rememberAsyncImagePainter
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.components.AddAppDialog
import com.launcher.samiboxtv.presentation.components.AppContextMenu
import com.launcher.samiboxtv.presentation.overlay.SystemMonitorLog
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
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
            onDismiss = { viewModel.onEvent(HomeUiEvent.CloseSystemLog) }
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
            CyberHudHeader(onOpenLog = { onEvent(HomeUiEvent.OpenSystemLog) })
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

@Composable
fun CyberHudHeader(onOpenLog: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp, end = 48.dp, top = 24.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bloque Izquierdo: Reloj + Info de red
        Column {
            ClockView()
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NET STATUS: ONLINE | BOGOTÁ",
                color = CyberCyan.copy(alpha = 0.6f),
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 12.sp
            )
        }

        // Bloque Central: Widget Terminal HUD (Clickeable para abrir logs completos)
        Box(
            modifier = Modifier
                .width(420.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF09101F))
                .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                .clickable { onOpenLog() }
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = "SAMIBOX TV LAUNCHER // HUD SYS",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "> SYSTEM OK\n> MEMORY: STABLE\n> RENDER: JETPACK COMPOSE TV",
                    color = Color(0xFF7E9BB8),
                    fontSize = 10.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    lineHeight = 13.sp
                )
            }
        }

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
                text = "HUD INTERFACE v2.0",
                color = CyberAmber,
                fontSize = 10.sp,
                fontFamily = ShareTechMonoFontFamily
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
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.06f else 1.0f, label = "card_scale")
    val borderColor = if (isFocused) CyberCyan else CyberCyan.copy(alpha = 0.2f)
    val glowWidth = if (isFocused) 2.dp else 1.dp

    Card(
        onClick = onClick,
        onLongClick = onLongClick,
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = Color(0xFF0C1322),
            focusedContainerColor = Color(0xFF142038)
        ),
        modifier = modifier
            .scale(scale)
            .aspectRatio(16f / 9f)
            .onFocusChanged { isFocused = it.isFocused }
            .border(glowWidth, borderColor, RoundedCornerShape(8.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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

@Composable
fun AddAppCyberCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) CyberAmber else CyberCyan.copy(alpha = 0.2f)

    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF090F1B))
            .border(if (isFocused) 2.dp else 1.dp, borderColor, RoundedCornerShape(8.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() },
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
