package com.launcher.samiboxtv.presentation.media

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.domain.model.MediaFile
import com.launcher.samiboxtv.domain.model.MediaType
import com.launcher.samiboxtv.domain.model.StorageDrive
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Explorador de Medios nativo de pantalla completa para Android TV.
 * Permite explorar almacenamiento interno y memorias USB conectadas con navegación D-Pad en dos paneles.
 */
@Composable
fun MediaExplorerScreen(
    drives: List<StorageDrive>,
    selectedDrive: StorageDrive?,
    currentFilter: MediaType?,
    files: List<MediaFile>,
    isLoading: Boolean,
    onSelectDrive: (StorageDrive) -> Unit,
    onFilterChange: (MediaType?) -> Unit,
    onSelectFile: (MediaFile) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialFocusRequester = remember { FocusRequester() }

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        try {
            initialFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF060A14), Color(0xFF03050A))
                )
            )
            .padding(horizontal = 36.dp, vertical = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Cabecera Cyberpunk
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "CYBER MEDIA HUB",
                            color = CyberCyan,
                            fontSize = 20.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberMagenta.copy(alpha = 0.25f))
                                .border(1.dp, CyberMagenta, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "USB & LOCAL",
                                color = CyberMagenta,
                                fontSize = 9.sp,
                                fontFamily = ShareTechMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "EXPLORADOR MULTIMEDIA NATIVO PARA ANDROID TV",
                        color = CyberGrey,
                        fontSize = 11.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )
                }

                // Botón Atrás / Salir
                ExitHubButton(onClick = onBack)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Selector de Unidades de Almacenamiento (Drives)
            Text(
                text = "UNIDADES DE ALMACENAMIENTO DETECTADAS:",
                color = CyberAmber,
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(drives) { drive ->
                    val isSelected = drive.path.absolutePath == selectedDrive?.path?.absolutePath
                    DriveSelectorCard(
                        drive = drive,
                        isSelected = isSelected,
                        onClick = { onSelectDrive(drive) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Barra de Filtros y Estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        title = "TODOS (${files.size})",
                        isSelected = currentFilter == null,
                        onClick = { onFilterChange(null) },
                        modifier = Modifier.focusRequester(initialFocusRequester)
                    )
                    FilterChip(
                        title = "🎬 VIDEOS",
                        isSelected = currentFilter == MediaType.VIDEO,
                        onClick = { onFilterChange(MediaType.VIDEO) }
                    )
                    FilterChip(
                        title = "♫ MÚSICA",
                        isSelected = currentFilter == MediaType.AUDIO,
                        onClick = { onFilterChange(MediaType.AUDIO) }
                    )
                }

                Text(
                    text = "ARCHIVOS ENCONTRADOS: ${files.size}",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Lista o Estado Vacío de Archivos Multimedia
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "> ESCANEANDO UNIDAD Y LEYENDO ARCHIVOS...",
                        color = CyberCyan,
                        fontSize = 14.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )
                }
            } else if (files.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NO SE ENCONTRARON ARCHIVOS MULTIMEDIA EN ESTA UNIDAD",
                            color = CyberGrey,
                            fontSize = 13.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Copia videos (.mp4, .mkv) o canciones (.mp3, .flac) a la memoria o conecta un pendrive USB.",
                            color = CyberAmber,
                            fontSize = 11.sp,
                            fontFamily = ShareTechMonoFontFamily
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(files, key = { it.path }) { mediaFile ->
                        MediaFileRowItem(
                            mediaFile = mediaFile,
                            onClick = { onSelectFile(mediaFile) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriveSelectorCard(
    drive: StorageDrive,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(shape)
            .background(if (isFocused) Color(0xFF162544) else (if (isSelected) Color(0xFF0F1A30) else Color(0xFF080D1A)))
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) CyberMagenta else (if (isSelected) CyberCyan else CyberCyan.copy(alpha = 0.2f)),
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    onClick()
                    true
                } else false
            }
            .focusable()
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (drive.isUsb) "🖴 USB" else "☵ INTERNA",
                    color = if (drive.isUsb) CyberMagenta else CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
                if (isSelected) {
                    Text(
                        text = "● ACTIVA",
                        color = CyberAmber,
                        fontSize = 9.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = drive.name,
                color = if (isFocused) Color.White else Color(0xFFD6E4F0),
                fontSize = 12.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${drive.freeSpaceGb} GB libres de ${drive.totalSpaceGb} GB",
                color = CyberGrey,
                fontSize = 9.sp,
                fontFamily = ShareTechMonoFontFamily
            )
        }
    }
}

@Composable
private fun FilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (isFocused) Color(0xFF1E2E50) else (if (isSelected) CyberCyan.copy(alpha = 0.2f) else Color(0xFF090F1C)))
            .border(
                width = 1.dp,
                color = if (isFocused) CyberAmber else (if (isSelected) CyberCyan else CyberCyan.copy(alpha = 0.25f)),
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    onClick()
                    true
                } else false
            }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = if (isFocused) CyberAmber else (if (isSelected) CyberCyan else Color.White),
            fontSize = 11.sp,
            fontFamily = ShareTechMonoFontFamily,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun MediaFileRowItem(
    mediaFile: MediaFile,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isFocused) Color(0xFF15223C) else Color(0xFF080D1A))
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) (if (mediaFile.type == MediaType.VIDEO) CyberCyan else CyberMagenta) else CyberCyan.copy(alpha = 0.15f),
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    onClick()
                    true
                } else false
            }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (mediaFile.type == MediaType.VIDEO) "🎬" else "♫",
                    fontSize = 16.sp
                )

                Column {
                    Text(
                        text = mediaFile.name,
                        color = if (isFocused) Color.White else Color(0xFFD6E4F0),
                        fontSize = 13.sp,
                        fontFamily = ShareTechMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${mediaFile.sizeMb} MB  •  ${dateFormat.format(Date(mediaFile.dateModified))}",
                        color = CyberGrey,
                        fontSize = 10.sp,
                        fontFamily = ShareTechMonoFontFamily
                    )
                }
            }

            Text(
                text = if (isFocused) "[REPRODUCIR ▶]" else if (mediaFile.type == MediaType.VIDEO) "VIDEO" else "AUDIO",
                color = if (isFocused) CyberAmber else (if (mediaFile.type == MediaType.VIDEO) CyberCyan else CyberMagenta),
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExitHubButton(onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (isFocused) Color(0xFF1E283C) else Color(0xFF0C1220))
            .border(
                width = 1.dp,
                color = if (isFocused) CyberAmber else CyberCyan.copy(alpha = 0.3f),
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_ENTER ||
                            keyEvent.key.nativeKeyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                ) {
                    onClick()
                    true
                } else false
            }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = "◄ VOLVER AL LAUNCHER",
            color = if (isFocused) CyberAmber else CyberGrey,
            fontSize = 11.sp,
            fontFamily = ShareTechMonoFontFamily,
            fontWeight = FontWeight.Bold
        )
    }
}
