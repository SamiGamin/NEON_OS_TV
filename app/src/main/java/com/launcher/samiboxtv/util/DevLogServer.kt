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

    var onM3uUrlReceived: ((String) -> Unit)? = null

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
    fun clearLogs() {
        logBuffer.clear()
        val timestamp = dateFormat.format(Date())
        logBuffer.addLast("[$timestamp] [SYSTEM] Búfer de logs vaciado por el usuario")
    }

    @Synchronized
    fun getRawLogs(): String {
        return logBuffer.joinToString("\n")
    }

    @Synchronized
    fun getLogsHtml(): String = getUnifiedWebHtml()

    private fun getUnifiedWebHtml(successMsg: String? = null): String {
        return """
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NEONOS TV // Terminal & Control IPTV</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, monospace; }
        body { background: #070B16; color: #FFF; display: flex; flex-direction: column; height: 100vh; overflow: hidden; }

        /* Barra Superior */
        .top-bar {
            background: #0B1120;
            border-bottom: 2px solid #00F0FF;
            padding: 12px 20px;
            display: flex;
            flex-direction: column;
            gap: 10px;
            box-shadow: 0 4px 25px rgba(0, 240, 255, 0.15);
            flex-shrink: 0;
        }
        .header-info {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 8px;
        }
        .brand {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 15px;
            font-weight: 800;
            letter-spacing: 1px;
            color: #00F0FF;
        }
        .badge {
            background: rgba(0, 240, 255, 0.15);
            border: 1px solid #00F0FF;
            color: #00F0FF;
            font-size: 11px;
            padding: 2px 8px;
            border-radius: 4px;
            font-weight: 700;
        }
        .toast {
            background: rgba(0, 255, 136, 0.15);
            border: 1px solid #00FF88;
            color: #00FF88;
            padding: 5px 12px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            display: none;
        }

        /* Formulario M3U */
        .form-row {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            align-items: center;
        }
        .url-input {
            flex: 1;
            min-width: 250px;
            padding: 10px 14px;
            background: #040711;
            border: 1.5px solid #1E2E4E;
            border-radius: 8px;
            color: #FFF;
            font-size: 13px;
            outline: none;
            transition: border-color 0.2s, box-shadow 0.2s;
        }
        .url-input:focus {
            border-color: #00F0FF;
            box-shadow: 0 0 10px rgba(0, 240, 255, 0.25);
        }
        .btn-send {
            padding: 10px 18px;
            background: #00F0FF;
            border: none;
            border-radius: 8px;
            color: #000;
            font-weight: 800;
            font-size: 12px;
            cursor: pointer;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            transition: background 0.2s;
            white-space: nowrap;
        }
        .btn-send:hover { background: #50F7FF; }
        .btn-clear {
            padding: 10px 16px;
            background: #141E33;
            border: 1.5px solid #FFB800;
            border-radius: 8px;
            color: #FFB800;
            font-weight: 700;
            font-size: 12px;
            cursor: pointer;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            transition: all 0.2s;
            white-space: nowrap;
        }
        .btn-clear:hover {
            background: rgba(255, 184, 0, 0.2);
        }

        /* Cuerpo Principal / Terminal */
        .terminal-container {
            flex: 1;
            display: flex;
            flex-direction: column;
            padding: 12px 20px 16px 20px;
            min-height: 0;
            background: #070B16;
        }
        .terminal-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
            font-size: 12px;
            color: #A6C5E2;
            flex-shrink: 0;
            flex-wrap: wrap;
            gap: 8px;
        }
        .legend {
            display: flex;
            gap: 12px;
            font-size: 11px;
            flex-wrap: wrap;
        }
        .dot-key { color: #00FF88; font-weight: bold; }
        .dot-iptv { color: #FF007F; font-weight: bold; }
        .dot-sys { color: #00F0FF; font-weight: bold; }
        .dot-dev { color: #FFB800; font-weight: bold; }

        .terminal-box {
            flex: 1;
            background: #040711;
            border: 1.5px solid #14223E;
            border-radius: 8px;
            padding: 14px;
            overflow-y: auto;
            font-family: 'Consolas', 'Menlo', 'Monaco', 'Courier New', monospace;
            font-size: 12px;
            line-height: 1.6;
            box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.6);
        }
        .log-line {
            margin-bottom: 2px;
            word-break: break-all;
            white-space: pre-wrap;
        }
    </style>
</head>
<body>
    <div class="top-bar">
        <div class="header-info">
            <div class="brand">
                <span>⚡ NEONOS TV // CONTROL & MONITOREO</span>
                <span class="badge">PUERTO $SERVER_PORT</span>
            </div>
            <div id="toast" class="toast"></div>
        </div>
        <form id="m3uForm" class="form-row" method="GET" action="/">
            <input type="url" id="m3uInput" name="set_m3u" class="url-input" placeholder="Pega aquí la URL de la lista M3U (ej: https://m3u.cl/lista/...)" required autocomplete="off" />
            <button type="submit" id="submitBtn" class="btn-send">ENVIAR A LA TV</button>
            <button type="button" class="btn-clear" onclick="clearLogs()">BORRAR LOGS</button>
        </form>
    </div>

    <div class="terminal-container">
        <div class="terminal-meta">
            <div>
                <strong>TERMINAL EN VIVO</strong> // Polling 800ms // <span id="eventCount">0</span> eventos
            </div>
            <div class="legend">
                <span class="dot-key">● [KEY] Teclas</span>
                <span class="dot-iptv">● [IPTV] Canales</span>
                <span class="dot-sys">● [SYSTEM] Sistema</span>
                <span class="dot-dev">● [DEV] Ajustes</span>
            </div>
        </div>
        <div id="terminal" class="terminal-box"></div>
    </div>

    <script>
        const terminal = document.getElementById('terminal');
        const toast = document.getElementById('toast');
        const eventCount = document.getElementById('eventCount');
        let autoScroll = true;

        terminal.addEventListener('scroll', function() {
            const dist = terminal.scrollHeight - terminal.scrollTop - terminal.clientHeight;
            autoScroll = dist < 50;
        });

        function showToast(msg) {
            toast.innerText = msg;
            toast.style.display = 'block';
            setTimeout(function() { toast.style.display = 'none'; }, 4000);
        }

        ${if (successMsg != null) "showToast('$successMsg');" else ""}

        function escapeHtml(text) {
            return text
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;");
        }

        function formatLine(raw) {
            const safe = escapeHtml(raw);
            let color = '#A6C5E2';
            let extra = '';
            if (safe.indexOf('[KEY]') !== -1) {
                color = '#00FF88';
                extra = 'font-weight: 600;';
            } else if (safe.indexOf('[IPTV]') !== -1) {
                color = '#FF007F';
                extra = 'font-weight: 600;';
            } else if (safe.indexOf('[SYSTEM]') !== -1) {
                color = '#00F0FF';
            } else if (safe.indexOf('[DEV]') !== -1) {
                color = '#FFB800';
            } else if (safe.indexOf('[ERROR]') !== -1) {
                color = '#FF3366';
                extra = 'font-weight: bold;';
            }
            return '<div class="log-line" style="color: ' + color + '; ' + extra + '">' + safe + '</div>';
        }

        let lastText = '';
        async function updateLogs() {
            try {
                const res = await fetch('/raw_logs');
                if (!res.ok) return;
                const text = await res.text();
                if (text === lastText) return;
                lastText = text;

                const trimmed = text.trim();
                const lines = trimmed ? trimmed.split('\n') : [];
                eventCount.innerText = lines.length;

                let html = '';
                for (let i = 0; i < lines.length; i++) {
                    html += formatLine(lines[i]);
                }
                terminal.innerHTML = html;

                if (autoScroll) {
                    terminal.scrollTop = terminal.scrollHeight;
                }
            } catch(e) {}
        }

        async function clearLogs() {
            try {
                await fetch('/clear');
                lastText = '';
                terminal.innerHTML = '';
                eventCount.innerText = '0';
                showToast('Búfer de logs vaciado');
            } catch(e) {}
        }

        document.getElementById('m3uForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            const input = document.getElementById('m3uInput');
            const url = input.value.trim();
            if (!url) return;
            const btn = document.getElementById('submitBtn');
            btn.disabled = true;
            btn.innerText = 'ENVIANDO...';
            try {
                await fetch('/?set_m3u=' + encodeURIComponent(url));
                showToast('¡Lista enviada correctamente a tu TV!');
                input.value = '';
                updateLogs();
            } catch(err) {
                showToast('Error al enviar la lista');
            } finally {
                btn.disabled = false;
                btn.innerText = 'ENVIAR A LA TV';
            }
        });

        updateLogs();
        setInterval(updateLogs, 800);
    </script>
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
            val line = reader.readLine() ?: return

            if (line.startsWith("GET")) {
                val fullPath = line.split(" ")[1]
                val path = fullPath.substringBefore("?")
                var successMessage: String? = null

                if (fullPath.contains("set_m3u=")) {
                    val rawUrl = fullPath.substringAfter("set_m3u=").substringBefore("&")
                    val decodedUrl = java.net.URLDecoder.decode(rawUrl, "UTF-8")
                    if (decodedUrl.isNotBlank()) {
                        onM3uUrlReceived?.invoke(decodedUrl)
                        log("IPTV", "Lista M3U recibida desde la web: $decodedUrl")
                        successMessage = "¡Lista enviada correctamente a tu TV!"
                    }
                }

                when {
                    path == "/raw_logs" -> {
                        val rawText = getRawLogs()
                        val bytes = rawText.toByteArray(Charsets.UTF_8)
                        val writer = PrintWriter(socket.getOutputStream(), true)
                        writer.println("HTTP/1.1 200 OK")
                        writer.println("Content-Type: text/plain; charset=utf-8")
                        writer.println("Access-Control-Allow-Origin: *")
                        writer.println("Content-Length: ${bytes.size}")
                        writer.println("Connection: close")
                        writer.println()
                        writer.flush()
                        socket.getOutputStream().write(bytes)
                    }
                    path == "/clear" -> {
                        clearLogs()
                        val bytes = "OK".toByteArray(Charsets.UTF_8)
                        val writer = PrintWriter(socket.getOutputStream(), true)
                        writer.println("HTTP/1.1 200 OK")
                        writer.println("Content-Type: text/plain; charset=utf-8")
                        writer.println("Access-Control-Allow-Origin: *")
                        writer.println("Content-Length: ${bytes.size}")
                        writer.println("Connection: close")
                        writer.println()
                        writer.flush()
                        socket.getOutputStream().write(bytes)
                    }
                    else -> {
                        val html = getUnifiedWebHtml(successMessage)
                        val bytes = html.toByteArray(Charsets.UTF_8)
                        val writer = PrintWriter(socket.getOutputStream(), true)
                        writer.println("HTTP/1.1 200 OK")
                        writer.println("Content-Type: text/html; charset=utf-8")
                        writer.println("Access-Control-Allow-Origin: *")
                        writer.println("Content-Length: ${bytes.size}")
                        writer.println("Connection: close")
                        writer.println()
                        writer.flush()
                        socket.getOutputStream().write(bytes)
                    }
                }
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