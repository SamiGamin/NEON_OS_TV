package com.launcher.samiboxtv.presentation.components.hud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ClockView(modifier: Modifier = Modifier) {
    val formatter = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("America/Bogota")
        }
    }
    val dateHolder = remember { Date() }

    var timeText by remember {
        dateHolder.time = System.currentTimeMillis()
        mutableStateOf(formatter.format(dateHolder))
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            // Sincronizar con el cambio de minuto exacto para no despertar la CPU cada segundo
            val millisToNextMinute = 60_000L - (now % 60_000L) + 50L
            delay(millisToNextMinute.coerceIn(1000L, 60_000L))

            dateHolder.time = System.currentTimeMillis()
            val newTime = formatter.format(dateHolder)
            if (newTime != timeText) {
                timeText = newTime
            }
        }
    }

    Text(
        text = timeText,
        color = CyberAmber,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = ShareTechMonoFontFamily,
        modifier = modifier
    )
}