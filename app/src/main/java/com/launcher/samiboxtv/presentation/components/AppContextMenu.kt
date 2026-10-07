package com.launcher.samiboxtv.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCard
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppContextMenu(
    appItem: AppItem,
    onDismiss: () -> Unit,
    onMove: () -> Unit,
    onHide: () -> Unit,
    onToggleFavorite: () -> Unit = {}
) {
    var buttonsEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        buttonsEnabled = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(CyberCard, RoundedCornerShape(12.dp))
                .border(1.5.dp, CyberCyan, RoundedCornerShape(12.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "OPCIONES: ${appItem.name.uppercase()}",
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )

                // Botón Destacar / Quitar de Destacadas
                Button(
                    onClick = {
                        onToggleFavorite()
                        onDismiss()
                    },
                    enabled = buttonsEnabled,
                    colors = ButtonDefaults.colors(
                        containerColor = Color(0xFF1B2238),
                        focusedContainerColor = CyberAmber,
                        focusedContentColor = Color.Black
                    )
                ) {
                    val favText = if (appItem.isFavorite) "★ Quitar de Destacadas" else "★ Destacar en Superior"
                    Text(favText, fontWeight = FontWeight.Medium)
                }

                // Botón Mover de posición
                Button(
                    onClick = {
                        onMove()
                        onDismiss()
                    },
                    enabled = buttonsEnabled,
                    colors = ButtonDefaults.colors(
                        containerColor = Color(0xFF1B2238),
                        focusedContainerColor = CyberCyan,
                        focusedContentColor = Color.Black
                    )
                ) {
                    Text("⇄ Mover de Posición", fontWeight = FontWeight.Medium)
                }

                // Botón Ocultar
                Button(
                    onClick = {
                        onHide()
                        onDismiss()
                    },
                    enabled = buttonsEnabled,
                    colors = ButtonDefaults.colors(
                        containerColor = Color(0xFF1B2238),
                        focusedContainerColor = CyberMagenta,
                        focusedContentColor = Color.White
                    )
                ) {
                    Text("✕ Ocultar de la Pantalla", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
