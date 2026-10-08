package com.launcher.samiboxtv.data.repository

import com.launcher.samiboxtv.data.parser.M3uParser
import com.launcher.samiboxtv.domain.model.IptvChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class IptvRepository {

    // Carga desde archivo local o USB
    suspend fun loadFromFile(file: File): List<IptvChannel> = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext emptyList()
        file.inputStream().use { stream ->
            M3uParser.parseStream(stream)
        }
    }

    // Carga desde enlace web remoto (URL)
    suspend fun loadFromUrl(urlString: String): List<IptvChannel> = withContext(Dispatchers.IO) {
        val connection = URL(urlString).openConnection()
        connection.connectTimeout = 10000
        connection.readTimeout = 15000
        connection.getInputStream().use { stream ->
            M3uParser.parseStream(stream)
        }
    }
}