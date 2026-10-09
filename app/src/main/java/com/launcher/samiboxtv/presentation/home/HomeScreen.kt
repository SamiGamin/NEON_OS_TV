package com.launcher.samiboxtv.presentation.home

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.VirtualApps
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
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberDarkBg
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import com.launcher.samiboxtv.presentation.theme.rememberTvLayoutDimensions
import com.launcher.samiboxtv.util.CategoryHelper
import kotlinx.coroutines.delay

private fun getDrawableResId(context: Context, vararg names: String): Int {
    val res = context.resources
    val pkg = context.packageName
    for (name in names) {
        val id = res.getIdentifier(name, "drawable", pkg)
        if (id != 0) return id
    }
    return 0
}

private val ManageAppsVirtualApp = AppItem(
    packageName = VirtualApps.PKG_MANAGE_APPS,
    name = "+ GESTIONAR APPS",
    category = "CONFIG",
    isSystemApp = true
)

private fun resolveAppBackdropModel(context: Context, app: AppItem?): Any? {
    if (app == null) return null
    if (app.packageName == VirtualApps.PKG_MANAGE_APPS ||
        app.name.contains("GESTIONAR", ignoreCase = true)
    ) {
        val bannerRes = getDrawableResId(context, "baner_app", "banner_app", "bamer_app")
        if (bannerRes != 0) return bannerRes
    }
    if (app.packageName == VirtualApps.PKG_IPTV ||
        app.packageName.contains("iptv", ignoreCase = true) ||
        app.name.contains("IPTV", ignoreCase = true)
    ) {
        val bannerRes = getDrawableResId(context, "banner_live_tv", "banner_iptv")
        if (bannerRes != 0) return bannerRes
    }
    if (app.packageName == VirtualApps.PKG_MEDIA_HUB ||
        app.name.contains("MEDIA HUB", ignoreCase = true)
    ) {
        val bannerRes = getDrawableResId(context, "banner_media")
        if (bannerRes != 0) return bannerRes
    }
    return app.bannerDrawable ?: app.iconDrawable
}

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
    val initialFocusRequester = remember { FocusRequester() }

    // Solicitar foco automático en el primer elemento de la pantalla principal
    LaunchedEffect(
        uiState.isSettingsOpen,
        uiState.isIptvPlayerOpen,
        uiState.currentPlayingMedia,
        uiState.isMediaHubOpen,
        uiState.isSystemLogOpen,
        uiState.selectedAppForMenu,
        uiState.isAddDialogOpen
    ) {
        if (!uiState.isSettingsOpen &&
            !uiState.isIptvPlayerOpen &&
            uiState.currentPlayingMedia == null &&
            !uiState.isMediaHubOpen &&
            !uiState.isSystemLogOpen &&
            uiState.selectedAppForMenu == null &&
            !uiState.isAddDialogOpen
        ) {
            delay(150)
            try {
                initialFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

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

    Box(modifier = modifier.fillMaxSize().background(CyberDarkBg)) {
        HomeScreenContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            initialFocusRequester = initialFocusRequester,
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
    modifier: Modifier = Modifier,
    initialFocusRequester: FocusRequester? = null
) {
    val context = LocalContext.current
    val dimensions = rememberTvLayoutDimensions(layoutMode = uiState.appLayoutMode)

    var focusedApp by remember { mutableStateOf<AppItem?>(null) }
    var activeBackdropApp by remember { mutableStateOf<AppItem?>(null) }

    LaunchedEffect(focusedApp) {
        delay(180)
        activeBackdropApp = focusedApp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
    ) {
        // Capa 0: Fondo ambiental dinámico con Crossfade y debounce
        Crossfade(
            targetState = activeBackdropApp,
            animationSpec = tween(durationMillis = 350),
            label = "ambientBackdrop"
        ) { app ->
            val backdropModel = remember(app?.packageName) {
                resolveAppBackdropModel(context, app)
            }
            if (backdropModel != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(backdropModel)
                            .crossfade(false)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Máscara degradada oscura para preservar contraste de las tarjetas
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        CyberDarkBg.copy(alpha = 0.40f),
                                        CyberDarkBg.copy(alpha = 0.75f),
                                        CyberDarkBg.copy(alpha = 0.95f),
                                        CyberDarkBg
                                    )
                                )
                            )
                    )
                }
            }
        }

        // Capa 1: Título central holográfico
        val holographicTitle = (activeBackdropApp?.name ?: "NEONOS TV").uppercase()
        Text(
            text = holographicTitle,
            color = CyberCyan.copy(alpha = 0.50f),
            fontSize = 18.sp,
            fontFamily = ShareTechMonoFontFamily,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )

        // Capa 2: UI normal del launcher
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
        // 1. ZONA SUPERIOR HUD MILITAR (Reloj militar HH:mm:ss + Perfil de pantalla + Nodos)
        item(key = "header_hud") {
            CyberHeaderHud(
                layoutMode = uiState.appLayoutMode,
                appCount = uiState.allApps.size,
                networkStatus = uiState.networkStatus,
                onOpenSettings = { onEvent(HomeUiEvent.OpenSettings) }
            )
        }

        if (uiState.appLayoutMode == AppLayoutMode.MODERN_GRID) {
            // ==============================================================
            // MODO CUADRÍCULA MODERNA (3 columnas x 2 filas = 6 en pantalla)
            // ==============================================================
            val favoriteApps = uiState.favoriteApps
            val nonFavoriteApps = uiState.nonFavoriteApps

            if (favoriteApps.isNotEmpty()) {
                item(key = "grid_section_favorites") {
                    CyberModernGrid(
                        title = "★ FAVORITOS & DESTACADOS",
                        apps = favoriteApps,
                        dimensions = dimensions,
                        initialFocusRequester = initialFocusRequester,
                        movingAppPackageName = uiState.movingAppPackageName,
                        showAppName = uiState.showAppNames,
                        onLaunchApp = { onEvent(HomeUiEvent.LaunchApp(it)) },
                        onOpenContextMenu = { onEvent(HomeUiEvent.OpenContextMenu(it)) },
                        onMoveDirection = { onEvent(HomeUiEvent.MoveApp(uiState.movingAppPackageName ?: "", it)) },
                        onConfirmMove = { onEvent(HomeUiEvent.ConfirmReorder) },
                        onAppFocus = { focusedApp = it },
                        trailingCard = if (nonFavoriteApps.isEmpty()) {
                            {
                                AddAppCyberCard(
                                    width = dimensions.cardWidth,
                                    height = dimensions.cardHeight,
                                    cardStyle = uiState.cardStyle,
                                    showAppName = uiState.showAppNames,
                                    onFocus = { focusedApp = ManageAppsVirtualApp },
                                    onClick = { onEvent(HomeUiEvent.OpenAddDialog) }
                                )
                            }
                        } else null
                    )
                }
            }

            if (nonFavoriteApps.isNotEmpty()) {
                item(key = "grid_section_all") {
                    CyberModernGrid(
                        title = if (favoriteApps.isEmpty()) "TODAS LAS APLICACIONES" else "OTRAS APLICACIONES",
                        apps = nonFavoriteApps,
                        dimensions = dimensions,
                        initialFocusRequester = if (favoriteApps.isEmpty()) initialFocusRequester else null,
                        movingAppPackageName = uiState.movingAppPackageName,
                        showAppName = uiState.showAppNames,
                        onLaunchApp = { onEvent(HomeUiEvent.LaunchApp(it)) },
                        onOpenContextMenu = { onEvent(HomeUiEvent.OpenContextMenu(it)) },
                        onMoveDirection = { onEvent(HomeUiEvent.MoveApp(uiState.movingAppPackageName ?: "", it)) },
                        onConfirmMove = { onEvent(HomeUiEvent.ConfirmReorder) },
                        onAppFocus = { focusedApp = it },
                        trailingCard = {
                            AddAppCyberCard(
                                width = dimensions.cardWidth,
                                height = dimensions.cardHeight,
                                cardStyle = uiState.cardStyle,
                                showAppName = uiState.showAppNames,
                                onFocus = { focusedApp = ManageAppsVirtualApp },
                                onClick = { onEvent(HomeUiEvent.OpenAddDialog) }
                            )
                        }
                    )
                }
            }

            if (favoriteApps.isEmpty() && nonFavoriteApps.isEmpty()) {
                item(key = "grid_manage_empty") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensions.horizontalPadding),
                        horizontalArrangement = Arrangement.spacedBy(dimensions.spacing)
                    ) {
                        AddAppCyberCard(
                            width = dimensions.cardWidth,
                            height = dimensions.cardHeight,
                            cardStyle = uiState.cardStyle,
                            showAppName = uiState.showAppNames,
                            onFocus = { focusedApp = ManageAppsVirtualApp },
                            onClick = { onEvent(HomeUiEvent.OpenAddDialog) }
                        )
                    }
                }
            }
        } else {
            // ==============================================================
            // MODOS HORIZONTALES (COMPACT_HIGH_DENSITY, PANORAMIC_16_9, COMPACT_STANDARD, DUAL_PRIORITY)
            // ==============================================================

            // 2. SECCIÓN FAVORITOS (Condición estricta: sólo si favoriteApps.isNotEmpty())
            val favoriteApps = uiState.favoriteApps
            val nonFavoriteApps = uiState.nonFavoriteApps
            val activeCategoriesWithApps = uiState.categories.mapNotNull { categoryName ->
                val apps = nonFavoriteApps.filter { app ->
                    val assigned = uiState.appCategoryMap[app.packageName] ?: app.category.ifBlank { "APPS" }
                    assigned.equals(categoryName, ignoreCase = true)
                }
                if (apps.isNotEmpty()) categoryName to apps else null
            }

            if (favoriteApps.isNotEmpty()) {
                item(key = "section_favorites") {
                    CyberAppCarousel(
                        title = "★ FAVORITOS & DESTACADOS",
                        apps = favoriteApps,
                        dimensions = dimensions,
                        initialFocusRequester = initialFocusRequester,
                        movingAppPackageName = uiState.movingAppPackageName,
                        showAppName = uiState.showAppNames,
                        onLaunchApp = { onEvent(HomeUiEvent.LaunchApp(it)) },
                        onOpenContextMenu = { onEvent(HomeUiEvent.OpenContextMenu(it)) },
                        onMoveDirection = { onEvent(HomeUiEvent.MoveApp(uiState.movingAppPackageName ?: "", it)) },
                        onConfirmMove = { onEvent(HomeUiEvent.ConfirmReorder) },
                        onAppFocus = { focusedApp = it },
                        trailingCard = if (activeCategoriesWithApps.isEmpty() && nonFavoriteApps.isEmpty()) {
                            {
                                AddAppCyberCard(
                                    width = dimensions.cardWidth,
                                    height = dimensions.cardHeight,
                                    cardStyle = uiState.cardStyle,
                                    showAppName = uiState.showAppNames,
                                    onFocus = { focusedApp = ManageAppsVirtualApp },
                                    onClick = { onEvent(HomeUiEvent.OpenAddDialog) }
                                )
                            }
                        } else null
                    )
                }
            }

            // 3. SECCIONES DINÁMICAS POR CATEGORÍA
            activeCategoriesWithApps.forEachIndexed { index, (categoryName, categoryApps) ->
                val isLastCategory = index == activeCategoriesWithApps.lastIndex
                item(key = "section_cat_$categoryName") {
                    CyberAppCarousel(
                        title = "${CategoryHelper.getCategoryIcon(categoryName)} $categoryName",
                        apps = categoryApps,
                        dimensions = dimensions,
                        initialFocusRequester = if (favoriteApps.isEmpty() && index == 0) initialFocusRequester else null,
                        movingAppPackageName = uiState.movingAppPackageName,
                        showAppName = uiState.showAppNames,
                        onLaunchApp = { onEvent(HomeUiEvent.LaunchApp(it)) },
                        onOpenContextMenu = { onEvent(HomeUiEvent.OpenContextMenu(it)) },
                        onMoveDirection = { onEvent(HomeUiEvent.MoveApp(uiState.movingAppPackageName ?: "", it)) },
                        onConfirmMove = { onEvent(HomeUiEvent.ConfirmReorder) },
                        onAppFocus = { focusedApp = it },
                        trailingCard = if (isLastCategory) {
                            {
                                AddAppCyberCard(
                                    width = dimensions.cardWidth,
                                    height = dimensions.cardHeight,
                                    cardStyle = uiState.cardStyle,
                                    showAppName = uiState.showAppNames,
                                    onFocus = { focusedApp = ManageAppsVirtualApp },
                                    onClick = { onEvent(HomeUiEvent.OpenAddDialog) }
                                )
                            }
                        } else null
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
                            initialFocusRequester = initialFocusRequester,
                            movingAppPackageName = uiState.movingAppPackageName,
                            showAppName = uiState.showAppNames,
                            onLaunchApp = { onEvent(HomeUiEvent.LaunchApp(it)) },
                            onOpenContextMenu = { onEvent(HomeUiEvent.OpenContextMenu(it)) },
                            onMoveDirection = { onEvent(HomeUiEvent.MoveApp(uiState.movingAppPackageName ?: "", it)) },
                            onConfirmMove = { onEvent(HomeUiEvent.ConfirmReorder) },
                            onAppFocus = { focusedApp = it },
                            trailingCard = {
                                AddAppCyberCard(
                                    width = dimensions.cardWidth,
                                    height = dimensions.cardHeight,
                                    cardStyle = uiState.cardStyle,
                                    showAppName = uiState.showAppNames,
                                    onFocus = { focusedApp = ManageAppsVirtualApp },
                                    onClick = { onEvent(HomeUiEvent.OpenAddDialog) }
                                )
                            }
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
                                width = dimensions.cardWidth,
                                height = dimensions.cardHeight,
                                cardStyle = uiState.cardStyle,
                                showAppName = uiState.showAppNames,
                                onFocus = { focusedApp = ManageAppsVirtualApp },
                                onClick = { onEvent(HomeUiEvent.OpenAddDialog) }
                            )
                        }
                    }
                }
            }
        }
    }
}
}