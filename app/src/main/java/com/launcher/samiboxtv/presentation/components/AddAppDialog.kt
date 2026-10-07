package com.launcher.samiboxtv.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.rememberAsyncImagePainter
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCard
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberGrey
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AddAppDialog(
    allInstalledApps: List<AppItem>,
    onToggleVisibility: (AppItem) -> Unit,
    onShowAll: () -> Unit,
    onHideAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val visibleCount = allInstalledApps.count { !it.isHidden }
    val totalCount = allInstalledApps.size

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .background(Color(0xFF070B16), RoundedCornerShape(16.dp))
                .border(1.5.dp, CyberCyan, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header del Gestor de Visibilidad
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GESTOR DE VISIBILIDAD // MOSTRAR Y OCULTAR",
                            color = CyberCyan,
                            fontSize = 18.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Pulsa OK para alternar cada app. Mostrando $visibleCount de $totalCount aplicaciones.",
                            color = CyberAmber,
                            fontSize = 12.sp,
                            fontFamily = ShareTechMonoFontFamily
                        )
                    }

                    // Botones de acción masiva
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onShowAll,
                            colors = ButtonDefaults.colors(
                                containerColor = Color(0xFF142038),
                                focusedContainerColor = CyberCyan,
                                focusedContentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "MOSTRAR TODAS",
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onHideAll,
                            colors = ButtonDefaults.colors(
                                containerColor = Color(0xFF142038),
                                focusedContainerColor = CyberMagenta,
                                focusedContentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "OCULTAR TODAS",
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Cuadrícula con todas las aplicaciones y estado ON/OFF
                if (allInstalledApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No se detectaron aplicaciones instaladas.", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allInstalledApps, key = { it.packageName }) { appItem ->
                            AppVisibilityToggleCard(
                                appItem = appItem,
                                onToggle = { onToggleVisibility(appItem) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun AppVisibilityToggleCard(
    appItem: AppItem,
    onToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val isVisible = !appItem.isHidden
    val statusColor = if (isVisible) CyberCyan else CyberGrey
    val badgeBg = if (isVisible) CyberCyan.copy(alpha = 0.15f) else Color(0x33333333)
    val badgeText = if (isVisible) "✔ VISIBLE" else "✕ OCULTA"

    Card(
        onClick = onToggle,
        interactionSource = interactionSource,
        shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
        colors = CardDefaults.colors(
            containerColor = CyberCard,
            focusedContainerColor = if (isVisible) Color(0xFF162544) else Color(0xFF1C1D24)
        ),
        border = CardDefaults.border(
            focusedBorder = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, if (isVisible) CyberCyan else CyberAmber),
                shape = RoundedCornerShape(8.dp)
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = rememberAsyncImagePainter(appItem.iconDrawable ?: appItem.bannerDrawable),
                contentDescription = appItem.name,
                modifier = Modifier
                    .size(48.dp)
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = appItem.name,
                color = if (isFocused) Color.White else Color(0xFFD0D5DD),
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 12.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Badge indicador de visibilidad
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeBg)
                    .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
