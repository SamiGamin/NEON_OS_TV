package com.launcher.samiboxtv.domain.model

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

/**
 * Modelo de dominio que representa una aplicación instalada en el Launcher TV
 * con soporte para banners 16:9 y categorías.
 */
@Immutable
data class AppItem(
    val packageName: String,
    val activityName: String = "",
    val name: String,
    val iconDrawable: Drawable? = null,
    val bannerDrawable: Drawable? = null,
    val category: String = "APPS", // "STREAMING", "GAMING", "APPS", etc.
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val orderIndex: Int = -1,
    val isSystemApp: Boolean = false
) {
    // Compatibilidad para componentes que consumen .icon
    val icon: Drawable? get() = iconDrawable
}
