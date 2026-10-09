package com.launcher.samiboxtv.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.domain.model.NetworkType
import com.launcher.samiboxtv.presentation.theme.CyberAmber
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

private val CyberPink = Color(0xFFFF007F)

@Composable
fun CyberNetworkBadge(
    status: NetworkStatus,
    modifier: Modifier = Modifier
) {
    val targetColor = when {
        !status.isConnected -> CyberPink
        !status.hasInternetAccess -> CyberAmber
        else -> CyberCyan
    }
    val themeColor by animateColorAsState(targetValue = targetColor, label = "badge_color")

    @DrawableRes val iconRes: Int = when (status.type) {
        NetworkType.ETHERNET -> R.drawable.ic_network_ethernet
        NetworkType.WIFI -> R.drawable.ic_network_wifi
        NetworkType.DISCONNECTED,
        NetworkType.UNKNOWN,
        NetworkType.CELLULAR -> R.drawable.ic_network_disconnected
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(themeColor.copy(alpha = 0.10f))
            .border(0.8.dp, themeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = status.type.name,
                colorFilter = ColorFilter.tint(themeColor),
                modifier = Modifier.size(13.dp)
            )

            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(themeColor.copy(alpha = if (status.isConnected) dotAlpha else 1f))
            )

            Text(
                text = status.hudDisplayString,
                color = themeColor,
                fontSize = 9.sp,
                fontFamily = ShareTechMonoFontFamily,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
    }
}