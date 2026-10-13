package com.launcher.samiboxtv

import com.launcher.samiboxtv.util.CategoryHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryHelperTest {

    @Test
    fun getCategoryIcon_returnsCorrectIcons() {
        assertEquals("🎬", CategoryHelper.getCategoryIcon("STREAMING"))
        assertEquals("🎬", CategoryHelper.getCategoryIcon("PELICULAS"))
        assertEquals("🎮", CategoryHelper.getCategoryIcon("GAMING"))
        assertEquals("🎮", CategoryHelper.getCategoryIcon("JUEGOS"))
        assertEquals("🎵", CategoryHelper.getCategoryIcon("MUSICA"))
        assertEquals("📡", CategoryHelper.getCategoryIcon("IPTV"))
        assertEquals("⚙", CategoryHelper.getCategoryIcon("SISTEMA"))
        assertEquals("★", CategoryHelper.getCategoryIcon("FAVORITOS"))
        assertEquals("▦", CategoryHelper.getCategoryIcon("APPS"))
        assertEquals("❖", CategoryHelper.getCategoryIcon("OTRA_COSA"))
    }

    @Test
    fun detectCategory_streamingSignatures() {
        assertEquals("STREAMING", CategoryHelper.detectCategory(null, "Netflix TV", "com.netflix.ninja"))
        assertEquals("STREAMING", CategoryHelper.detectCategory(null, "YouTube", "com.google.android.youtube.tv"))
        assertEquals("STREAMING", CategoryHelper.detectCategory(null, "Spotify", "com.spotify.tv.android"))
        assertEquals("STREAMING", CategoryHelper.detectCategory(null, "SmartTube", "com.teamsmart.videomanager.tv"))
        assertEquals("STREAMING", CategoryHelper.detectCategory(null, "TiviMate", "ar.tvplayer.tv"))
    }

    @Test
    fun detectCategory_gamingSignatures() {
        assertEquals("GAMING", CategoryHelper.detectCategory(null, "RetroArch", "com.retroarch"))
        assertEquals("GAMING", CategoryHelper.detectCategory(null, "PPSSPP", "org.ppsspp.ppsspp"))
        assertEquals("GAMING", CategoryHelper.detectCategory(null, "Minecraft", "com.mojang.minecraftpe"))
    }

    @Test
    fun detectCategory_fallbackToApps() {
        assertEquals("APPS", CategoryHelper.detectCategory(null, "Calculadora", "com.example.calculator"))
        assertEquals("APPS", CategoryHelper.detectCategory(null, "Chrome", "com.android.chrome"))
    }
}
