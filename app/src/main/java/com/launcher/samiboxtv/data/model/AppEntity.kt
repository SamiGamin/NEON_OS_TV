package com.launcher.samiboxtv.data.model

import android.graphics.drawable.Drawable

/**
 * Entidad de datos que modela la información obtenida desde PackageManager.
 */
data class AppEntity(
    val name: String,
    val packageName: String,
    val icon: Drawable? = null
)
