package com.launcher.samiboxtv.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.components.AddAppDialog
import com.launcher.samiboxtv.presentation.components.AppContextSideDrawer
import com.launcher.samiboxtv.presentation.components.AssignCategoryDialog
import com.launcher.samiboxtv.presentation.components.CyberAppCarousel
import com.launcher.samiboxtv.presentation.components.CyberHeaderHud
import com.launcher.samiboxtv.presentation.components.CyberModernGrid
import com.launcher.samiboxtv.presentation.components.UpdateDialog
import com.launcher.samiboxtv.presentation.components.cards.AddAppCyberCard
import com.launcher.samiboxtv.presentation.iptv.IptvPlayerScreen
import com.launcher.samiboxtv.presentation.media.CyberMediaPlayer
import com.launcher.samiboxtv.presentation.media.MediaExplorerScreen
import com.launcher.samiboxtv.presentation.overlay.SystemMonitorLog
import com.launcher.samiboxtv.presentation.settings.TvSettingsPanel
import com.launcher.samiboxtv.presentation.theme.AppLayoutMode
import com.launcher.samiboxtv.presentation.theme.rememberTvLayoutDimensions
import com.launcher.samiboxtv.util.CategoryHelper

/**
 * HomeScreen: Director de orquesta principal del Launcher.
 * Conecta el estado del ViewModel con la UI y administra diálogos/overlays.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var appToMoveCategory by remember { mutableStateOf<AppItem?>(null) }

    // 1. Prioridad Máxima: Si estás reorganizando tarjetas (modo fantasma)
    BackHandler(enabled = uiState.movingAppPackageName != null) {
        viewModel.onEvent(HomeUiEvent.ConfirmReorder)
    }

    // 2. Prioridad Menú Contextual (Side Drawer)
    BackHandler(enabled = uiState.selectedAppForMenu != null) {
        viewModel.onEvent(HomeUiEvent.CloseContextMenu)
    }

    // 3. Prioridad Diálogo de Crear Categoría / Añadir
    BackHandler(enabled = uiState.isAddDialogOpen) {
        viewModel.onEvent(HomeUiEvent.CloseAddDialog)
    }

    // 4. Prioridad Centro de Telemetría / RAM
    BackHandler(enabled = uiState.isSystemLogOpen) {
        viewModel.onEvent(HomeUiEvent.CloseSystemLog)
    }

    // 5. Prioridad Panel de Ajustes Lateral
    BackHandler(enabled = uiState.isSettingsOpen) {
        viewModel.onEvent(HomeUiEvent.CloseSettings)
    }

    // 6. Base del Launcher: Bloquea la salida en la pantalla principal
    BackHandler(enabled = !uiState.isIptvPlayerOpen && uiState.currentPlayingMedia == null) {
        // Un Launcher nunca debe salir al pulsar Atrás
    }

    Box(modifier = modifier.fillMaxSize()) {
        HomeScreenContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            modifier = Modifier.fillMaxSize()
        )
    }

    // Overlays y Diálogos
    uiState.selectedAppForMenu?.let { app ->
        AppContextSideDrawer(
            appItem = app,
            onDismiss = { viewModel.onEvent(HomeUiEvent.CloseContextMenu) },
            onLaunch = { viewModel.onEvent(HomeUiEvent.LaunchApp(app)) },
            onToggleFavorite = { viewModel.onEvent(HomeUiEvent.ToggleAppFavorite(app)) },
            onHide = { viewModel.onEvent(HomeUiEvent.HideApp(app)) },
            onStartReorder = { viewModel.onEvent(HomeUiEvent.StartReordering(app)) },
            onMoveCategory = {
                viewModel.onEvent(HomeUiEvent.CloseContextMenu)
                appToMoveCategory = app
            }
        )
    }

    appToMoveCategory?.let { app ->
        AssignCategoryDialog(
            app = app,
            categories = uiState.categories,
            currentCategory = app.category,
            onDismiss = { appToMoveCategory = null },
            onSelectCategory = { newCategory ->
                viewModel.onEvent(HomeUiEvent.AssignCategory(app.packageName, newCategory))
                appToMoveCategory = null
            }
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

    if (uiState.isSettingsOpen) {
        TvSettingsPanel(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
    }

    uiState.updateInfo?.let { updateInfo ->
        UpdateDialog(
            updateInfo = updateInfo,
            onDismiss = { viewModel.onEvent(HomeUiEvent.DismissUpdateDialog) }
        )
    }

    if (uiState.isMediaHubOpen) {
        MediaExplorerScreen(
            drives = uiState.storageDrives,
            selectedDrive = uiState.selectedDrive,
            currentFilter = uiState.mediaFilter,
            files = uiState.mediaFilesList,
            isLoading = uiState.isLoadingMedia,
            onSelectDrive = { viewModel.onEvent(HomeUiEvent.SelectStorageDrive(it)) },
            onFilterChange = { viewModel.onEvent(HomeUiEvent.FilterMediaType(it)) },
            onSelectFile = { viewModel.onEvent(HomeUiEvent.PlayMedia(it)) },
            onRefresh = { viewModel.onEvent(HomeUiEvent.RefreshMedia) },
            onBack = { viewModel.onEvent(HomeUiEvent.CloseMediaHub) }
        )
    }

    uiState.currentPlayingMedia?.let { mediaFile ->
        CyberMediaPlayer(
            mediaFile = mediaFile,
            onClose = { viewModel.onEvent(HomeUiEvent.CloseMediaPlayer) },
            onPlayNext = { viewModel.onEvent(HomeUiEvent.PlayNextMedia) },
            onPlayPrevious = { viewModel.onEvent(HomeUiEvent.PlayPreviousMedia) }
        )
    }

    if (uiState.isIptvPlayerOpen) {
        IptvPlayerScreen(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
    }
}

/**
 * HomeScreenContent: Ensambla el feed vertical del Launcher con diseño adaptativo,
 * anclaje magnético Snap Fling y estética Cyberpunk Neón.
 */
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = rememberTvLayoutDimensions(layoutMode = uiState.appLayoutMode)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060913)),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. ZONA SUPERIOR HUD MILITAR (Reloj militar HH:mm:ss + Perfil de pantalla + Nodos)
        item(key = "header_hud") {
            CyberHeaderHud(
                layoutMode = uiState.appLayoutMode,
                appCount = uiState.allApps.size,
                onOpenSettings = { onEvent(HomeUiEvent.OpenSettings) }
            )
        }

        if (uiState.appLayoutMode == AppLayoutMode.MODERN_GRID) {
            // ==============================================================
            // MODO CUADRÍCULA MODERNA (3 columnas x 2 filas = 6 en pantalla)
            // ==============================================================
            val favoriteApps = uiState.allApps.filter { it.isFavorite }
            if (favoriteApps.isNotEmpty()) {
                item(key = "grid_section_favorites") {
                    CyberModernGrid(
                        title = "★ FAVORITOS & DESTACADOS",
                        apps = favoriteApps,
                        dimensions = dimensions,
                        movingAppPackageName = uiState.movingAppPackageName,
                        onLaunchApp = { onEvent(HomeUiEvent.LaunchApp(it)) },
                        onOpenContextMenu = { onEvent(HomeUiEvent.OpenContextMenu(it)) },
                        onMoveDirection = { onEvent(HomeUiEvent.MoveApp(uiState.movingAppPackageName ?: "", it)) },
                        onConfirmMove = { onEvent(HomeUiEvent.ConfirmReorder) }
                    )
                }
            }

            val nonFavoriteApps = uiState.allApps.filter { !it.isFavorite }
            if (nonFavoriteApps.isNotEmpty()) {
                item(key = "grid_section_all") {
                    CyberModernGrid(
                        title = if (favoriteApps.isEmpty()) "TODAS LAS APLICACIONES" else "OTRAS APLICACIONES",
                        apps = nonFavoriteApps,
                        dimensions = dimensions,
                        movingAppPackageName = uiState.movingAppPackageName,
                        onLaunchApp = { onEvent(HomeUiEvent.LaunchApp(it)) },
                        onOpenContextMenu = { onEvent(HomeUiEvent.OpenContextMenu(it)) },
                        onMoveDirection = { onEvent(HomeUiEvent.MoveApp(uiState.movingAppPackageName ?: "", it)) },
                        onConfirmMove = { onEvent(HomeUiEvent.ConfirmReorder) }
                    )
                }
            }
        } else {
            // ==============================================================
            // MODOS HORIZONTALES (COMPACT_HIGH_DENSITY, PANORAMIC_16_9, COMPACT_STANDARD, DUAL_PRIORITY)
            // ==============================================================

            // 2. SECCIÓN FAVORITOS
            val favoriteApps = uiState.allApps.filter { it.isFavorite }
            if (favoriteApps.isNotEmpty()) {
                item(key = "section_favorites") {
                    CyberAppCarousel(
                        title = "★ FAVORITOS & DESTACADOS",
                        apps = favoriteApps,
                        dimensions = dimensions,
                        movingAppPackageName = uiState.movingAppPackageName,
                        onLaunchApp = { onEvent(HomeUiEvent.LaunchApp(it)) },
                        onOpenContextMenu = { onEvent(HomeUiEvent.OpenContextMenu(it)) },
                        onMoveDirection = { onEvent(HomeUiEvent.MoveApp(uiState.movingAppPackageName ?: "", it)) },
                        onConfirmMove = { onEvent(HomeUiEvent.ConfirmReorder) }
                    )
                }
            }

            // 3. SECCIONES DINÁMICAS POR CATEGORÍA
            val nonFavoriteApps = uiState.allApps.filter { !it.isFavorite }
            val activeCategoriesWithApps = uiState.categories.mapNotNull { categoryName ->
                val apps = nonFavoriteApps.filter { app ->
                    val assigned = uiState.appCategoryMap[app.packageName] ?: app.category.ifBlank { "APPS" }
                    assigned.equals(categoryName, ignoreCase = true)
                }
                if (apps.isNotEmpty()) categoryName to apps else null
            }

            activeCategoriesWithApps.forEach { (categoryName, categoryApps) ->
                item(key = "section_cat_$categoryName") {
                    CyberAppCarousel(
                        title = "${CategoryHelper.getCategoryIcon(categoryName)} $categoryName",
                        apps = categoryApps,
                        dimensions = dimensions,
                        movingAppPackageName = uiState.movingAppPackageName,
                        onLaunchApp = { onEvent(HomeUiEvent.LaunchApp(it)) },
                        onOpenContextMenu = { onEvent(HomeUiEvent.OpenContextMenu(it)) },
                        onMoveDirection = { onEvent(HomeUiEvent.MoveApp(uiState.movingAppPackageName ?: "", it)) },
                        onConfirmMove = { onEvent(HomeUiEvent.ConfirmReorder) }
                    )
                }
            }

            // Garantía absoluta contra pantalla en blanco al instalar
            if (activeCategoriesWithApps.isEmpty()) {
                if (favoriteApps.isEmpty() && nonFavoriteApps.isNotEmpty()) {
                    item(key = "section_fallback_all") {
                        CyberAppCarousel(
                            title = "${CategoryHelper.getCategoryIcon("APPS")} TODAS LAS APLICACIONES",
                            apps = nonFavoriteApps,
                            dimensions = dimensions,
                            movingAppPackageName = uiState.movingAppPackageName,
                            onLaunchApp = { onEvent(HomeUiEvent.LaunchApp(it)) },
                            onOpenContextMenu = { onEvent(HomeUiEvent.OpenContextMenu(it)) },
                            onMoveDirection = { onEvent(HomeUiEvent.MoveApp(uiState.movingAppPackageName ?: "", it)) },
                            onConfirmMove = { onEvent(HomeUiEvent.ConfirmReorder) }
                        )
                    }
                } else if (favoriteApps.isEmpty()) {
                    item(key = "section_manage_empty") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = dimensions.horizontalPadding),
                            horizontalArrangement = Arrangement.spacedBy(dimensions.spacing)
                        ) {
                            AddAppCyberCard(
                                cardStyle = uiState.cardStyle,
                                onClick = { onEvent(HomeUiEvent.OpenAddDialog) },
                                modifier = Modifier.width(dimensions.cardWidth)
                            )
                        }
                    }
                }
            }
        }
    }
}