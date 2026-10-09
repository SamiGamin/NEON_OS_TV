package com.launcher.samiboxtv.util

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
import java.net.URLDecoder

object DevLogManager {

    const val SERVER_PORT = 8080
    var onM3uUrlReceived: ((String) -> Unit)? = null

    private var serverJob: Job? = null
    private var serverSocket: ServerSocket? = null

    fun log(tag: String, message: String) {
        // Mantenido para trazas de depuración interna y Logcat
        android.util.Log.d(tag, message)
    }

    fun startServer(scope: CoroutineScope) {
        if (serverJob != null && serverSocket?.isClosed == false) return

        serverJob = scope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(SERVER_PORT)
                while (isActive && serverSocket?.isClosed == false) {
                    val client = serverSocket?.accept() ?: break
                    launch(Dispatchers.IO) {
                        handleClient(client)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun stopServer() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverJob?.cancel()
        serverSocket = null
        serverJob = null
    }

    private fun handleClient(client: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(client.getInputStream()))
            val out = PrintWriter(client.getOutputStream(), true)

            val line = reader.readLine() ?: return
            val parts = line.split(" ")
            if (parts.size < 2) return

            val method = parts[0]
            val path = parts[1]

            if (path.startsWith("/api/m3u") || path.startsWith("/m3u")) {
                var receivedUrl = ""
                if (method == "POST") {
                    var contentLength = 0
                    var headerLine = reader.readLine()
                    while (!headerLine.isNullOrBlank()) {
                        if (headerLine.startsWith("Content-Length:", ignoreCase = true)) {
                            contentLength = headerLine.substringAfter(":").trim().toIntOrNull() ?: 0
                        }
                        headerLine = reader.readLine()
                    }
                    val bodyChars = CharArray(contentLength)
                    reader.read(bodyChars, 0, contentLength)
                    val body = String(bodyChars)
                    receivedUrl = parseParam(body, "url")
                } else if (path.contains("?")) {
                    val query = path.substringAfter("?")
                    receivedUrl = parseParam(query, "url")
                }

                if (receivedUrl.isNotBlank()) {
                    val cleanUrl = URLDecoder.decode(receivedUrl, "UTF-8").trim()
                    onM3uUrlReceived?.invoke(cleanUrl)

                    val jsonResponse = """{"status":"ok","message":"Lista enviada correctamente a la TV"}"""
                    out.print("HTTP/1.1 200 OK\r\n")
                    out.print("Content-Type: application/json; charset=utf-8\r\n")
                    out.print("Access-Control-Allow-Origin: *\r\n")
                    out.print("Content-Length: ${jsonResponse.toByteArray().size}\r\n\r\n")
                    out.print(jsonResponse)
                    out.flush()
                } else {
                    out.print("HTTP/1.1 400 Bad Request\r\nContent-Length: 0\r\n\r\n")
                    out.flush()
                }
            } else {
                // Interfaz web optimizada para móvil (sin logs técnicos)
                val html = getCleanWebHtml()
                val htmlBytes = html.toByteArray(Charsets.UTF_8)
                out.print("HTTP/1.1 200 OK\r\n")
                out.print("Content-Type: text/html; charset=utf-8\r\n")
                out.print("Content-Length: ${htmlBytes.size}\r\n\r\n")
                out.print(html)
                out.flush()
            }
            client.close()
        } catch (_: Exception) {}
    }

    private fun parseParam(queryOrBody: String, key: String): String {
        return queryOrBody.split("&")
            .firstOrNull { it.startsWith("$key=") }
            ?.substringAfter("$key=")
            ?.trim() ?: ""
    }

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {}
        return "127.0.0.1"
    }

    private fun getCleanWebHtml(): String {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>NEON OS TV - Asistente</title>
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
                body { background-color: #060913; color: #E0E6ED; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 100vh; padding: 20px; }
                .card { background: #0D1527; border: 1.5px solid #00F0FF; border-radius: 16px; width: 100%; max-width: 480px; padding: 28px; box-shadow: 0 10px 30px rgba(0, 240, 255, 0.15); }
                h1 { color: #00F0FF; font-size: 22px; margin-bottom: 6px; letter-spacing: 1px; text-transform: uppercase; }
                p { color: #8EA5BE; font-size: 13px; margin-bottom: 22px; line-height: 1.4; }
                label { display: block; font-size: 11px; font-weight: bold; color: #FFAA00; text-transform: uppercase; margin-bottom: 6px; }
                input[type="text"] { width: 100%; padding: 14px; background: #070B14; border: 1.5px solid #1A2E4C; border-radius: 10px; color: #FFF; font-size: 14px; margin-bottom: 20px; outline: none; transition: 0.2s; }
                input[type="text"]:focus { border-color: #00F0FF; }
                button { width: 100%; padding: 15px; background: #00F0FF; color: #060913; border: none; border-radius: 10px; font-size: 15px; font-weight: bold; cursor: pointer; text-transform: uppercase; letter-spacing: 1px; transition: 0.2s; }
                button:active { transform: scale(0.98); background: #FFAA00; }
                .status { margin-top: 16px; padding: 12px; border-radius: 8px; font-size: 13px; text-align: center; display: none; }
                .status.success { display: block; background: rgba(0, 230, 118, 0.15); border: 1px solid #00E676; color: #00E676; }
                .status.error { display: block; background: rgba(255, 82, 82, 0.15); border: 1px solid #FF5252; color: #FF5252; }
            </style>
        </head>
        <body>
            <div class="card">
                <h1>📺 NEON OS TV</h1>
                <p>Pega el enlace de tu lista M3U para enviarla y activarla en tu televisor.</p>
                <form id="iptvForm">
                    <label>Enlace M3U (HTTP / HTTPS)</label>
                    <input type="text" id="m3uUrl" placeholder="https://ejemplo.com/lista.m3u" required autocomplete="off" autocorrect="off">
                    <button type="submit" id="btnSend">⚡ Enviar a la TV</button>
                </form>
                <div id="statusBox" class="status"></div>
            </div>
            <script>
                document.getElementById('iptvForm').addEventListener('submit', async (e) => {
                    e.preventDefault();
                    const urlInput = document.getElementById('m3uUrl');
                    const btn = document.getElementById('btnSend');
                    const status = document.getElementById('statusBox');
                    const url = urlInput.value.trim();

                    if (!url) return;

                    btn.disabled = true;
                    btn.innerText = 'Enviando...';
                    status.className = 'status';

                    try {
                        const res = await fetch('/api/m3u?url=' + encodeURIComponent(url));
                        if (res.ok) {
                            status.className = 'status success';
                            status.innerText = '✓ ¡Lista enviada y activada con éxito en tu TV!';
                            urlInput.value = '';
                        } else {
                            throw new Error('Respuesta del servidor no válida');
                        }
                    } catch (err) {
                        status.className = 'status error';
                        status.innerText = '✕ Error al enviar la lista. Comprueba la conexión.';
                    } finally {
                        btn.disabled = false;
                        btn.innerText = '⚡ Enviar a la TV';
                    }
                });
            </script>
        </body>
        </html>
        """.trimIndent()
    }
}