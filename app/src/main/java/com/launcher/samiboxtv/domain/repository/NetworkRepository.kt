package com.launcher.samiboxtv.domain.repository

import com.launcher.samiboxtv.domain.model.NetworkStatus
import kotlinx.coroutines.flow.Flow

/**
 * Contrato del repositorio para la observación del estado de conectividad en tiempo real.
 */
interface NetworkRepository {
    /**
     * Emite actualizaciones continuas del estado y tipo de red activa (Wi-Fi, Ethernet, Desconectado).
     */
    fun observeNetworkStatus(): Flow<NetworkStatus>

    /**
     * Obtiene una captura sincrónica inmediata del estado actual de red.
     */
    fun getCurrentNetworkStatus(): NetworkStatus
}
