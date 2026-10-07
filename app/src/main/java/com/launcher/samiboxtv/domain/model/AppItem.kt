package com.launcher.samiboxtv.domain.model

import android.graphics.drawable.Drawable

/**
 * Modelo de dominio que representa una aplicación instalada en el Launcher TV
 * con soporte para banners 16:9 y categorías.
 */
data class AppItem(
    val packageName: String,
    val activityName: String = "",
    val name: String,
    val iconDrawable: Drawable? = null,
    val bannerDrawable: Drawable? = null,
    val category: String = "APP", // "STREAMING", "GAMING", "SYSTEM", etc.
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val orderIndex: Int = -1
) {
    // Compatibilidad para componentes que consumen .icon
    val icon: Drawable? get() = iconDrawable
}
