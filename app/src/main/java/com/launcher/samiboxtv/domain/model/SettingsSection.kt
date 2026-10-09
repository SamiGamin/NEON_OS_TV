package com.launcher.samiboxtv.domain.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.launcher.samiboxtv.R

/**
 * Secciones del panel de configuración con sus drawables vectoriales nativos.
 */
@Immutable
enum class SettingsSection(
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int
) {
    FAVORITES(
        title = "FAVORITOS & DESTACADOS",
        subtitle = "Configura la primera fila de acceso rápido",
        iconRes = R.drawable.ic_star
    ),
    CATEGORIES(
        title = "CATEGORÍAS & FILAS",
        subtitle = "Crea filas temáticas personalizadas para tus apps",
        iconRes = R.drawable.ic_categories
    ),
    APP_STYLE(
        title = "FORMATO DE APPS",
        subtitle = "Banner 16:9, cuadrícula 1:1 o vista compacta",
        iconRes = R.drawable.ic_layout_grid
    ),
    IPTV(
        title = "TELEVISIÓN IPTV",
        subtitle = "Gestión de listas M3U por enlace o archivo",
        iconRes = R.drawable.ic_live_tv
    ),
    SYSTEM(
        title = "SISTEMA & TV",
        subtitle = "Launcher por defecto, ajustes de TV y telemetría",
        iconRes = R.drawable.ic_settings
    ),
    DEVELOPER(
        title = "MODO DESARROLLADOR",
        subtitle = "Monitoreo, registro de teclas y logs de red",
        iconRes = R.drawable.ic_developer
    )
}