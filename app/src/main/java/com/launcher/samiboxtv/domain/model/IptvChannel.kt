package com.launcher.samiboxtv.domain.model

data class IptvChannel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val groupTitle: String = "GENERAL", // Ej: DEPORTES, NOTICIAS, CINE
    val channelNumber: Int? = null
)