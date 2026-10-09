package com.launcher.samiboxtv.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.domain.model.NetworkType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.Inet4Address
import java.net.NetworkInterface

class NetworkMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    val networkStatus: Flow<NetworkStatus> = callbackFlow {
        fun resolveStatus(): NetworkStatus {
            val activeNetwork = connectivityManager.activeNetwork ?: return NetworkStatus()
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus()

            val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            val ip = getLocalIpv4()
            val speedMbps = caps.linkDownstreamBandwidthKbps.takeIf { it > 0 }?.let { it / 1000 }

            return when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    NetworkStatus(
                        isConnected = true,
                        hasInternetAccess = hasInternet,
                        type = NetworkType.ETHERNET,
                        ipAddress = ip,
                        linkSpeedMbps = speedMbps
                    )
                }
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    val (band, signalLevel, ssid) = extractWifiTelemetry(caps)
                    NetworkStatus(
                        isConnected = true,
                        hasInternetAccess = hasInternet,
                        type = NetworkType.WIFI,
                        ipAddress = ip,
                        ssid = ssid,
                        linkSpeedMbps = speedMbps,
                        wifiFrequencyGhz = band,
                        signalLevel = signalLevel
                    )
                }
                else -> NetworkStatus(
                    isConnected = true,
                    hasInternetAccess = hasInternet,
                    type = NetworkType.UNKNOWN,
                    ipAddress = ip
                )
            }
        }

        // Emitir estado inicial
        trySend(resolveStatus())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(resolveStatus())
            }

            override fun onLost(network: Network) {
                trySend(resolveStatus())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(resolveStatus())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    private fun extractWifiTelemetry(caps: NetworkCapabilities): Triple<String?, Int, String?> {
        var band: String? = null
        var level = 3
        var ssid: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (caps.transportInfo as? WifiInfo)?.let { info ->
                val freq = info.frequency
                band = if (freq in 4900..5900) "5GHz" else if (freq in 2400..2500) "2.4GHz" else null
                level = WifiManager.calculateSignalLevel(info.rssi, 5)
                ssid = info.ssid?.trim('"', ' ')?.takeIf { it != "<unknown ssid>" }
            }
        } else {
            wifiManager?.connectionInfo?.let { info ->
                val freq = info.frequency
                band = if (freq in 4900..5900) "5GHz" else if (freq in 2400..2500) "2.4GHz" else null
                level = WifiManager.calculateSignalLevel(info.rssi, 5)
            }
        }
        return Triple(band, level, ssid)
    }

    private fun getLocalIpv4(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
                ?.hostAddress
        } catch (_: Exception) {
            null
        }
    }
}