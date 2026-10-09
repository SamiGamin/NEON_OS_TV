package com.launcher.samiboxtv.presentation.settings.dialogs

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import com.launcher.samiboxtv.domain.model.VirtualApps
import com.launcher.samiboxtv.presentation.settings.components.SettingsActionItem
import com.launcher.samiboxtv.presentation.settings.components.tvClickable
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import com.launcher.samiboxtv.util.DefaultLauncherHelper
import com.launcher.samiboxtv.util.M3uFileScanner
import java.io.File

@Composable
fun AddCategoryDialog(
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
                            .border(1.dp, if (cancelFocused) CyberAmber else Color.Transparent, RoundedCornerShape(8.dp))
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
                            .border(1.dp, if (confirmFocused) CyberAmber else CyberCyan, RoundedCornerShape(8.dp))
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
fun ConfirmDeleteCategoryDialog(
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
                                .border(1.dp, if (cancelFocused) CyberAmber else Color.Transparent, RoundedCornerShape(8.dp))
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
                                .border(1.dp, if (deleteFocused) CyberAmber else CyberMagenta.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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
fun DefaultLauncherDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    val isAggressiveActive = remember { DefaultLauncherHelper.isAccessibilityServiceEnabled(context) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(560.dp)
                    .background(Color(0xFF090E1B), RoundedCornerShape(14.dp))
                    .border(1.5.dp, CyberCyan, RoundedCornerShape(14.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONFIGURAR BOTÓN HOME",
                            color = CyberCyan,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isAggressiveActive) CyberCyan.copy(alpha = 0.2f) else CyberAmber.copy(alpha = 0.2f))
                                .border(1.dp, if (isAggressiveActive) CyberCyan else CyberAmber, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAggressiveActive) "ANCLAJE ACTIVO" else "MODO NORMAL",
                                color = if (isAggressiveActive) CyberCyan else CyberAmber,
                                fontSize = 10.sp,
                                fontFamily = ShareTechMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Selecciona una estrategia según las restricciones de tu TV Box (Android 10/11):",
                        color = Color(0xFFA6C5E2),
                        fontSize = 11.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LauncherOptionItem(
                        title = "1. FORZAR SELECTOR DE ANDROID",
                        subtitle = "Invalida la caché del sistema para que Android pregunte qué launcher abrir. Elige SamiBox TV y toca 'SIEMPRE'.",
                        isHighlighted = false,
                        onClick = {
                            DefaultLauncherHelper.resetAndPromptDefaultLauncher(context)
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LauncherOptionItem(
                        title = if (isAggressiveActive) {
                            "2. MODO AGRESIVO (ACCESIBILIDAD) [✓ ACTIVO]"
                        } else {
                            "2. MODO AGRESIVO (ACCESIBILIDAD) [ACTIVAR]"
                        },
                        subtitle = if (isAggressiveActive) {
                            "El servicio ya intercepta el botón HOME y bloquea el launcher de fábrica automáticamente."
                        } else {
                            "Recomendado para Android 10/11 con launcher bloqueado. Abre Ajustes de Accesibilidad para encender SamiBox TV."
                        },
                        isHighlighted = isAggressiveActive,
                        onClick = {
                            DefaultLauncherHelper.openAccessibilitySettings(context)
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LauncherOptionItem(
                        title = "3. AJUSTES DE APPS DEL SISTEMA",
                        subtitle = "Abre la pantalla de ajustes de aplicaciones para cambiar el inicio de Android manualmente.",
                        isHighlighted = false,
                        onClick = {
                            DefaultLauncherHelper.openDefaultAppsSettings(context)
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        var cancelFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (cancelFocused) Color(0xFF1F2B48) else Color(0xFF141C30))
                                .border(1.dp, if (cancelFocused) CyberAmber else Color.Transparent, RoundedCornerShape(8.dp))
                                .onFocusChanged { cancelFocused = it.isFocused }
                                .tvClickable { onDismiss() }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "CERRAR",
                                color = if (cancelFocused) CyberAmber else CyberGrey,
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
private fun LauncherOptionItem(
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
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = if (isHighlighted) CyberCyan.copy(alpha = 0.9f) else Color(0xFF88A8C7),
                fontSize = 10.sp,
                fontFamily = ShareTechMonoFontFamily,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun IptvUrlInputDialog(
    initialUrl: String = VirtualApps.DEFAULT_IPTV_URL,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var urlText by remember { mutableStateOf(initialUrl) }
    var isInputFocused by remember { mutableStateOf(false) }
    var restoreFocused by remember { mutableStateOf(false) }
    var clearFocused by remember { mutableStateOf(false) }
    var cancelFocused by remember { mutableStateOf(false) }
    var confirmFocused by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(540.dp)
                .background(Color(0xFF090E1B), RoundedCornerShape(14.dp))
                .border(1.5.dp, CyberCyan, RoundedCornerShape(14.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "ENLACE DE LISTA M3U // IPTV",
                    color = CyberCyan,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Puedes usar la lista de ejemplo o ingresar tu propio enlace HTTP/HTTPS:",
                    color = Color.White,
                    fontSize = 11.sp,
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
                    if (urlText.isEmpty()) {
                        Text(
                            text = "https://servidor.com/lista.m3u",
                            color = Color(0xFF6B7E96),
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp
                        )
                    }
                    BasicTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isInputFocused = it.isFocused },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 12.sp
                        ),
                        cursorBrush = SolidColor(CyberCyan),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (urlText.isNotBlank()) onConfirm(urlText.trim())
                        })
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (restoreFocused) CyberCyan else Color(0xFF131D33))
                            .border(1.dp, if (restoreFocused) CyberAmber else CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .onFocusChanged { restoreFocused = it.isFocused }
                            .tvClickable { urlText = VirtualApps.DEFAULT_IPTV_URL }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "USAR EJEMPLO",
                            color = if (restoreFocused) Color.Black else CyberCyan,
                            fontSize = 10.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (clearFocused) CyberMagenta else Color(0xFF131D33))
                            .border(1.dp, if (clearFocused) CyberAmber else CyberMagenta.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .onFocusChanged { clearFocused = it.isFocused }
                            .tvClickable { urlText = "" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LIMPIAR CAMPO",
                            color = if (clearFocused) Color.White else CyberMagenta,
                            fontSize = 10.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (cancelFocused) Color(0xFF1B2848) else Color(0xFF0F172B))
                            .border(1.dp, if (cancelFocused) CyberAmber else CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .onFocusChanged { cancelFocused = it.isFocused }
                            .tvClickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CANCELAR",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = ShareTechMonoFontFamily
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (confirmFocused) CyberAmber else CyberCyan)
                            .onFocusChanged { confirmFocused = it.isFocused }
                            .tvClickable {
                                if (urlText.isNotBlank()) onConfirm(urlText.trim())
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CARGAR LISTA",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IptvFilePickerItemDialog(
    context: Context,
    onDismiss: () -> Unit,
    onSelectFile: (File) -> Unit
) {
    val files = remember { M3uFileScanner.findM3uFiles(context) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(520.dp)
                .background(Color(0xFF090E1B), RoundedCornerShape(14.dp))
                .border(1.5.dp, CyberCyan, RoundedCornerShape(14.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "ARCHIVOS M3U ENCONTRADOS",
                    color = CyberCyan,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (files.isEmpty()) {
                    Text(
                        text = "No se encontraron archivos .m3u o .m3u8 en la memoria o memorias USB.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(files) { file ->
                            SettingsActionItem(
                                title = file.name,
                                subtitle = "Ruta: ${file.parent ?: ""}",
                                isHighlighted = false,
                                onClick = { onSelectFile(file) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    SettingsActionItem(
                        title = "CERRAR",
                        subtitle = "",
                        isHighlighted = false,
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}