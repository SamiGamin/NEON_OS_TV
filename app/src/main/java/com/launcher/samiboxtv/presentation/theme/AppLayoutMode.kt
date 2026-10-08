package com.launcher.samiboxtv.presentation.theme

import androidx.compose.runtime.Immutable

/**
 * Modos de diseño responsivo y densidad de tarjetas para NEONOS TV.
 * Define la cantidad exacta de tarjetas visibles en pantalla (Zero Partial Cards)
 * y la relación de aspecto matemática.
 */
@Immutable
enum class AppLayoutMode(
    val title: String,
    val description: String,
    val visibleCards: Int,
    val aspectRatio: Float
) {
    COMPACT_HIGH_DENSITY(
        title = "ALTA DENSIDAD (5 TARJETAS)",
        description = "5 tarjetas completas en pantalla (ratio 4:3). Máximo aprovechamiento del espacio.",
        visibleCards = 5,
        aspectRatio = 4f / 3f
    ),
    PANORAMIC_16_9(
        title = "PANORÁMICO 16:9 (4 TARJETAS)",
        description = "4 tarjetas cinematográficas panorámicas estilo Android TV clásico.",
        visibleCards = 4,
        aspectRatio = 16f / 9f
    ),
    COMPACT_STANDARD(
        title = "ESTÁNDAR (3 TARJETAS)",
        description = "3 tarjetas medianas para una vista amplia y equilibrada.",
        visibleCards = 3,
        aspectRatio = 16f / 9f
    ),
    DUAL_PRIORITY(
        title = "DUAL ULTRA-WIDE (2 TARJETAS)",
        description = "2 tarjetas grandes ultra panorámicas con proporción 21:9 para foco total.",
        visibleCards = 2,
        aspectRatio = 21f / 9f
    ),
    MODERN_GRID(
        title = "CUADRÍCULA MODERNA (3x2 - 6 TARJETAS)",
        description = "Matriz fija de 3 columnas x 2 filas (6 tarjetas en pantalla) sin scroll vertical cortado.",
        visibleCards = 6,
        aspectRatio = 16f / 9f
    );

    companion object {
        fun fromName(name: String?): AppLayoutMode {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: COMPACT_HIGH_DENSITY
        }
    }
}
