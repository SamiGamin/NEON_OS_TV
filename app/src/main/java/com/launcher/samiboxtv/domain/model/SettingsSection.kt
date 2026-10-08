package com.launcher.samiboxtv.domain.model

import androidx.compose.runtime.Immutable

/**
 * Secciones del panel de configuración estilo Android TV.
 */
@Immutable
enum class SettingsSection(
    val title: String,
    val subtitle: String,
    val symbol: String
) {
    FAVORITES(
        title = "FAVORITOS & DESTACADOS",
        subtitle = "Configura la primera fila de acceso rápido",
        symbol = "★"
    ),
    CATEGORIES(
        title = "CATEGORÍAS & FILAS",
        subtitle = "Crea filas temáticas personalizadas para tus apps",
        symbol = "☷"
    ),
    APP_STYLE(
        title = "FORMATO DE APPS",
        subtitle = "Banner 16:9, cuadrícula 1:1 o vista compacta",
        symbol = "◫"
    ),
    SYSTEM(
        title = "SISTEMA & TV",
        subtitle = "Launcher por defecto, ajustes de TV y telemetría",
        symbol = "⚙"
    ),
    DEVELOPER("MODO DESARROLLADOR", "Monitoreo, registro de teclas y logs de red", "λ")
}
