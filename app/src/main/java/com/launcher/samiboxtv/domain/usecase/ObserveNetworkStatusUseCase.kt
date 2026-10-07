package com.launcher.samiboxtv.domain.usecase

import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para observar el estado y tipo de red (Wi-Fi, Ethernet, Desconectado)
 * en tiempo real dentro del Launcher.
 */
class ObserveNetworkStatusUseCase(
    private val networkRepository: NetworkRepository
) {
    operator fun invoke(): Flow<NetworkStatus> = networkRepository.observeNetworkStatus()
}
