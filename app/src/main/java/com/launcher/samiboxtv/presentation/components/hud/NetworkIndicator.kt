package com.launcher.samiboxtv.presentation.components.hud

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.launcher.samiboxtv.R
import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.domain.model.NetworkType
import com.launcher.samiboxtv.presentation.theme.CyberCyan
import com.launcher.samiboxtv.presentation.theme.CyberMagenta
import com.launcher.samiboxtv.presentation.theme.ShareTechMonoFontFamily

@Composable
fun NetworkIndicator(
    networkStatus: NetworkStatus,
    modifier: Modifier = Modifier
) {
    val (iconRes, label, color) = when (networkStatus.type) {
        NetworkType.ETHERNET -> Triple(R.drawable.ic_ethernet, "ETHERNET (LAN)", CyberCyan)
        NetworkType.WIFI -> Triple(R.drawable.ic_wifi, "WI-FI (ONLINE)", CyberCyan)
        NetworkType.CELLULAR -> Triple(R.drawable.ic_wifi, "MÓVIL (ONLINE)", CyberCyan)
        NetworkType.DISCONNECTED, NetworkType.UNKNOWN -> Triple(
            R.drawable.ic_network_disconnected,
            "DESCONECTADO",
            CyberMagenta
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Estado de Red",
            colorFilter = ColorFilter.tint(color),
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = label,
            color = color,
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        if (!networkStatus.ipAddress.isNullOrBlank() && networkStatus.isConnected) {
            Text(
                text = "| ${networkStatus.ipAddress}",
                color = CyberCyan.copy(alpha = 0.6f),
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 11.sp
            )
        }
    }
}