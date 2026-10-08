package com.launcher.samiboxtv.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.components.AddAppDialog
import com.launcher.samiboxtv.presentation.components.AppContextSideDrawer
import com.launcher.samiboxtv.presentation.components.AssignCategoryDialog
import com.launcher.samiboxtv.presentation.components.UpdateDialog
import com.launcher.samiboxtv.presentation.components.cards.AddAppCyberCard
import com.launcher.samiboxtv.presentation.components.cards.MediaHubCard
import com.launcher.samiboxtv.presentation.components.cards.TvCyberBannerCard
import com.launcher.samiboxtv.presentation.components.hud.CyberHudHeader
import com.launcher.samiboxtv.presentation.media.CyberMediaPlayer
import com.launcher.samiboxtv.presentation.media.MediaExplorerScreen
import com.launcher.samiboxtv.presentation.overlay.SystemInfoOverlay
import com.launcher.samiboxtv.presentation.overlay.SystemMonitorLog
import com.launcher.samiboxtv.presentation.settings.TvSettingsPanel
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
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
}

/**
 * HomeScreenContent: Ensambla el feed vertical del Launcher.
 */
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardWidth = uiState.cardStyle.widthDp.dp

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060913)),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. ZONA SUPERIOR HUD
        item(key = "header_hud") {
            CyberHudHeader(
                networkStatus = uiState.networkStatus,
                onOpenSettings = { onEvent(HomeUiEvent.OpenSettings) }
            )
        }

        // 2. SECCIÓN FAVORITOS
        val favoriteApps = uiState.allApps.filter { it.isFavorite }
        if (favoriteApps.isNotEmpty()) {
            item(key = "section_favorites") {
                CategoryAppRow(
                    title = "★ FAVORITOS & DESTACADOS",
                    apps = favoriteApps,
                    uiState = uiState,
                    onEvent = onEvent
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

        activeCategoriesWithApps.forEachIndexed { index, (categoryName, categoryApps) ->
            val isLast = index == activeCategoriesWithApps.lastIndex
            item(key = "section_cat_$categoryName") {
                CategoryAppRow(
                    title = "${CategoryHelper.getCategoryIcon(categoryName)} $categoryName",
                    apps = categoryApps,
                    uiState = uiState,
                    onEvent = onEvent,
                    showSystemActions = isLast
                )
            }
        }

        // Garantía absoluta contra pantalla en blanco al instalar
        if (activeCategoriesWithApps.isEmpty()) {
            if (favoriteApps.isEmpty() && nonFavoriteApps.isNotEmpty()) {
                // Fallback de rescate: muestra todas las apps no favoritas agrupadas en APPS
                item(key = "section_fallback_all") {
                    CategoryAppRow(
                        title = "${CategoryHelper.getCategoryIcon("APPS")} TODAS LAS APLICACIONES",
                        apps = nonFavoriteApps,
                        uiState = uiState,
                        onEvent = onEvent,
                        showSystemActions = true
                    )
                }
            } else if (favoriteApps.isEmpty()) {
                item(key = "section_manage_empty") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        MediaHubCard(
                            cardStyle = uiState.cardStyle,
                            onClick = { onEvent(HomeUiEvent.OpenMediaHub) },
                            modifier = Modifier.width(cardWidth)
                        )
                        AddAppCyberCard(
                            cardStyle = uiState.cardStyle,
                            onClick = { onEvent(HomeUiEvent.OpenAddDialog) },
                            modifier = Modifier.width(cardWidth)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fila horizontal reutilizable para cualquier categoría o sección de apps.
 */
@Composable
private fun CategoryAppRow(
    title: String,
    apps: List<AppItem>,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    showSystemActions: Boolean = false
) {
    val cardWidth = uiState.cardStyle.widthDp.dp

    Column {
        SectionTitle(title = title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items = apps, key = { it.packageName }) { app ->
                val isMovingThisApp = uiState.movingAppPackageName == app.packageName

                TvCyberBannerCard(
                    appItem = app,
                    cardStyle = uiState.cardStyle,
                    showAppName = uiState.showAppNames,
                    isGhostMode = isMovingThisApp,
                    isAnyAppMoving = uiState.movingAppPackageName != null,
                    isEditing = isMovingThisApp,
                    onClick = {
                        if (isMovingThisApp) {
                            onEvent(HomeUiEvent.ConfirmReorder)
                        } else {
                            onEvent(HomeUiEvent.LaunchApp(app))
                        }
                    },
                    onLongClick = { onEvent(HomeUiEvent.OpenContextMenu(app)) },
                    onMoveDirection = { direction ->
                        onEvent(HomeUiEvent.MoveApp(app.packageName, direction))
                    },
                    onConfirmMove = { onEvent(HomeUiEvent.ConfirmReorder) },
                    modifier = Modifier
                        .width(cardWidth)
                        .animateItem()
                )
            }

            if (showSystemActions) {
                item(key = "action_media_hub") {
                    MediaHubCard(
                        cardStyle = uiState.cardStyle,
                        onClick = { onEvent(HomeUiEvent.OpenMediaHub) },
                        modifier = Modifier.width(cardWidth)
                    )
                }

                item(key = "action_add_app") {
                    AddAppCyberCard(
                        cardStyle = uiState.cardStyle,
                        onClick = { onEvent(HomeUiEvent.OpenAddDialog) },
                        modifier = Modifier.width(cardWidth)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        color = CyberCyan.copy(alpha = 0.85f),
        fontFamily = ShareTechMonoFontFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = modifier.padding(start = 48.dp, bottom = 8.dp)
    )
}