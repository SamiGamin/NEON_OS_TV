package com.launcher.samiboxtv.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Typography
import com.launcher.samiboxtv.R

val OutfitFontFamily = FontFamily(
    Font(R.font.outfit, FontWeight.Normal),
    Font(R.font.outfit, FontWeight.Medium),
    Font(R.font.outfit, FontWeight.Bold)
)

val ShareTechMonoFontFamily = FontFamily(
    Font(resId = R.font.plus_jakarta_sans_regular, weight = FontWeight.Normal),
    Font(resId = R.font.plus_jakarta_sans_bold, weight = FontWeight.Bold),
    Font(resId = R.font.plus_jakarta_sans_medium, weight = FontWeight.Medium),
    Font(resId = R.font.plus_jakarta_sans_semi_bold, weight = FontWeight.SemiBold),
    Font(resId = R.font.plus_jakarta_sans_extrablod, weight = FontWeight.ExtraBold),
)

@OptIn(ExperimentalTvMaterial3Api::class)
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    titleLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
