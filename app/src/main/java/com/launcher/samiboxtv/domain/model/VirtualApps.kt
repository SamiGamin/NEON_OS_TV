package com.launcher.samiboxtv.domain.model

object VirtualApps {
    const val PKG_MEDIA_HUB = "com.launcher.samiboxtv.virtual.mediahub"
    const val PKG_IPTV = "com.launcher.samiboxtv.virtual.iptv"
    const val DEFAULT_IPTV_URL = "https://m3u.cl/lista/total.m3u"

    fun isVirtual(packageName: String): Boolean =
        packageName == PKG_MEDIA_HUB || packageName == PKG_IPTV
}