package com.launcher.samiboxtv.data.repository

import com.launcher.samiboxtv.core.dispatcher.DispatcherProvider
import com.launcher.samiboxtv.data.datasource.NetworkDataSource
import com.launcher.samiboxtv.domain.model.NetworkStatus
import com.launcher.samiboxtv.domain.repository.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * Implementación del repositorio de red siguiendo Clean Architecture.
 */
class NetworkRepositoryImpl(
    private val networkDataSource: NetworkDataSource,
    private val dispatcherProvider: DispatcherProvider
) : NetworkRepository {

    override fun observeNetworkStatus(): Flow<NetworkStatus> {
        return networkDataSource.observeNetworkStatus()
            .flowOn(dispatcherProvider.io)
    }

    override fun getCurrentNetworkStatus(): NetworkStatus {
        return networkDataSource.getCurrentNetworkStatus()
    }
}
