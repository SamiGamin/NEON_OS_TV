package com.launcher.samiboxtv.presentation.settings.sections

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.domain.model.VirtualApps
import com.launcher.samiboxtv.presentation.home.HomeUiEvent
import com.launcher.samiboxtv.presentation.home.HomeUiState
import com.launcher.samiboxtv.presentation.settings.components.SettingsActionItem
import com.launcher.samiboxtv.presentation.settings.components.tvClickable
import com.launcher.samiboxtv.presentation.settings.dialogs.ConfirmDeleteCategoryDialog
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import com.launcher.samiboxtv.util.CategoryHelper
import kotlinx.coroutines.delay

@Composable
fun CategoriesSettingsSection(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    onAddCategory: () -> Unit,
    onSelectAppForCategory: (AppItem) -> Unit
) {
    var isManagingFavorites by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }

    if (isManagingFavorites) {
        BackHandler { isManagingFavorites = false }
        val backBtnFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            delay(50)
            try { backBtnFocusRequester.requestFocus() } catch (_: Exception) {}
        }

        Column(modifier = Modifier.fillMaxSize()) {
            SettingsActionItem(
                title = "VOLVER A CATEGORÍAS & FILAS",
                subtitle = "Regresar al listado de categorías",
                isHighlighted = false,
                iconRes = R.drawable.arrow_back,
                modifier = Modifier.focusRequester(backBtnFocusRequester),
                onClick = { isManagingFavorites = false }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "GESTIONAR FAVORITOS (PULSA OK PARA MARCAR / DESMARCAR):",
                color = CyberCyan,
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            FavoritesSettingsContent(
                allApps = uiState.allInstalledApps,
                onToggleFavorite = { onEvent(HomeUiEvent.ToggleAppFavorite(it)) }
            )
        }
        return
    }

    categoryToDelete?.let { catName ->
        ConfirmDeleteCategoryDialog(
            categoryName = catName,
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                onEvent(HomeUiEvent.RemoveCategory(catName))
                categoryToDelete = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "cat_manage_favorites") {
            val favCount = uiState.allInstalledApps.count { it.isFavorite }
            SettingsActionItem(
                title = "GESTIONAR FAVORITOS",
                subtitle = if (favCount == 0) {
                    "Sin apps destacadas (fila oculta en el Home) • Pulsa OK para añadir"
                } else {
                    "$favCount apps destacadas en la fila superior • Pulsa OK para gestionar"
                },
                isHighlighted = favCount > 0,
                iconRes = R.drawable.ic_star,
                onClick = { isManagingFavorites = true }
            )
        }

        item(key = "cat_create_btn") {
            SettingsActionItem(
                title = "CREAR NUEVA FILA / CATEGORÍA",
                subtitle = "Agrega una nueva sección temática para organizar tus apps",
                isHighlighted = false,
                iconRes = R.drawable.ic_categories,
                onClick = onAddCategory
            )
        }

        item(key = "cat_header_active") {
            Text(
                text = "FILAS ACTIVAS EN LA PANTALLA PRINCIPAL:",
                color = CyberCyan,
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(items = uiState.categories, key = { "cat_group_$it" }) { categoryName ->
            var isFocused by remember { mutableStateOf(false) }
            val isDefault = categoryName in listOf("STREAMING", "GAMING", "APPS")
            val shape = RoundedCornerShape(8.dp)
            val icon = CategoryHelper.getCategoryIcon(categoryName)
            val appCount = uiState.allInstalledApps.count { app ->
                val assigned = uiState.appCategoryMap[app.packageName] ?: app.category.ifBlank { "APPS" }
                !app.isFavorite && assigned.equals(categoryName, ignoreCase = true)
            }

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
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = icon, fontSize = 16.sp)
                        Column {
                            Text(
                                text = categoryName,
                                color = if (isFocused) (if (!isDefault) CyberMagenta else CyberAmber) else Color.White,
                                fontSize = 12.sp,
                                fontFamily = ShareTechMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (appCount == 0) "Sin apps asignadas" else "$appCount ${if (appCount == 1) "app activa" else "apps activas"}",
                                color = if (appCount > 0) CyberCyan.copy(alpha = 0.85f) else CyberGrey,
                                fontSize = 9.sp,
                                fontFamily = ShareTechMonoFontFamily
                            )
                        }
                    }

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

        item(key = "cat_header_assign") {
            Text(
                text = "ASIGNAR APPS A CATEGORÍAS (PULSA PARA CAMBIAR):",
                color = CyberCyan,
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
        }

        items(items = uiState.allInstalledApps, key = { "cat_app_${it.packageName}" }) { app ->
            val assignedCat = uiState.appCategoryMap[app.packageName] ?: app.category.ifBlank { "APPS" }
            val isFav = app.isFavorite
            var isFocused by remember { mutableStateOf(false) }
            val shape = RoundedCornerShape(8.dp)
            val assignedIcon = CategoryHelper.getCategoryIcon(assignedCat)

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
                        val iconModel = when (app.packageName) {
                            VirtualApps.PKG_IPTV -> R.drawable.ic_cyber_iptv
                            VirtualApps.PKG_MEDIA_HUB -> R.drawable.ic_cyber_media_hub
                            else -> app.iconDrawable ?: app.bannerDrawable
                        }
                        AsyncImage(
                            model = iconModel,
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
                        text = if (isFav) "★ EN FAVORITOS" else "$assignedIcon $assignedCat ▸",
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
fun FavoritesSettingsContent(
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
                        val iconModel = when (app.packageName) {
                            VirtualApps.PKG_IPTV -> R.drawable.ic_cyber_iptv
                            VirtualApps.PKG_MEDIA_HUB -> R.drawable.ic_cyber_media_hub
                            else -> app.iconDrawable ?: app.bannerDrawable
                        }
                        AsyncImage(
                            model = iconModel,
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