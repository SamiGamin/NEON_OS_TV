package com.launcher.samiboxtv.domain.model

import androidx.compose.runtime.Immutable

/**
 * Representa una categoría o fila personalizada para agrupar aplicaciones en el Launcher TV.
 */
@Immutable
data class AppCategory(
    val name: String,
    val isSystemDefault: Boolean = false
)
