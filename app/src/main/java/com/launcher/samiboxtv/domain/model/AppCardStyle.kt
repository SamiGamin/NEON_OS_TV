package com.launcher.samiboxtv.domain.model

import androidx.compose.runtime.Immutable

/**
 * Estilo visual y aspecto de visualización de las tarjetas de aplicaciones en la pantalla de inicio TV.
 */
@Immutable
enum class AppCardStyle(
    val title: String,
    val description: String,
    val widthDp: Int,
    val aspectRatio: Float
) {
    BANNER_16_9(
        title = "BANNER PANORÁMICO 16:9",
        description = "Estilo oficial Android TV con banners de cine y arte horizontal.",
        widthDp = 220,
        aspectRatio = 16f / 9f
    ),
    SQUARE_1_1(
        title = "CUADRÍCULA 1:1 (MODERNO)",
        description = "Iconos centrados en tarjetas cuadradas estilo Google TV.",
        widthDp = 140,
        aspectRatio = 1f
    ),
    COMPACT(
        title = "MODO COMPACTO (ALTA DENSIDAD)",
        description = "Tarjetas horizontales reducidas para visualizar más apps a la vez.",
        widthDp = 170,
        aspectRatio = 16f / 9f
    );

    companion object {
        fun fromName(name: String?): AppCardStyle {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: BANNER_16_9
        }
    }
}
