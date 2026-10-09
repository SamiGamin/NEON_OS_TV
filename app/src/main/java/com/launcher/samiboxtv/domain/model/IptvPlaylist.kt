package com.launcher.samiboxtv.domain.model

data class IptvPlaylist(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val url: String,
    val isDefault: Boolean = false
)