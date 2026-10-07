package com.launcher.samiboxtv.domain.model

/**
 * Modelo de datos con la telemetría en tiempo real del hardware y sistema operativo del TV Box.
 *
 * @property ramUsedMb Memoria RAM actualmente ocupada en Megabytes.
 * @property ramTotalMb Memoria RAM física total del dispositivo en Megabytes.
 * @property ramAvailableMb Memoria RAM libre/disponible en Megabytes.
 * @property ramUsagePercentage Porcentaje de ocupación de RAM (0 a 100%).
 * @property isLowMemory Indica si el sistema operativo está bajo condición crítica de memoria baja.
 * @property storageUsedGb Espacio de almacenamiento interno utilizado en Gigabytes.
 * @property storageTotalGb Espacio de almacenamiento interno total en Gigabytes.
 * @property storageFreeGb Espacio libre de almacenamiento interno en Gigabytes.
 * @property cpuUsagePercentage Estimación de carga de CPU en porcentaje.
 * @property cpuTemperature Temperatura del procesador (°C) si el sensor térmico del kernel está expuesto.
 * @property uptime Tiempo que lleva el dispositivo encendido desde el último reinicio.
 * @property deviceModel Fabricante y modelo del dispositivo (e.g., Xiaomi Box, Rockchip TV Box).
 * @property androidVersion Versión de Android y nivel de API.
 */
data class SystemTelemetry(
    val ramUsedMb: Long = 0,
    val ramTotalMb: Long = 0,
    val ramAvailableMb: Long = 0,
    val ramUsagePercentage: Int = 0,
    val isLowMemory: Boolean = false,
    val storageUsedGb: Long = 0,
    val storageTotalGb: Long = 0,
    val storageFreeGb: Long = 0,
    val cpuUsagePercentage: String = "0%",
    val cpuTemperature: String? = null,
    val uptime: String = "0h 0m",
    val deviceModel: String = "",
    val androidVersion: String = ""
)
