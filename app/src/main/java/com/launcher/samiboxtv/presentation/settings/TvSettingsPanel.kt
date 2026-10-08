package com.launcher.samiboxtv.presentation.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.domain.model.AppCardStyle
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.SettingsSection
import com.launcher.samiboxtv.presentation.home.HomeUiEvent
import com.launcher.samiboxtv.presentation.home.HomeUiState
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

/**
 * Panel de Ajustes y Configuración estilo Android TV / Google TV.
 * Se presenta como un panel lateral dividido a dos columnas (Secciones a la izquierda, opciones a la derecha).
 */
@Composable
fun TvSettingsPanel(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedAppForCategoryChange by remember { mutableStateOf<AppItem?>(null) }
    val initialFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        initialFocusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = { onEvent(HomeUiEvent.CloseSettings) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
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
            // Contenedor principal del panel lateral de TV (680dp de ancho)
            Row(
                modifier = Modifier
                    .width(680.dp)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consumir eventos táctiles dentro del panel */ }
                    .background(Color(0xFF090E1B))
                    .border(
                        width = 1.5.dp,
                        color = CyberCyan.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyUp &&
                            keyEvent.key.nativeKeyCode == android.view.KeyEvent.KEYCODE_BACK
                        ) {
                            onEvent(HomeUiEvent.CloseSettings)
                            true
                        } else false
                    }
            ) {
                // COLUMNA IZQUIERDA: MENÚ DE SECCIONES (240dp)
                Column(
                    modifier = Modifier
                        .width(240.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF060913))
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "AJUSTES // TV",
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

                    SettingsSection.entries.forEachIndexed { index, section ->
                        val isSelected = uiState.activeSettingsSection == section
                        SettingsMenuTabItem(
                            section = section,
                            isSelected = isSelected,
                            onSelect = { onEvent(HomeUiEvent.SelectSettingsSection(section)) },
                            modifier = if (index == 0) Modifier.focusRequester(initialFocusRequester) else Modifier
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botón para cerrar ajustes
                    SettingsActionItem(
                        title = "← SALIR DE AJUSTES",
                        subtitle = "Volver a la pantalla principal",
                        isHighlighted = false,
                        onClick = { onEvent(HomeUiEvent.CloseSettings) }
                    )
                }

                // COLUMNA DERECHA: CONTENIDO Y OPCIONES DE LA SECCIÓN ACTIVA (440dp)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = uiState.activeSettingsSection.title,
                        color = CyberCyan,
                        fontSize = 16.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.activeSettingsSection.subtitle,
                        color = Color(0xFF7E9BB8),
                        fontSize = 11.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (uiState.activeSettingsSection) {
                        SettingsSection.FAVORITES -> {
                            FavoritesSettingsContent(
                                allApps = uiState.allInstalledApps,
                                onToggleFavorite = { onEvent(HomeUiEvent.ToggleAppFavorite(it)) }
                            )
                        }
                        SettingsSection.CATEGORIES -> {
                            CategoriesSettingsContent(
                                categories = uiState.categories,
                                allApps = uiState.allInstalledApps,
                                appCategoryMap = uiState.appCategoryMap,
                                onAddCategory = { showAddCategoryDialog = true },
                                onRemoveCategory = { onEvent(HomeUiEvent.RemoveCategory(it)) },
                                onSelectAppForCategory = { selectedAppForCategoryChange = it }
                            )
                        }
                        SettingsSection.APP_STYLE -> {
                            AppStyleSettingsContent(
                                currentStyle = uiState.cardStyle,
                                onSelectStyle = { onEvent(HomeUiEvent.ChangeCardStyle(it)) }
                            )
                        }
                        SettingsSection.SYSTEM -> {
                            SystemSettingsContent(
                                context = context,
                                isHudOverlayVisible = uiState.isHudOverlayVisible,
                                onOpenTelemetry = {
                                    onEvent(HomeUiEvent.CloseSettings)
                                    onEvent(HomeUiEvent.OpenSystemLog)
                                },
                                onCheckUpdates = { onEvent(HomeUiEvent.CheckUpdates) },
                                onToggleHudOverlay = {
                                    onEvent(HomeUiEvent.ToggleHudOverlay)
                                    onEvent(HomeUiEvent.CloseSettings)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo para crear una nueva categoría
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name ->
                onEvent(HomeUiEvent.CreateCategory(name))
                showAddCategoryDialog = false
            }
        )
    }

    // Diálogo para asignar app a categoría
    selectedAppForCategoryChange?.let { app ->
        AssignCategoryDialog(
            app = app,
            categories = uiState.categories,
            currentCategory = uiState.appCategoryMap[app.packageName] ?: app.category,
            onDismiss = { selectedAppForCategoryChange = null },
            onSelectCategory = { newCategory ->
                onEvent(HomeUiEvent.AssignCategory(app.packageName, newCategory))
                selectedAppForCategoryChange = null
            }
        )
    }
}

@Composable
private fun SettingsMenuTabItem(
    section: SettingsSection,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    val borderColor = when {
        isFocused -> CyberAmber
        isSelected -> CyberCyan
        else -> Color.Transparent
    }
    val containerBg = when {
        isFocused -> Color(0xFF1B2B4C)
        isSelected -> Color(0xFF101B33)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerBg)
            .border(width = if (isFocused || isSelected) 1.5.dp else 0.dp, color = borderColor, shape = shape)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) {
                    onSelect()
                }
            }
            .focusable()
            .clickable { onSelect() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = section.symbol,
                color = if (isSelected || isFocused) CyberCyan else CyberGrey,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = section.title,
                color = when {
                    isFocused -> CyberAmber
                    isSelected -> Color.White
                    else -> Color(0xFFA6C5E2)
                },
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun FavoritesSettingsContent(
    allApps: List<AppItem>,
    onToggleFavorite: (AppItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = allApps, key = { it.packageName }) { app ->
            var isFocused by remember { mutableStateOf(false) }
            val shape = RoundedCornerShape(8.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (isFocused) Color(0xFF172545) else Color(0xFF0C1425))
                    .border(
                        width = if (isFocused) 1.5.dp else 1.dp,
                        color = if (isFocused) CyberCyan else CyberCyan.copy(alpha = 0.2f),
                        shape = shape
                    )
                    .onFocusChanged { isFocused = it.isFocused }
                    .tvClickable { onToggleFavorite(app) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        AsyncImage(
                            model = app.iconDrawable,
                            contentDescription = app.name,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = app.name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = ShareTechMonoFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = app.packageName,
                                color = Color(0xFF6B8BAA),
                                fontSize = 9.sp,
                                fontFamily = ShareTechMonoFontFamily
                            )
                        }
                    }

                    Text(
                        text = if (app.isFavorite) "★ DESTACADA" else "○ NORMAL",
                        color = if (app.isFavorite) CyberAmber else CyberGrey,
                        fontSize = 11.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesSettingsContent(
    categories: List<String>,
    allApps: List<AppItem>,
    appCategoryMap: Map<String, String>,
    onAddCategory: () -> Unit,
    onRemoveCategory: (String) -> Unit,
    onSelectAppForCategory: (AppItem) -> Unit
) {
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    categoryToDelete?.let { catName ->
        ConfirmDeleteCategoryDialog(
            categoryName = catName,
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                onRemoveCategory(catName)
                categoryToDelete = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Botón para crear nueva categoría
            SettingsActionItem(
                title = "+ CREAR NUEVA FILA / CATEGORÍA",
                subtitle = "Agrega una nueva sección temática para organizar tus apps",
                isHighlighted = false,
                onClick = onAddCategory
            )
        }

        item {
            Text(
                text = "FILAS ACTIVAS EN LA PANTALLA PRINCIPAL:",
                color = CyberCyan,
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(items = categories) { categoryName ->
            var isFocused by remember { mutableStateOf(false) }
            val isDefault = categoryName in listOf("STREAMING", "GAMING", "APPS")
            val shape = RoundedCornerShape(8.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (isFocused) Color(0xFF1A2644) else Color(0xFF0F172B))
                    .border(
                        width = if (isFocused) 1.5.dp else 1.dp,
                        color = if (isFocused) (if (!isDefault) CyberMagenta else CyberAmber) else CyberCyan.copy(alpha = 0.25f),
                        shape = shape
                    )
                    .onFocusChanged { isFocused = it.isFocused }
                    .then(
                        if (!isDefault) {
                            Modifier.tvClickable { categoryToDelete = categoryName }
                        } else {
                            Modifier.focusable()
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "☷ $categoryName",
                        color = if (isFocused) (if (!isDefault) CyberMagenta else CyberAmber) else Color.White,
                        fontSize = 12.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isDefault) {
                        Text(
                            text = if (isFocused) "✕ PULSA OK PARA ELIMINAR" else "[ELIMINAR ✕]",
                            color = CyberMagenta,
                            fontSize = 10.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "[SISTEMA]",
                            color = CyberGrey,
                            fontSize = 10.sp,
                            fontFamily = ShareTechMonoFontFamily
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "ASIGNAR APPS A CATEGORÍAS (PULSA PARA CAMBIAR):",
                color = CyberCyan,
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
        }

        items(items = allApps, key = { it.packageName }) { app ->
            val assignedCat = appCategoryMap[app.packageName] ?: app.category
            val isFav = app.isFavorite
            var isFocused by remember { mutableStateOf(false) }
            val shape = RoundedCornerShape(8.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (isFocused) Color(0xFF172545) else (if (isFav) Color(0xFF0F1522) else Color(0xFF0C1425)))
                    .border(
                        width = if (isFocused) 1.5.dp else 1.dp,
                        color = if (isFocused) CyberAmber else (if (isFav) CyberGrey.copy(alpha = 0.25f) else CyberCyan.copy(alpha = 0.2f)),
                        shape = shape
                    )
                    .onFocusChanged { isFocused = it.isFocused }
                    .tvClickable {
                        if (!isFav) {
                            onSelectAppForCategory(app)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        AsyncImage(
                            model = app.iconDrawable,
                            contentDescription = app.name,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = app.name,
                                color = if (isFav) Color.LightGray else Color.White,
                                fontSize = 11.sp,
                                fontFamily = ShareTechMonoFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isFav) {
                                Text(
                                    text = "★ Exclusivo de Favoritos",
                                    color = CyberAmber.copy(alpha = 0.75f),
                                    fontSize = 9.sp,
                                    fontFamily = ShareTechMonoFontFamily
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isFav) "★ EN FAVORITOS" else "FILA: $assignedCat ▸",
                        color = if (isFav) CyberAmber else CyberCyan,
                        fontSize = 10.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AppStyleSettingsContent(
    currentStyle: AppCardStyle,
    onSelectStyle: (AppCardStyle) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AppCardStyle.entries.forEach { style ->
            val isSelected = currentStyle == style
            var isFocused by remember { mutableStateOf(false) }
            val shape = RoundedCornerShape(10.dp)

            val borderColor = when {
                isFocused -> CyberCyan
                isSelected -> CyberAmber
                else -> CyberCyan.copy(alpha = 0.25f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (isFocused) Color(0xFF14203B) else Color(0xFF0C1425))
                    .border(width = if (isFocused || isSelected) 2.dp else 1.dp, color = borderColor, shape = shape)
                    .onFocusChanged { isFocused = it.isFocused }
                    .tvClickable { onSelectStyle(style) }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = style.title,
                            color = if (isFocused) CyberCyan else Color.White,
                            fontSize = 13.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = style.description,
                            color = Color(0xFFA6C5E2),
                            fontSize = 10.sp,
                            fontFamily = ShareTechMonoFontFamily
                        )
                    }

                    Text(
                        text = if (isSelected) "● ACTIVO" else "○ SELECCIONAR",
                        color = if (isSelected) CyberAmber else CyberGrey,
                        fontSize = 11.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemSettingsContent(
    context: Context,
    isHudOverlayVisible: Boolean,
    onOpenTelemetry: () -> Unit,
    onCheckUpdates: () -> Unit,
    onToggleHudOverlay: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsActionItem(
            title = "⌂ ESTABLECER COMO LAUNCHER PREDETERMINADO",
            subtitle = "Abre los ajustes de Android para que la casita siempre abra SamiBox TV",
            isHighlighted = false,
            onClick = {
                try {
                    val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallback)
                }
            }
        )

        SettingsActionItem(
            title = "⚙ AJUSTES GENERALES DEL SISTEMA ANDROID",
            subtitle = "Abrir el panel de configuración de red, pantalla y bluetooth de la TV",
            isHighlighted = false,
            onClick = {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        )

        SettingsActionItem(
            title = "⚡ TELEMETRÍA Y LIMPIADOR DE RAM",
            subtitle = "Inspeccionar procesos de hardware y forzar liberación de memoria física",
            isHighlighted = false,
            onClick = onOpenTelemetry
        )

        SettingsActionItem(
            title = "⬆ BUSCAR ACTUALIZACIONES DE SAMIBOX TV",
            subtitle = "Verificar si hay una nueva versión disponible en GitHub Releases",
            isHighlighted = false,
            onClick = onCheckUpdates
        )
        SettingsActionItem(
            title = if (isHudOverlayVisible) "🎮 HUD OVERLAY // FPS EN VIVO [ACTIVADO]" else "🎮 HUD OVERLAY // FPS EN VIVO [DESACTIVADO]",
            subtitle = if (isHudOverlayVisible) "Contador de fotogramas por segundo (FPS) en vivo en pantalla. Clic para ocultar" else "Contador de fotogramas por segundo (FPS) en vivo en pantalla. Clic para mostrar",
            isHighlighted = isHudOverlayVisible,
            onClick = onToggleHudOverlay
        )
    }
}

@Composable
private fun SettingsActionItem(
    title: String,
    subtitle: String,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    val borderColor = when {
        isFocused -> CyberAmber
        isHighlighted -> CyberCyan
        else -> CyberCyan.copy(alpha = 0.25f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isFocused) Color(0xFF1B2848) else Color(0xFF0F172B))
            .border(width = if (isFocused) 2.dp else 1.dp, color = borderColor, shape = shape)
            .onFocusChanged { isFocused = it.isFocused }
            .tvClickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = title,
                color = if (isFocused) CyberAmber else (if (isHighlighted) CyberCyan else Color.White),
                fontSize = 12.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color(0xFF7E9BB8),
                fontSize = 9.sp,
                fontFamily = ShareTechMonoFontFamily
            )
        }
    }
}

@Composable
private fun ConfirmDeleteCategoryDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(440.dp)
                    .background(Color(0xFF090E1B), RoundedCornerShape(14.dp))
                    .border(1.5.dp, CyberMagenta, RoundedCornerShape(14.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "ELIMINAR FILA // CATEGORÍA",
                        color = CyberMagenta,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¿Deseas eliminar la categoría \"$categoryName\"?",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Las apps asignadas a esta categoría volverán a su sección predeterminada.",
                        color = Color(0xFFA6C5E2),
                        fontSize = 11.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        var cancelFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (cancelFocused) Color(0xFF1F2B48) else Color(0xFF141C30))
                                .border(
                                    1.dp,
                                    if (cancelFocused) CyberAmber else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .onFocusChanged { cancelFocused = it.isFocused }
                                .tvClickable { onDismiss() }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "CANCELAR",
                                color = if (cancelFocused) CyberAmber else CyberGrey,
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        var deleteFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (deleteFocused) CyberMagenta else Color(0xFF4A1020))
                                .border(
                                    1.dp,
                                    if (deleteFocused) CyberAmber else CyberMagenta.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .onFocusChanged { deleteFocused = it.isFocused }
                                .tvClickable { onConfirm() }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "ELIMINAR",
                                color = Color.White,
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var isInputFocused by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(460.dp)
                .background(Color(0xFF090E1B), RoundedCornerShape(14.dp))
                .border(1.5.dp, CyberCyan, RoundedCornerShape(14.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "NUEVA CATEGORÍA // FILA",
                    color = CyberCyan,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Escribe el nombre de la nueva fila temática:",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = ShareTechMonoFontFamily
                )
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172B))
                        .border(
                            width = 1.dp,
                            color = if (isInputFocused) CyberAmber else CyberCyan.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    if (categoryName.isEmpty()) {
                        Text(
                            text = "Ej: JUEGOS, IPTV, MUSICA",
                            color = Color(0xFF6B7E96),
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 13.sp
                        )
                    }
                    BasicTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isInputFocused = it.isFocused },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(CyberCyan),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (categoryName.isNotBlank()) onConfirm(categoryName.trim())
                        })
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    var cancelFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (cancelFocused) Color(0xFF1F2B48) else Color(0xFF141C30))
                            .border(
                                1.dp,
                                if (cancelFocused) CyberAmber else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .onFocusChanged { cancelFocused = it.isFocused }
                            .tvClickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "CANCELAR",
                            color = if (cancelFocused) CyberAmber else CyberGrey,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    var confirmFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (confirmFocused) CyberCyan else Color(0xFF003852))
                            .border(
                                1.dp,
                                if (confirmFocused) CyberAmber else CyberCyan,
                                RoundedCornerShape(8.dp)
                            )
                            .onFocusChanged { confirmFocused = it.isFocused }
                            .tvClickable {
                                if (categoryName.isNotBlank()) onConfirm(categoryName.trim())
                            }
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "CREAR",
                            color = if (confirmFocused) Color.Black else CyberCyan,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignCategoryDialog(
    app: AppItem,
    categories: List<String>,
    currentCategory: String,
    onDismiss: () -> Unit,
    onSelectCategory: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(480.dp)
                .background(Color(0xFF090E1B), RoundedCornerShape(14.dp))
                .border(1.5.dp, CyberCyan, RoundedCornerShape(14.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "MOVER: ${app.name}",
                    color = CyberCyan,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selecciona la fila donde deseas mostrar esta aplicación:",
                    color = Color(0xFFA6C5E2),
                    fontSize = 11.sp,
                    fontFamily = ShareTechMonoFontFamily
                )
                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isCurrent = cat.equals(currentCategory, ignoreCase = true)
                        var isFocused by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isFocused) Color(0xFF1B2B4C) else Color(0xFF121B30))
                                .border(
                                    width = if (isFocused) 1.5.dp else 1.dp,
                                    color = if (isFocused) CyberAmber else (if (isCurrent) CyberCyan else Color.Transparent),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .onFocusChanged { isFocused = it.isFocused }
                                .tvClickable { onSelectCategory(cat) }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isCurrent) CyberCyan else Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = ShareTechMonoFontFamily,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isCurrent) {
                                    Text(
                                        text = "✓ ACTUAL",
                                        color = CyberAmber,
                                        fontSize = 10.sp,
                                        fontFamily = ShareTechMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    var closeFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (closeFocused) Color(0xFF1F2B48) else Color(0xFF141C30))
                            .border(
                                1.dp,
                                if (closeFocused) CyberAmber else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .onFocusChanged { closeFocused = it.isFocused }
                            .tvClickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "CERRAR",
                            color = if (closeFocused) CyberAmber else CyberGrey,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modificador para garantizar soporte completo de D-Pad Center y ENTER en Android TV.
 */
private fun Modifier.tvClickable(onClick: () -> Unit): Modifier = this
    .onKeyEvent { keyEvent ->
        if (keyEvent.type == KeyEventType.KeyDown &&
            (keyEvent.key.nativeKeyCode == android.view.KeyEvent.KEYCODE_DPAD_CENTER ||
             keyEvent.key.nativeKeyCode == android.view.KeyEvent.KEYCODE_ENTER ||
             keyEvent.key.nativeKeyCode == android.view.KeyEvent.KEYCODE_NUMPAD_ENTER)
        ) {
            onClick()
            true
        } else false
    }
    .focusable()
    .clickable { onClick() }
