package com.launcher.samiboxtv

import com.launcher.samiboxtv.data.parser.M3uParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class M3uParserTest {

    @Test
    fun parseStream_parsesStandardM3uCorrectly() = runBlocking {
        val m3uContent = """
            #EXTM3U
            #EXTINF:-1 tvg-logo="https://example.com/logo1.png" group-title="NOTICIAS" tvg-chno="1",Canal 1 HD
            http://example.com/stream1.m3u8
            #EXTINF:-1 tvg-logo="https://example.com/logo2.png" group-title="DEPORTES" tvg-chno="2",Canal 2 Deportes
            http://example.com/stream2.m3u8
        """.trimIndent()

        val inputStream = ByteArrayInputStream(m3uContent.toByteArray(Charsets.UTF_8))
        val channels = M3uParser.parseStream(inputStream)

        assertEquals(2, channels.size)

        val first = channels[0]
        assertEquals("Canal 1 HD", first.name)
        assertEquals("http://example.com/stream1.m3u8", first.streamUrl)
        assertEquals("https://example.com/logo1.png", first.logoUrl)
        assertEquals("NOTICIAS", first.groupTitle)
        assertEquals(1, first.channelNumber)
        assertTrue(first.id.isNotBlank())

        val second = channels[1]
        assertEquals("Canal 2 Deportes", second.name)
        assertEquals("http://example.com/stream2.m3u8", second.streamUrl)
        assertEquals("https://example.com/logo2.png", second.logoUrl)
        assertEquals("DEPORTES", second.groupTitle)
        assertEquals(2, second.channelNumber)
    }

    @Test
    fun parseStream_handlesCountryPipeFormat() = runBlocking {
        val m3uContent = """
            #EXTM3U
            #EXTINF:-1,Telemedellin | CO
            http://example.com/tm.m3u8
        """.trimIndent()

        val inputStream = ByteArrayInputStream(m3uContent.toByteArray(Charsets.UTF_8))
        val channels = M3uParser.parseStream(inputStream)

        assertEquals(1, channels.size)
        val channel = channels[0]
        assertEquals("Telemedellin", channel.name)
        assertEquals("COLOMBIA", channel.groupTitle)
    }

    @Test
    fun parseStream_emptyStreamReturnsEmptyList() = runBlocking {
        val inputStream = ByteArrayInputStream("".toByteArray(Charsets.UTF_8))
        val channels = M3uParser.parseStream(inputStream)
        assertTrue(channels.isEmpty())
    }
}
