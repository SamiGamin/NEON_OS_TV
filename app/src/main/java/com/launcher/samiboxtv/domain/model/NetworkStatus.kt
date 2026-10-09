package com.launcher.samiboxtv.domain.model

import androidx.compose.runtime.Immutable

enum class NetworkType {
    ETHERNET,
    WIFI,
    CELLULAR,
    DISCONNECTED,
    UNKNOWN
}

@Immutable
data class NetworkStatus(
    val isConnected: Boolean = false,
    val hasInternetAccess: Boolean = false,
    val type: NetworkType = NetworkType.DISCONNECTED,
    val ipAddress: String? = null,
    val ssid: String? = null,
    val linkSpeedMbps: Int? = null,
    val wifiFrequencyGhz: String? = null,
    val signalLevel: Int = 0
) {
    val hudDisplayString: String
        get() = when {
            !isConnected -> "SYS // OFFLINE"
            !hasInternetAccess -> "${type.name} // NO_ROUTE"
            type == NetworkType.ETHERNET -> {
                val speed = linkSpeedMbps?.let { " [${it}M]" } ?: ""
                "ETH$speed // ${ipAddress ?: "0.0.0.0"}"
            }
            type == NetworkType.WIFI -> {
                val band = wifiFrequencyGhz?.let { " $it" } ?: ""
                "WIFI$band // ${ipAddress ?: "0.0.0.0"}"
            }
            else -> "${type.name} // ${ipAddress ?: "ONLINE"}"
        }
}