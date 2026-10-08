package com.launcher.samiboxtv.presentation.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.domain.model.AppItem
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import com.launcher.samiboxtv.presentation.theme.TvViewportDimensions

/**
 * Cuadrícula moderna de alta eficiencia 3 columnas x 2 filas (6 tarjetas en pantalla).
 * Agrupa los elementos en columnas de 2 tarjetas dentro de un LazyRow magnético (Snap Fling),
 * permitiendo una navegación D-Pad bidireccional perfecta:
 * - UP / DOWN: Cambia entre la fila superior e inferior.
 * - LEFT / RIGHT: Desplaza suavemente hacia la columna anterior o siguiente.
 */
@Composable
fun CyberModernGrid(
    apps: List<AppItem>,
    dimensions: TvViewportDimensions,
    onLaunchApp: (AppItem) -> Unit,
    onOpenContextMenu: (AppItem) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    movingAppPackageName: String? = null,
    onMoveDirection: (Int) -> Unit = {},
    onConfirmMove: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    // Agrupamos en pares verticales (columnas de 2)
    val columns = apps.chunked(2)

    Column(modifier = modifier.fillMaxWidth()) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                color = CyberCyan.copy(alpha = 0.85f),
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = dimensions.horizontalPadding, bottom = 8.dp)
            )
        }

        LazyRow(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(horizontal = dimensions.horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(dimensions.spacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(
                items = columns,
                key = { index, pair -> pair.firstOrNull()?.packageName ?: index.toString() }
            ) { _, columnApps ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(dimensions.spacing),
                    modifier = Modifier.width(dimensions.cardWidth)
                ) {
                    columnApps.forEach { app ->
                        val isMoving = movingAppPackageName == app.packageName
                        CyberAppCard(
                            app = app,
                            width = dimensions.cardWidth,
                            height = dimensions.cardHeight,
                            isGhostMode = isMoving,
                            isEditing = isMoving,
                            onClick = {
                                if (isMoving) onConfirmMove() else onLaunchApp(app)
                            },
                            onLongClick = { onOpenContextMenu(app) },
                            onMoveDirection = onMoveDirection,
                            onConfirmMove = onConfirmMove
                        )
                    }
                }
            }
        }
    }
}
