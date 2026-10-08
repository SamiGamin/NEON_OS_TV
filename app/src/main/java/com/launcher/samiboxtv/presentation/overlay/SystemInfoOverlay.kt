package com.launcher.samiboxtv.presentation.overlay

import android.view.Choreographer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

/**
 * Overlay HUD flotante que mide y muestra en tiempo real los fotogramas por segundo (FPS)
 * y la estabilidad de renderizado de la pantalla en Android TV.
 */
@Composable
fun SystemInfoOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    var currentFps by remember { mutableIntStateOf(60) }

    // Medición precisa de FPS mediante Choreographer
    DisposableEffect(visible) {
        var frameCount = 0
        var lastTimeNanos = System.nanoTime()

        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount++
                val elapsedNanos = frameTimeNanos - lastTimeNanos
                if (elapsedNanos >= 1_000_000_000L) { // 1 segundo transcurrido
                    currentFps = (frameCount * 1_000_000_000L / elapsedNanos).toInt()
                    frameCount = 0
                    lastTimeNanos = frameTimeNanos
                }
                Choreographer.getInstance().postFrameCallback(this)
            }
        }

        Choreographer.getInstance().postFrameCallback(callback)

        onDispose {
            Choreographer.getInstance().removeFrameCallback(callback)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 20.dp, end = 28.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xDD050A14))
                    .border(1.5.dp, CyberCyan.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FPS:",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$currentFps",
                            color = if (currentFps >= 50) CyberCyan else (if (currentFps >= 30) CyberAmber else CyberMagenta),
                            fontSize = 14.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentFps >= 50) "● SMOOTH" else "▲ DROP",
                            color = if (currentFps >= 50) CyberCyan else CyberAmber,
                            fontSize = 9.sp,
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HUD OVERLAY // HARDWARE REFRESH",
                            color = Color(0xFF6B8BAA),
                            fontSize = 8.sp,
                            fontFamily = ShareTechMonoFontFamily
                        )
                    }
                }
            }
        }
    }
}
