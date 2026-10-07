package com.launcher.samiboxtv.presentation.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
import com.launcher.samiboxtv.domain.model.UpdateInfo
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

/**
 * Diálogo Cyberpunk para notificar y gestionar nuevas actualizaciones del Launcher
 * obtenidas desde GitHub Releases.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val techFont = ShareTechMonoFontFamily

    Dialog(
        onDismissRequest = onDismiss,
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

                Spacer(modifier = Modifier.height(20.dp))

                // Botones de Acción (TV Remote D-Pad friendly)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.colors(
                            containerColor = Color(0xFF141A28),
                            focusedContainerColor = Color(0xFF26324D)
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "RECORDAR MÁS TARDE",
                            color = CyberGrey,
                            fontFamily = techFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            val downloadUrl = updateInfo.apkDownloadUrl
                            if (!downloadUrl.isNullOrBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    // Fallback a navegador
                                }
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.colors(
                            containerColor = CyberMagenta,
                            focusedContainerColor = CyberCyan
                        ),
                        shape = ButtonDefaults.shape(RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "DESCARGAR E INSTALAR",
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
