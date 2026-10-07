package com.launcher.samiboxtv.domain.model

/**
 * Representa los diferentes tipos de interfaz de conexión de red soportados por Android TV.
 */
enum class NetworkType {
    ETHERNET,
    WIFI,
    CELLULAR,
    DISCONNECTED,
    UNKNOWN
}

/**
 * Estado inmutable de la conexión de red del dispositivo.
 *
 * @property isConnected Indica si hay conexión de red activa a internet.
 * @property type Tipo de interfaz de red activa (ETHERNET, WIFI, etc.).
 * @property ipAddress Dirección IP local del dispositivo (IPv4), si está disponible.
 * @property ssid Nombre de la red Wi-Fi si aplica y está disponible.
 */
data class NetworkStatus(
    val isConnected: Boolean = false,
    val type: NetworkType = NetworkType.DISCONNECTED,
    val ipAddress: String? = null,
    val ssid: String? = null
)
