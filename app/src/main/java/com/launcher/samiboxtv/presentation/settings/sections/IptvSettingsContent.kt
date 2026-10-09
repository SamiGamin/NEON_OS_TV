package com.launcher.samiboxtv.presentation.settings.sections

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.launcher.samiboxtv.domain.model.VirtualApps
import com.launcher.samiboxtv.presentation.home.HomeUiEvent
import com.launcher.samiboxtv.presentation.home.HomeUiState
import com.launcher.samiboxtv.presentation.settings.components.SettingsActionItem
import com.launcher.samiboxtv.presentation.settings.dialogs.IptvFilePickerItemDialog
import com.launcher.samiboxtv.presentation.settings.dialogs.IptvUrlInputDialog

@Composable
fun IptvSettingsSection(
    uiState: HomeUiState,
    context: Context,
    onEvent: (HomeUiEvent) -> Unit,
    onOpenQrDialog: () -> Unit,
    qrButtonFocusRequester: FocusRequester = remember { FocusRequester() }
) {
    var showUrlDialog by remember { mutableStateOf(false) }
    var showFileDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val isDefaultList = uiState.currentIptvUrl == VirtualApps.DEFAULT_IPTV_URL
        val sourceLabel = when {
            uiState.activeIptvSource != null -> uiState.activeIptvSource
            isDefaultList -> "LISTA DE EJEMPLO"
            else -> "Sin lista configurada"
        }
        val countLabel = "${uiState.iptvChannels.size} canales activos"
        val subtitle = if (uiState.isIptvLoading) {
            uiState.iptvStatusMessage ?: "Cargando lista..."
        } else {
            "$sourceLabel • $countLabel"
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

        item(key = "iptv_qr") {
            SettingsActionItem(
                title = "CARGAR LISTA ESCANEANDO CÓDIGO QR",
                subtitle = "Abre la cámara de tu celular, escanea la pantalla y pega el link con un toque",
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

        item(key = "iptv_sample") {
            SettingsActionItem(
                title = "CARGAR LISTA DE EJEMPLO",
                subtitle = "${VirtualApps.DEFAULT_IPTV_URL} • Canales predeterminados de demostración",
                isHighlighted = isDefaultList && uiState.iptvChannels.isNotEmpty(),
                onClick = {
                    onEvent(HomeUiEvent.LoadIptvFromUrl(VirtualApps.DEFAULT_IPTV_URL))
                }
            )
        }

        item(key = "iptv_url") {
            SettingsActionItem(
                title = "CAMBIAR ENLACE URL PERSONALIZADO",
                subtitle = if (uiState.currentIptvUrl.isNotBlank()) "URL actual: ${uiState.currentIptvUrl}" else "Pega o escribe tu enlace M3U personalizado",
                isHighlighted = false,
                onClick = { showUrlDialog = true }
            )
        }

        item(key = "iptv_file") {
            SettingsActionItem(
                title = "BUSCAR EN MEMORIA INTERNA O DISCO USB",
                subtitle = "Detecta y carga automáticamente archivos .m3u o .m3u8 en la TV",
                isHighlighted = false,
                onClick = { showFileDialog = true }
            )
        }

        if (uiState.iptvChannels.isNotEmpty()) {
            item(key = "iptv_clear") {
                SettingsActionItem(
                    title = "ELIMINAR LISTA ACTUAL",
                    subtitle = "Borra los canales cargados de la memoria",
                    isHighlighted = false,
                    onClick = { onEvent(HomeUiEvent.ClearIptvList) }
                )
            }
        }
    }

    if (showUrlDialog) {
        IptvUrlInputDialog(
            initialUrl = uiState.currentIptvUrl,
            onDismiss = { showUrlDialog = false },
            onConfirm = { url ->
                onEvent(HomeUiEvent.LoadIptvFromUrl(url))
                showUrlDialog = false
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
            }
        )
    }
}