package com.launcher.samiboxtv.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.launcher.samiboxtv.core.dispatcher.ApkInstallerHelper
import com.launcher.samiboxtv.domain.model.UpdateInfo
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import kotlinx.coroutines.launch
import java.io.File

private enum class UpdateDownloadState {
    IDLE,
    DOWNLOADING,
    READY_TO_INSTALL,
    ERROR
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val techFont = ShareTechMonoFontFamily

    var downloadState by remember { mutableStateOf(UpdateDownloadState.IDLE) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(targetValue = downloadProgress, label = "ProgressAnim")

    val apkFile = remember { File(context.cacheDir, "update_launcher.apk") }

    Dialog(
        onDismissRequest = {
            // Evitar cerrar accidentalmente mientras descarga
            if (downloadState != UpdateDownloadState.DOWNLOADING) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .background(Color(0xFF070B16), RoundedCornerShape(16.dp))
                .border(2.dp, CyberMagenta, RoundedCornerShape(16.dp))
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Cabecera del Diálogo
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTUALIZACIÓN DISPONIBLE // OTA UPDATE",
                            color = CyberMagenta,
                            fontSize = 18.sp,
                            fontFamily = techFont,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberMagenta.copy(alpha = 0.2f))
                                .border(1.dp, CyberMagenta, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "v${updateInfo.currentVersion} ➔ v${updateInfo.latestVersion}",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontFamily = techFont,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = updateInfo.releaseName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontFamily = techFont,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Área de Changelog / Notas de versión
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C1322))
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "NOTAS DE LA VERSIÓN (CHANGELOG):",
                        color = CyberAmber,
                        fontSize = 11.sp,
                        fontFamily = techFont,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = updateInfo.releaseNotes.ifBlank { "Sin notas de versión disponibles." },
                            color = Color(0xFFD0D8E8),
                            fontSize = 13.sp,
                            fontFamily = techFont,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Barra de Progreso Cyberpunk (Visible al descargar o al ocurrir un error)
                if (downloadState != UpdateDownloadState.IDLE) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (downloadState) {
                                    UpdateDownloadState.DOWNLOADING -> "DESCARGANDO PAQUETE OTA..."
                                    UpdateDownloadState.READY_TO_INSTALL -> "PAQUETE DESCARGADO // LISTO PARA INSTALAR"
                                    UpdateDownloadState.ERROR -> "ERROR DE DESCARGA // VERIFICA TU RED"
                                    else -> ""
                                },
                                color = if (downloadState == UpdateDownloadState.ERROR) Color(0xFFFF5252) else CyberCyan,
                                fontFamily = techFont,
                                fontSize = 11.sp
                            )
                            if (downloadState == UpdateDownloadState.DOWNLOADING) {
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}%",
                                    color = CyberAmber,
                                    fontFamily = techFont,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Barra contenedora
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF141A28))
                                .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedProgress)
                                    .background(CyberMagenta)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Botones de Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Cancelar / Recordar
                    Button(
                        onClick = onDismiss,
                        enabled = downloadState != UpdateDownloadState.DOWNLOADING,
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF141A28),
                            focusedContainerColor = Color(0xFF26324D)
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = if (downloadState == UpdateDownloadState.DOWNLOADING) "DESCARGANDO..." else "RECORDAR MÁS TARDE",
                            color = CyberGrey,
                            fontFamily = techFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Botón Principal de Acción
                    Button(
                        onClick = {
                            when (downloadState) {
                                UpdateDownloadState.IDLE, UpdateDownloadState.ERROR -> {
                                    val url = updateInfo.apkDownloadUrl
                                    if (!url.isNullOrBlank()) {
                                        downloadState = UpdateDownloadState.DOWNLOADING
                                        downloadProgress = 0f

                                        coroutineScope.launch {
                                            val success = ApkInstallerHelper.downloadApk(
                                                downloadUrl = url,
                                                outputFile = apkFile,
                                                onProgress = { progress ->
                                                    downloadProgress = progress
                                                }
                                            )

                                            if (success) {
                                                downloadState = UpdateDownloadState.READY_TO_INSTALL
                                                // Abre el instalador nativo inmediatamente tras completar
                                                ApkInstallerHelper.launchInstallApk(context, apkFile)
                                            } else {
                                                downloadState = UpdateDownloadState.ERROR
                                            }
                                        }
                                    }
                                }
                                UpdateDownloadState.READY_TO_INSTALL -> {
                                    // Si el usuario vuelve a presionar el botón
                                    ApkInstallerHelper.launchInstallApk(context, apkFile)
                                }
                                UpdateDownloadState.DOWNLOADING -> {
                                    // Ignorar clics mientras descarga
                                }
                            }
                        },
                        colors = ButtonDefaults.colors(
                            containerColor = when (downloadState) {
                                UpdateDownloadState.READY_TO_INSTALL -> CyberCyan
                                else -> CyberMagenta
                            },
                            focusedContainerColor = CyberAmber
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = when (downloadState) {
                                UpdateDownloadState.IDLE -> "DESCARGAR E INSTALAR"
                                UpdateDownloadState.DOWNLOADING -> "DESCARGANDO..."
                                UpdateDownloadState.READY_TO_INSTALL -> "INSTALAR AHORA"
                                UpdateDownloadState.ERROR -> "REINTENTAR DESCARGA"
                            },
                            color = Color.Black,
                            fontFamily = techFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}