package com.launcher.samiboxtv.presentation.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SamiBoxTVTheme(
    content: @Composable () -> Unit,
) {
    val colorScheme = darkColorScheme(
        primary = CyberCyan,
        secondary = CyberMagenta,
        tertiary = CyberAmber,
        background = CyberBg,
        surface = CyberCard,
        onBackground = CyberWhite,
        onSurface = CyberWhite
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
