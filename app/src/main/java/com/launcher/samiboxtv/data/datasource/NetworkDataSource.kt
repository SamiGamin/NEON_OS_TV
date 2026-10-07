package com.launcher.samiboxtv.data.datasource

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.domain.model.NetworkType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

/**
 * Fuente de datos para la telemetría de red del sistema.
 */
interface NetworkDataSource {
    fun observeNetworkStatus(): Flow<NetworkStatus>
    fun getCurrentNetworkStatus(): NetworkStatus
}

class NetworkDataSourceImpl(
    private val context: Context
) : NetworkDataSource {

    private val connectivityManager: ConnectivityManager? by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }

    override fun getCurrentNetworkStatus(): NetworkStatus {
        val cm = connectivityManager ?: return NetworkStatus(isConnected = false, type = NetworkType.DISCONNECTED)
        val activeNetwork = cm.activeNetwork ?: return NetworkStatus(isConnected = false, type = NetworkType.DISCONNECTED)
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus(isConnected = false, type = NetworkType.DISCONNECTED)

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val type = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            else -> if (hasInternet) NetworkType.UNKNOWN else NetworkType.DISCONNECTED
        }

        val localIp = getLocalIpAddress()

        return NetworkStatus(
            isConnected = type != NetworkType.DISCONNECTED,
            type = type,
            ipAddress = localIp
        )
    }

    override fun observeNetworkStatus(): Flow<NetworkStatus> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            trySend(NetworkStatus(isConnected = false, type = NetworkType.DISCONNECTED))
            close()
            return@callbackFlow
        }

        // Emite el estado inicial inmediatamente
        trySend(getCurrentNetworkStatus())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getCurrentNetworkStatus())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(getCurrentNetworkStatus())
            }

            override fun onLost(network: Network) {
                trySend(getCurrentNetworkStatus())
            }

            override fun onUnavailable() {
                trySend(getCurrentNetworkStatus())
            }
        }

        try {
            cm.registerDefaultNetworkCallback(callback)
        } catch (_: Exception) {
            trySend(getCurrentNetworkStatus())
        }

        awaitClose {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (_: Exception) {
                // Silently handle unregister errors if already detached
            }
        }
    }.distinctUntilChanged()

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            for (networkInterface in Collections.list(interfaces)) {
                if (!networkInterface.isUp || networkInterface.isLoopback) continue
                val addresses = networkInterface.inetAddresses
                for (inetAddress in Collections.list(addresses)) {
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        val host = inetAddress.hostAddress
                        if (!host.isNullOrBlank()) {
                            return host
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignorado de forma segura
        }
        return null
    }
}
