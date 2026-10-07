package com.launcher.samiboxtv.domain.model

import android.graphics.drawable.Drawable

/**
 * Modelo de dominio que representa una aplicación instalada en el Launcher TV.
 */
data class AppItem(
    val name: String,
    val packageName: String,
    val icon: Drawable? = null,
    val isHidden: Boolean = false,
    val orderIndex: Int = -1
)
