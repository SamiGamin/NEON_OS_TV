package com.launcher.samiboxtv.data.parser

import com.launcher.samiboxtv.domain.model.IptvChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

object M3uParser {

    private val LOGO_REGEX = Regex("""tvg-logo="([^"]*)"""")
    private val GROUP_REGEX = Regex("""group-title="([^"]*)"""")
    private val CH_NO_REGEX = Regex("""tvg-chno="([^"]*)"""")
    private val COUNTRY_PIPE_REGEX = Regex("""\|\s*([A-Za-z]{2,3})\s*$""")

    private val COUNTRY_MAP = mapOf(
        "VE" to "VENEZUELA",
        "CO" to "COLOMBIA",
        "CL" to "CHILE",
        "AR" to "ARGENTINA",
        "MX" to "MÉXICO",
        "PE" to "PERÚ",
        "ES" to "ESPAÑA",
        "US" to "ESTADOS UNIDOS",
        "USA" to "ESTADOS UNIDOS",
        "UY" to "URUGUAY",
        "EC" to "ECUADOR",
        "PY" to "PARAGUAY",
        "BO" to "BOLIVIA",
        "BR" to "BRASIL",
        "CR" to "COSTA RICA",
        "PA" to "PANAMÁ",
        "DO" to "REP. DOMINICANA",
        "GT" to "GUATEMALA",
        "HN" to "HONDURAS",
        "SV" to "EL SALVADOR",
        "NI" to "NICARAGUA",
        "PR" to "PUERTO RICO",
        "CU" to "CUBA",
        "IT" to "ITALIA",
        "FR" to "FRANCIA",
        "UK" to "REINO UNIDO",
        "GB" to "REINO UNIDO",
        "DE" to "ALEMANIA",
        "PT" to "PORTUGAL",
        "CA" to "CANADÁ"
    )

    suspend fun parseStream(inputStream: InputStream): List<IptvChannel> = withContext(Dispatchers.IO) {
        val channels = ArrayList<IptvChannel>()
        val reader = BufferedReader(InputStreamReader(inputStream))

        var currentName = ""
        var currentLogo: String? = null
        var currentGroup = "GENERAL"
        var currentChNo: Int? = null

        var line: String? = reader.readLine()
        while (line != null) {
            val trimmed = line.trim()

            if (trimmed.startsWith("#EXTINF:")) {
                // 1. Extraer Atributos del tag #EXTINF
                currentLogo = LOGO_REGEX.find(trimmed)?.groupValues?.get(1)?.ifBlank { null }
                currentGroup = GROUP_REGEX.find(trimmed)?.groupValues?.get(1)?.ifBlank { "GENERAL" } ?: "GENERAL"
                currentChNo = CH_NO_REGEX.find(trimmed)?.groupValues?.get(1)?.toIntOrNull()

                // 2. Extraer el nombre del canal (lo que va después de la última coma)
                val commaIndex = trimmed.lastIndexOf(',')
                currentName = if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else {
                    "CANAL SIN NOMBRE"
                }
            } else if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                // Es la URL del stream
                if (currentName.isNotEmpty()) {
                    val (finalName, finalGroup) = resolveNameAndGroup(currentName, currentGroup)
                    channels.add(
                        IptvChannel(
                            id = UUID.randomUUID().toString(),
                            name = finalName,
                            streamUrl = trimmed,
                            logoUrl = currentLogo,
                            groupTitle = finalGroup,
                            channelNumber = currentChNo ?: (channels.size + 1)
                        )
                    )
                }
                // Limpiar temporales para el siguiente canal
                currentName = ""
                currentLogo = null
                currentGroup = "GENERAL"
                currentChNo = null
            }

            line = reader.readLine()
        }

        reader.close()
        channels
    }

    private fun resolveNameAndGroup(rawName: String, rawGroup: String): Pair<String, String> {
        val trimmedName = rawName.trim()
        val isGenericGroup = rawGroup.isBlank() || rawGroup.equals("GENERAL", ignoreCase = true)

        if (isGenericGroup) {
            val match = COUNTRY_PIPE_REGEX.find(trimmedName)
            if (match != null) {
                val code = match.groupValues[1].uppercase()
                val countryName = COUNTRY_MAP[code] ?: code
                val cleanedName = trimmedName.substringBeforeLast("|").trim()
                val finalName = if (cleanedName.isNotEmpty()) cleanedName else trimmedName
                return Pair(finalName, countryName)
            } else {
                return Pair(trimmedName, "VARIADOS")
            }
        }

        return Pair(trimmedName, rawGroup.uppercase())
    }
}