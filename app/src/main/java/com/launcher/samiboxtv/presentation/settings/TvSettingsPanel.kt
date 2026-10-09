package com.launcher.samiboxtv.presentation.settings

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.tv.material3.Text
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.SettingsSection
import com.launcher.samiboxtv.presentation.components.AssignCategoryDialog
import com.launcher.samiboxtv.presentation.components.CyberQrCard
import com.launcher.samiboxtv.presentation.home.HomeUiEvent
import com.launcher.samiboxtv.presentation.home.HomeUiState
import com.launcher.samiboxtv.presentation.settings.components.SettingsActionItem
import com.launcher.samiboxtv.presentation.settings.components.SettingsMenuTabItem
import com.launcher.samiboxtv.presentation.settings.dialogs.AddCategoryDialog
import com.launcher.samiboxtv.presentation.settings.dialogs.DefaultLauncherDialog
import com.launcher.samiboxtv.presentation.settings.sections.*
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import com.launcher.samiboxtv.util.DevLogManager
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Director / Orquestador principal del panel de ajustes de Android TV
 */
@Composable
fun TvSettingsPanel(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    val context = LocalContext.current
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedAppForCategoryChange by remember { mutableStateOf<AppItem?>(null) }
    var showDefaultLauncherDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    val activeSectionFocusRequester = remember { FocusRequester() }
    var wasQrOpen by remember { mutableStateOf(false) }
    val qrButtonFocusRequester = remember { FocusRequester()}

    BackHandler(enabled = uiState.isSettingsOpen) {
        onEvent(HomeUiEvent.CloseSettings)
    }

    LaunchedEffect(Unit) {
        delay(50.milliseconds)
        try { activeSectionFocusRequester.requestFocus() } catch (_: Exception) {}
    }
    LaunchedEffect(showQrDialog) {
        if (wasQrOpen && !showQrDialog) {
            delay(80.milliseconds)
            try {
                qrButtonFocusRequester.requestFocus()
            } catch (_: Exception) {
                delay(100.milliseconds)
                try { qrButtonFocusRequester.requestFocus() } catch (_: Exception) {}
            }
        }
        wasQrOpen = showQrDialog
    }

    Dialog(
        onDismissRequest = { onEvent(HomeUiEvent.CloseSettings) },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onEvent(HomeUiEvent.CloseSettings) },
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                modifier = Modifier
                    .width(680.dp)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
                    .background(Color(0xFF090E1B))
                    .border(
                        width = 1.5.dp,
                        color = CyberCyan.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyUp &&
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_BACK
                        ) {
                            onEvent(HomeUiEvent.CloseSettings)
                            true
                        } else false
                    }
            ) {
                Column(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF060913))
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "AJUSTES NEON OS TV",
                        color = CyberCyan,
                        fontSize = 18.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = stringResource(R.string.header_app_title),
                        color = CyberGrey,
                        fontSize = 10.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val visibleSettingsSections = remember {
                        listOf(
                            SettingsSection.CATEGORIES,
                            SettingsSection.APP_STYLE,
                            SettingsSection.IPTV,
                            SettingsSection.SYSTEM,
                            SettingsSection.DEVELOPER
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = visibleSettingsSections, key = { it.name }) { section ->
                            val effectiveSection = if (uiState.activeSettingsSection == SettingsSection.FAVORITES) {
                                SettingsSection.CATEGORIES
                            } else {
                                uiState.activeSettingsSection
                            }
                            val isSelected = effectiveSection == section
                            SettingsMenuTabItem(
                                section = section,
                                isSelected = isSelected,
                                onSelect = { onEvent(HomeUiEvent.SelectSettingsSection(section)) },
                                modifier = if (isSelected) Modifier.focusRequester(activeSectionFocusRequester) else Modifier
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsActionItem(
                        title = "SALIR DE AJUSTES",
                        subtitle = "Volver a la pantalla principal",
                        isHighlighted = false,
                        iconRes = R.drawable.arrow_back,
                        onClick = { onEvent(HomeUiEvent.CloseSettings) }
                    )
                }


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    val effectiveSection = if (uiState.activeSettingsSection == SettingsSection.FAVORITES) {
                        SettingsSection.CATEGORIES
                    } else {
                        uiState.activeSettingsSection
                    }

                    Text(
                        text = effectiveSection.title,
                        color = CyberCyan,
                        fontSize = 16.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = effectiveSection.subtitle,
                        color = Color(0xFF7E9BB8),
                        fontSize = 11.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (effectiveSection) {
                        SettingsSection.FAVORITES,
                        SettingsSection.CATEGORIES -> {
                            CategoriesSettingsSection(
                                uiState = uiState,
                                onEvent = onEvent,
                                onAddCategory = { showAddCategoryDialog = true },
                                onSelectAppForCategory = { selectedAppForCategoryChange = it }
                            )
                        }
                        SettingsSection.APP_STYLE -> {
                            AppStyleSettingsSection(
                                currentLayoutMode = uiState.appLayoutMode,
                                currentStyle = uiState.cardStyle,
                                showAppNames = uiState.showAppNames,
                                onEvent = onEvent
                            )
                        }
                        SettingsSection.IPTV -> {
                            IptvSettingsSection(
                                uiState = uiState,
                                context = context,
                                qrButtonFocusRequester = qrButtonFocusRequester,
                                onEvent = onEvent,
                                onOpenQrDialog = { showQrDialog = true }
                            )
                        }
                        SettingsSection.SYSTEM -> {
                            SystemSettingsSection(
                                uiState = uiState,
                                context = context,
                                onEvent = onEvent,
                                onOpenDefaultLauncherDialog = { showDefaultLauncherDialog = true }
                            )
                        }
                        SettingsSection.DEVELOPER -> {
                            DeveloperSettingsSection(
                                uiState = uiState,
                                onEvent = onEvent
                            )
                        }
                    }
                }
            }
        }

        // OVERLAY DEL CÓDIGO QR: 100% SÓLIDO (Oculta la configuración completamente)
        if (showQrDialog) {
            val serverUrl = uiState.logServerUrl.ifBlank {
                "http://${DevLogManager.getLocalIpAddress()}:${DevLogManager.SERVER_PORT}"
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(200f)
                    .background(Color(0xFF040711)) // Negro Cyberpunk sólido, sin transparencias
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                contentAlignment = Alignment.Center
            ) {
                val closeBtnFocusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    delay(100.milliseconds)
                    try { closeBtnFocusRequester.requestFocus() } catch (_: Exception) {}
                }

                BackHandler { showQrDialog = false }

                CyberQrCard(
                    serverUrl = serverUrl,
                    closeFocusRequester = closeBtnFocusRequester,
                    onClose = { showQrDialog = false }
                )
            }
        }
    }



    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name ->
                onEvent(HomeUiEvent.CreateCategory(name))
                showAddCategoryDialog = false
            }
        )
    }

    selectedAppForCategoryChange?.let { app ->
        AssignCategoryDialog(
            app = app,
            categories = uiState.categories,
            currentCategory = uiState.appCategoryMap[app.packageName] ?: app.category.ifBlank { "APPS" },
            onDismiss = { selectedAppForCategoryChange = null },
            onSelectCategory = { newCategory ->
                onEvent(HomeUiEvent.AssignCategory(app.packageName, newCategory))
                selectedAppForCategoryChange = null
            }
        )
    }

    if (showDefaultLauncherDialog) {
        DefaultLauncherDialog(
            context = context,
            onDismiss = { showDefaultLauncherDialog = false }
        )
    }
}