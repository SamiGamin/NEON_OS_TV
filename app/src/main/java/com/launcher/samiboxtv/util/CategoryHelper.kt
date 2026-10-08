package com.launcher.samiboxtv.util

import android.content.pm.ApplicationInfo

/**
 * Utilidad para categorización automática de aplicaciones y gestión de iconos
 * temáticos visuales para cada fila de la pantalla principal y panel de ajustes en Android TV.
 */
object CategoryHelper {

    /**
     * Retorna un icono o símbolo distintivo para cada categoría.
     * Soporta categorías estándar del sistema (STREAMING, GAMING, APPS)
     * y categorías temáticas personalizadas creadas por el usuario.
     */
    fun getCategoryIcon(categoryName: String): String {
        val upper = categoryName.trim().uppercase()
        return when {
            upper.contains("STREAM") || upper.contains("CINE") || upper.contains("PELI") ||
            upper.contains("VIDEO") || upper.contains("MOVIE") || upper.contains("FILM") -> "🎬"

            upper.contains("GAME") || upper.contains("GAMING") || upper.contains("JUEGO") ||
            upper.contains("EMULA") || upper.contains("RETRO") || upper.contains("ARCADE") -> "🎮"

            upper.contains("MUSIC") || upper.contains("MÚSICA") || upper.contains("AUDIO") ||
            upper.contains("SOUND") || upper.contains("SONIDO") || upper.contains("RADIO") -> "🎵"

            upper.contains("IPTV") || upper.contains("TV") || upper.contains("CANAL") ||
            upper.contains("CABLE") || upper.contains("TELE") || upper.contains("EN VIVO") -> "📡"

            upper.contains("TOOL") || upper.contains("HERRAMIENTA") || upper.contains("SISTEMA") ||
            upper.contains("AJUSTE") || upper.contains("UTIL") || upper.contains("CONFIG") -> "⚙"

            upper.contains("DEPORTE") || upper.contains("SPORT") || upper.contains("FUTBOL") ||
            upper.contains("SOCCER") -> "⚽"

            upper.contains("NOTICIA") || upper.contains("NEWS") || upper.contains("INFORMA") -> "📰"

            upper.contains("KID") || upper.contains("NIÑO") || upper.contains("INFANTIL") ||
            upper.contains("FAMILI") || upper.contains("DIBUJO") -> "🧸"

            upper.contains("FAV") || upper.contains("DESTACAD") -> "★"

            upper == "APPS" || upper.contains("APLICACI") -> "▦"

            else -> "❖"
        }
    }

    /**
     * Clasifica automáticamente una aplicación instalada en STREAMING, GAMING o APPS
     * mediante la API nativa de Android (API 26+) y un diccionario de firmas conocidas en Android TV.
     * Garantiza que en una instalación limpia ninguna app quede huérfana o sin categoría.
     */
    fun detectCategory(appInfo: ApplicationInfo?, appName: String, packageName: String): String {
        // 1. Detección nativa por categoría oficial de Android (API 26+)
        if (appInfo != null) {
            if (appInfo.category == ApplicationInfo.CATEGORY_GAME ||
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            ) {
                return "GAMING"
            }
            if (appInfo.category == ApplicationInfo.CATEGORY_VIDEO ||
                appInfo.category == ApplicationInfo.CATEGORY_AUDIO
            ) {
                return "STREAMING"
            }
        }

        val pkg = packageName.lowercase()
        val name = appName.lowercase()

        // 2. Firmas reconocidas de Streaming, TV, Reproductores y Multimedia
        val streamingSignatures = listOf(
            "netflix", "youtube", "smarttube", "amazonvideo", "primevideo", "avod",
            "disney", "hbo", "max.stream", "wbd.stream", "starplus", "spotify",
            "kodi", "xbmc", "videolan", "vlc", "magis", "magistv", "plex",
            "twitch", "pluto", "crunchyroll", "appletv", "tivimate", "iptv",
            "stremio", "directv", "dgo", "clarovideo", "movistarplay", "movistar",
            "vix", "rtve", "atresplayer", "mitele", "paramount", "peacock",
            "deezer", "tidal", "soundcloud", "tunein", "mxplayer", "novavideo",
            "justplayer", "ssiptv", "ottplayer", "tubi", "vimeo", "dailymotion",
            "cinetv", "cuevana", "reproductor", "stream", "cinema", "pelicula",
            "player", "media", "video"
        )
        if (streamingSignatures.any { pkg.contains(it) || name.contains(it) }) {
            return "STREAMING"
        }

        // 3. Firmas reconocidas de Videojuegos, Emuladores y Cloud Gaming
        val gamingSignatures = listOf(
            "retroarch", "ppsspp", "duckstation", "dolphin", "aethersx2", "mupen64",
            "drastic", "snes9x", "fba4droid", "mame4droid", "steamlink", "geforcenow",
            "moonlight", "xbox", "playstation", "stadia", "roblox", "minecraft",
            "asphalt", "gameloft", "game", "juego", "arcade", "emulator", "emulador",
            "playstore.games", "sega", "nintendo", "citra", "yuzu", "vita3k",
            "pacman", "sonic", "mario"
        )
        if (gamingSignatures.any { pkg.contains(it) || name.contains(it) }) {
            return "GAMING"
        }

        // 4. Categoría general por defecto
        return "APPS"
    }
}
