package com.launcher.samiboxtv.presentation.components

import android.view.KeyEvent
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import com.launcher.samiboxtv.util.AppActionsHelper
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily


@Composable
fun AppContextSideDrawer(
    appItem: AppItem,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onHide: () -> Unit,
    onStartReorder: (AppItem) -> Unit,
    onMoveCategory: () -> Unit = {}
) {
    val context = LocalContext.current
    val techFont = ShareTechMonoFontFamily
    val initialFocusRequester = remember { FocusRequester() }
    var isInteractionEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            initialFocusRequester.requestFocus()
        } catch (_: Exception) {}
        // Retardo para absorber el release del botón OK tras long-press
        delay(350)
        isInteractionEnabled = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.CenterEnd
        ) {
            // Contenedor lateral derecho (Ancho de 400dp)
            Box(
                modifier = Modifier
                    .width(400.dp)
                    .fillMaxHeight()
                    .clickable(enabled = false) {} // Bloquea clics al fondo
                    .background(Color(0xFF070B16))
                    .border(
                        width = 1.5.dp,
                        color = CyberCyan.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .padding(20.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Cabecera con datos de la app
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = appItem.iconDrawable),
                                contentDescription = appItem.name,
                                modifier = Modifier.size(46.dp)
                            )
                            Column {
                                Text(
                                    text = appItem.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontFamily = techFont,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = appItem.packageName,
                                    color = CyberGrey,
                                    fontSize = 9.sp,
                                    fontFamily = techFont
                                )
                            }
                        }
                    }

                    // 1. Sección: Organización y Launcher (Foco prioritario)
                    item { DrawerSectionHeader("ORGANIZACIÓN EN EL LAUNCHER") }

                    item {
                        DrawerActionItem(
                            title = "REORDENAR",
                            subtitle = "Cambiar de posición en la fila",
                            symbol = "⇄",
                            accentColor = CyberMagenta,
                            enabled = isInteractionEnabled,
                            modifier = Modifier.focusRequester(initialFocusRequester),
                            onClick = {
                                onDismiss()
                                onStartReorder(appItem)
                            }
                        )
                    }

                    item {
                        DrawerActionItem(
                            title = if (appItem.isFavorite) "QUITAR DE FAVORITOS" else "AÑADIR A FAVORITOS",
                            subtitle = "Fila superior destacada",
                            symbol = "★",
                            accentColor = CyberAmber,
                            enabled = isInteractionEnabled,
                            onClick = {
                                onToggleFavorite()
                                onDismiss()
                            }
                        )
                    }

                    item {
                        DrawerActionItem(
                            title = "MOVER DE CATEGORÍA",
                            subtitle = "Cambiar a Streaming, Apps, Juegos...",
                            symbol = "☷",
                            accentColor = CyberCyan,
                            enabled = isInteractionEnabled,
                            onClick = {
                                onMoveCategory()
                                onDismiss()
                            }
                        )
                    }

                    item {
                        DrawerActionItem(
                            title = "OCULTAR DEL LAUNCHER",
                            subtitle = "No mostrar en la pantalla principal",
                            symbol = "⊘",
                            accentColor = Color(0xFF6B8BAA),
                            enabled = isInteractionEnabled,
                            onClick = {
                                onHide()
                                onDismiss()
                            }
                        )
                    }

                    // 2. Sección: Sistema & Gestión
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        DrawerSectionHeader("SISTEMA Y GESTIÓN")
                    }

                    item {
                        DrawerActionItem(
                            title = "AJUSTES DE LA APLICACIÓN",
                            subtitle = "Forzar detención, memoria y permisos",
                            symbol = "⚙",
                            accentColor = CyberCyan,
                            enabled = isInteractionEnabled,
                            onClick = {
                                AppActionsHelper.openAppSettings(context, appItem.packageName)
                                onDismiss()
                            }
                        )
                    }

                    item {
                        DrawerActionItem(
                            title = "DESINSTALAR",
                            subtitle = "Eliminar aplicación del TV",
                            symbol = "✕",
                            accentColor = CyberMagenta,
                            enabled = isInteractionEnabled,
                            onClick = {
                                AppActionsHelper.launchUninstallApp(context, appItem.packageName)
                                onDismiss()
                            }
                        )
                    }

                    item {
                        DrawerActionItem(
                            title = "ABRIR APLICACIÓN",
                            subtitle = "Ejecutar en primer plano",
                            symbol = "▶",
                            accentColor = CyberCyan,
                            enabled = isInteractionEnabled,
                            onClick = {
                                onLaunch()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        color = CyberAmber,
        fontFamily = ShareTechMonoFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun DrawerActionItem(
    title: String,
    subtitle: String,
    symbol: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isFocused) Color(0xFF152238) else Color(0xFF0C1322))
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) accentColor else CyberCyan.copy(alpha = 0.2f),
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                val isConfirmKey = keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                        keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                        keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER

                if (isConfirmKey) {
                    if (keyEvent.type == KeyEventType.KeyDown && enabled) {
                        onClick()
                    }
                    // Consumir el evento (tanto KeyDown como KeyUp) para evitar que rebotes activen acciones
                    true
                } else false
            }
            .focusable()
            .clickable(enabled = enabled) {
                if (enabled) onClick()
            }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = symbol,
                color = if (isFocused) accentColor else CyberGrey,
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Column {
                Text(
                    text = title,
                    color = if (isFocused) accentColor else Color.White,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF7E9BB8),
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.sp
                )
            }
        }
    }
}