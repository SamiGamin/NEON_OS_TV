package com.launcher.samiboxtv.presentation.settings.sections

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.presentation.home.HomeUiEvent
import com.launcher.samiboxtv.presentation.home.HomeUiState
import com.launcher.samiboxtv.presentation.settings.components.SettingsActionItem
import com.launcher.samiboxtv.presentation.settings.components.tvClickable
import com.launcher.samiboxtv.presentation.settings.dialogs.AddIptvPlaylistDialog
import com.launcher.samiboxtv.presentation.settings.dialogs.IptvFilePickerItemDialog
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import com.launcher.samiboxtv.util.iptv.IptvPlaylistManager

@Composable
fun IptvSettingsSection(
    uiState: HomeUiState,
    context: Context,
    onEvent: (HomeUiEvent) -> Unit,
    onOpenQrDialog: () -> Unit,
    qrButtonFocusRequester: FocusRequester = remember { FocusRequester() }
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showFileDialog by remember { mutableStateOf(false) }
    var playlists by remember { mutableStateOf(IptvPlaylistManager.getSavedPlaylists(context)) }

    fun refreshPlaylists() {
        playlists = IptvPlaylistManager.getSavedPlaylists(context)
    }

    // Refresca la lista en pantalla automáticamente si el celular envía una lista por QR
    LaunchedEffect(uiState.currentIptvUrl) {
        refreshPlaylists()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val countLabel = "${uiState.iptvChannels.size} canales activos"
        val activeName = uiState.activeIptvSource ?: "Lista en uso"
        val subtitle = if (uiState.isIptvLoading) {
            uiState.iptvStatusMessage ?: "Cargando lista..."
        } else {
            "$activeName • $countLabel"
        }

        item(key = "iptv_status") {
            SettingsActionItem(
                title = "ESTADO DE LA LISTA IPTV",
                subtitle = subtitle,
                isHighlighted = uiState.iptvChannels.isNotEmpty(),
                enabled = false,
                onClick = {}
            )
        }

        item(key = "iptv_add_url") {
            SettingsActionItem(
                title = "+ AGREGAR NUEVA LISTA POR URL",
                subtitle = "Escribe un nombre y el enlace M3U para guardarla permanentemente",
                isHighlighted = false,
                onClick = { showAddDialog = true }
            )
        }

        item(key = "iptv_qr") {
            SettingsActionItem(
                title = "📲 CARGAR LISTA ESCANEANDO CÓDIGO QR",
                subtitle = "Abre la cámara de tu celular, escanea la pantalla y envía la lista",
                isHighlighted = true,
                modifier = Modifier.focusRequester(qrButtonFocusRequester),
                onClick = {
                    if (!uiState.isLogServerRunning) {
                        onEvent(HomeUiEvent.ToggleLogServer)
                    }
                    onOpenQrDialog()
                }
            )
        }

        item(key = "iptv_file") {
            SettingsActionItem(
                title = "📁 BUSCAR EN MEMORIA INTERNA O DISCO USB",
                subtitle = "Detecta y carga automáticamente archivos .m3u o .m3u8 en la TV",
                isHighlighted = false,
                onClick = { showFileDialog = true }
            )
        }

        item(key = "iptv_playlists_header") {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TUS LISTAS GUARDADAS (PULSA OK PARA ACTIVAR):",
                color = CyberCyan,
                fontSize = 11.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        items(items = playlists, key = { "pl_${it.id}" }) { playlist ->
            val isActive = uiState.currentIptvUrl == playlist.url
            var isFocused by remember { mutableStateOf(false) }
            val shape = RoundedCornerShape(8.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(if (isFocused) Color(0xFF1B2848) else Color(0xFF0F172B))
                    .border(
                        width = if (isFocused) 1.5.dp else 1.dp,
                        color = when {
                            isFocused -> CyberAmber
                            isActive -> CyberCyan
                            else -> CyberCyan.copy(alpha = 0.2f)
                        },
                        shape = shape
                    )
                    .onFocusChanged { isFocused = it.isFocused }
                    .tvClickable {
                        onEvent(HomeUiEvent.SelectIptvPlaylist(playlist))
                        refreshPlaylists()
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = playlist.name,
                                color = if (isFocused) CyberAmber else (if (isActive) CyberCyan else Color.White),
                                fontSize = 12.sp,
                                fontFamily = ShareTechMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (playlist.isDefault) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "[DEMO]",
                                    color = CyberAmber.copy(alpha = 0.8f),
                                    fontSize = 9.sp,
                                    fontFamily = ShareTechMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = playlist.url,
                            color = if (isActive) CyberCyan.copy(alpha = 0.85f) else Color(0xFF7E9BB8),
                            fontSize = 9.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isActive) "● EN USO" else "○ ELEGIR",
                            color = if (isActive) Color(0xFF00E676) else CyberGrey,
                            fontSize = 10.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )

                        if (!playlist.isDefault) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF2A0D15))
                                    .border(0.5.dp, CyberMagenta.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .tvClickable {
                                        onEvent(HomeUiEvent.DeleteIptvPlaylist(playlist.id))
                                        refreshPlaylists()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "✕",
                                    color = CyberMagenta,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.iptvChannels.isNotEmpty()) {
            item(key = "iptv_clear") {
                Spacer(modifier = Modifier.height(4.dp))
                SettingsActionItem(
                    title = "ELIMINAR CANALES DE LA MEMORIA",
                    subtitle = "Vacía el búfer de reproducción actual",
                    isHighlighted = false,
                    onClick = { onEvent(HomeUiEvent.ClearIptvList) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddIptvPlaylistDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, url ->
                onEvent(HomeUiEvent.AddIptvPlaylist(name, url))
                showAddDialog = false
                refreshPlaylists()
            }
        )
    }

    if (showFileDialog) {
        IptvFilePickerItemDialog(
            context = context,
            onDismiss = { showFileDialog = false },
            onSelectFile = { file ->
                onEvent(HomeUiEvent.LoadIptvFromFile(file))
                showFileDialog = false
                refreshPlaylists()
            }
        )
    }
}