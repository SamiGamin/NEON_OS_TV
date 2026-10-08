package com.launcher.samiboxtv.util

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

object DevLogManager {

    private const val MAX_LOGS = 250
    private val logBuffer = ArrayDeque<String>(MAX_LOGS)
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private var serverJob: Job? = null
    private var serverSocket: ServerSocket? = null
    const val SERVER_PORT = 8080

    @Synchronized
    fun log(tag: String, message: String) {
        val timestamp = dateFormat.format(Date())
        val entry = "[$timestamp] [$tag] $message"
        if (logBuffer.size >= MAX_LOGS) {
            logBuffer.removeFirst()
        }
        logBuffer.addLast(entry)
    }

    @Synchronized
    fun getLogsHtml(): String {
        val lines = logBuffer.joinToString("<br>") { line ->
            when {
                line.contains("[KEY]") -> "<span style='color: #00F0FF;'>$line</span>"
                line.contains("[ERROR]") -> "<span style='color: #FF0055; font-weight: bold;'>$line</span>"
                line.contains("[RAM]") -> "<span style='color: #FFB800;'>$line</span>"
                else -> "<span style='color: #A6C5E2;'>$line</span>"
            }
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>NEONOS TV // Consola de Depuración</title>
                <meta http-equiv="refresh" content="2">
                <style>
                    body { background: #070B16; color: #FFF; font-family: monospace; padding: 20px; }
                    h2 { color: #00F0FF; margin-bottom: 5px; }
                    .hud { background: #0F172B; padding: 10px; border-left: 4px solid #FFB800; margin-bottom: 15px; }
                    .log-box { background: #040711; border: 1px solid #1A2848; padding: 15px; border-radius: 6px; line-height: 1.6; }
                </style>
            </head>
            <body>
                <h2>⚡ NEONOS TV — MONITOR DE RED REMOTO</h2>
                <div class="hud">Auto-recarga cada 2s | Últimos $MAX_LOGS eventos capturados</div>
                <div class="log-box">$lines</div>
                <script>window.scrollTo(0, document.body.scrollHeight);</script>
            </body>
            </html>
        """.trimIndent()
    }

    fun startServer(scope: CoroutineScope) {
        if (serverJob != null) return
        serverJob = scope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                log("SYSTEM", "Servidor de depuración iniciado en el puerto $SERVER_PORT")

                while (isActive) {
                    val client = serverSocket?.accept() ?: break
                    handleClient(client)
                }
            } catch (e: Exception) {
                log("ERROR", "Servidor detenido: ${e.message}")
            }
        }
    }

    fun stopServer() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverJob?.cancel()
        serverJob = null
        serverSocket = null
        log("SYSTEM", "Servidor de depuración apagado")
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(socket.getOutputStream(), true)

            // Leer headers HTTP básicos
            val line = reader.readLine()
            if (line != null && line.startsWith("GET")) {
                val html = getLogsHtml()
                val bytes = html.toByteArray(Charsets.UTF_8)

                writer.println("HTTP/1.1 200 OK")
                writer.println("Content-Type: text/html; charset=utf-8")
                writer.println("Content-Length: ${bytes.size}")
                writer.println("Connection: close")
                writer.println()
                writer.flush()
                socket.getOutputStream().write(bytes)
            }
            socket.close()
        } catch (_: Exception) {}
    }

    @Suppress("DEPRECATION")
    fun getLocalIpAddress(): String {
        return try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addresses = Collections.list(intf.inetAddresses)
                for (addr in addresses) {
                    // Filtramos localhost (127.0.0.1) y nos quedamos con la IPv4 de la red local
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "Desconocida"
                    }
                }
            }
            "Desconocida"
        } catch (_: Exception) {
            "Desconocida"
        }
    }
}