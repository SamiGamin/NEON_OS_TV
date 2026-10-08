package com.launcher.samiboxtv.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dimensiones calculadas matemáticamente para garantizar "Zero Partial Cards".
 * El ancho y alto de las tarjetas se calculan en base a la resolución actual del TV
 * (1080p, 720p, 4K) y el padding horizontal estricto de seguridad.
 */
@Immutable
data class TvViewportDimensions(
    val cardWidth: Dp,
    val cardHeight: Dp,
    val spacing: Dp = 18.dp,
    val horizontalPadding: Dp = 48.dp,
    val visibleCount: Int,
    val isGrid: Boolean = false
)

@Composable
fun rememberTvLayoutDimensions(layoutMode: AppLayoutMode): TvViewportDimensions {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    return remember(layoutMode, screenWidth, screenHeight) {
        val horizontalPadding = 48.dp
        val spacing = 18.dp

        when (layoutMode) {
            AppLayoutMode.COMPACT_HIGH_DENSITY -> {
                val n = 5
                val availableWidth = screenWidth - (horizontalPadding * 2) - (spacing * (n - 1))
                val cardWidth = availableWidth / n
                val cardHeight = cardWidth / (4f / 3f)
                TvViewportDimensions(
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    spacing = spacing,
                    horizontalPadding = horizontalPadding,
                    visibleCount = n
                )
            }
            AppLayoutMode.PANORAMIC_16_9 -> {
                val n = 4
                val availableWidth = screenWidth - (horizontalPadding * 2) - (spacing * (n - 1))
                val cardWidth = availableWidth / n
                val cardHeight = cardWidth / (16f / 9f)
                TvViewportDimensions(
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    spacing = spacing,
                    horizontalPadding = horizontalPadding,
                    visibleCount = n
                )
            }
            AppLayoutMode.COMPACT_STANDARD -> {
                val n = 3
                val availableWidth = screenWidth - (horizontalPadding * 2) - (spacing * (n - 1))
                val cardWidth = availableWidth / n
                val cardHeight = cardWidth / (16f / 9f)
                TvViewportDimensions(
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    spacing = spacing,
                    horizontalPadding = horizontalPadding,
                    visibleCount = n
                )
            }
            AppLayoutMode.DUAL_PRIORITY -> {
                val n = 2
                val availableWidth = screenWidth - (horizontalPadding * 2) - (spacing * (n - 1))
                val cardWidth = availableWidth / n
                val cardHeight = cardWidth / (21f / 9f)
                TvViewportDimensions(
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    spacing = spacing,
                    horizontalPadding = horizontalPadding,
                    visibleCount = n
                )
            }
            AppLayoutMode.MODERN_GRID -> {
                val columns = 3
                val availableWidth = screenWidth - (horizontalPadding * 2) - (spacing * (columns - 1))
                val cardWidth = availableWidth / columns
                // Altura para que 2 filas completas encajen en la pantalla junto al header
                val headerSpace = 130.dp
                val availableGridHeight = (screenHeight - headerSpace - spacing - 40.dp).coerceAtLeast(200.dp)
                val cardHeight = (availableGridHeight / 2).coerceIn(110.dp, 165.dp)
                TvViewportDimensions(
                    cardWidth = cardWidth,
                    cardHeight = cardHeight,
                    spacing = spacing,
                    horizontalPadding = horizontalPadding,
                    visibleCount = 6,
                    isGrid = true
                )
            }
        }
    }
}
