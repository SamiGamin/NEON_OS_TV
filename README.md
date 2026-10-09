# 👾 NEONOS TV Launcher (SamiBox TV)

**SamiBox TV** es un launcher de alto rendimiento y código abierto diseñado exclusivamente para **Android TV**, Google TV y TV Boxes genéricas (SoCs Amlogic, Allwinner, Rockchip). Desarrollado al 100% con **Jetpack Compose for TV (`androidx.tv.material3`)** y **Clean Architecture**, combina una estética **Cyberpunk Neon** futurista con herramientas avanzadas de diagnóstico de hardware en tiempo real, fondo ambiental dinámico, reproducción multimedia integrada y gestión remota local.

El proyecto está optimizado minuciosamente para funcionar con fluidez absoluta (60 FPS estables) en dispositivos de bajos recursos (**1 GB a 2 GB de RAM**), priorizando overdraw cero, ausencia de shaders de desenfoque pesados en tiempo real, recolección asíncrona de recursos en segundo plano (`Dispatchers.IO`) y una navegación 100% nativa mediante **D-Pad / Control Remoto**.

---

## ✨ Características Principales

### 1. 🌌 Fondo Ambiental Dinámico (Ambient Backdrop) & Título Holográfico
* **Backdrop Reactivo al Foco:** Al navegar sobre cualquier tarjeta (aplicación instalada o nodo virtual), el launcher proyecta suavemente su carátula o banner panorámico como fondo de pantalla completa.
* **Optimización Extrema de Memoria:** Incorpora un **debounce de 180 ms** y transiciones suaves con **Crossfade (350 ms)** para evitar saturación del Garbage Collector y tirones durante desplazamientos rápidos con el D-Pad.
* **Máscara de Alto Contraste:** Degradado vertical profundo con `CyberDarkBg` (`#040711`) que asegura legibilidad perfecta de textos y carruseles.
* **Proyección Central Holográfica:** En la parte superior de la pantalla se proyecta en tiempo real el nombre de la app activa con tipografía terminal `Share Tech Mono`, estilo cursiva y espaciado amplio neón, manteniendo la identidad del sistema cuando no hay selección.

---

### 2. 📺 Tarjetas Virtuales Cyberpunk & Reproductor IPTV Nativo
* **Nodo "LIVE TV" (IPTV):**
  * Detección automática y renderizado de carátula panorámica (`banner_live_tv.jpg` / `icono_live_tv.jpg`).
  * Fallback procedimental con rejilla holográfica, ondas de señal de transmisión radiante en `CyberCyan` (`#00F0FF`) y badge luminoso pulsante `● LIVE`.
  * Acceso directo al reproductor IPTV integrado compatible con transmisiones HLS (`.m3u8`) y TS.
* **Nodo "MEDIA HUB":**
  * Acceso directo al centro multimedia con banner panorámico (`banner_media.jpg` / `icono_media.jpg`).
  * Detección y reproducción nativa de almacenamiento interno y memorias USB externas con Jetpack Media3 (ExoPlayer).
* **Tarjeta "+ GESTIONAR APPS":**
  * Banner panorámico (`baner_app.jpg`) integrado al sistema de fondo ambiental.
  * Respeta fielmente la preferencia del usuario sobre mostrar u ocultar nombres en modo minimalista.

---

### 3. 🌐 Servidor Web Embebido & Sincronización Rápida por QR
* **Consola Web HTTP (Puerto 8080):** Servidor HTTP ultraligero integrado (`DevLogManager`) que se ejecuta directamente en la TV Box.
* **Interfaz Web Split-View Cyberpunk:**
  * **Ingesta Remota de M3U:** Carga y actualización de listas de canales IPTV pegando la URL directamente desde el navegador de un móvil o PC.
  * **Consola de Telemetría en Vivo:** Flujo continuo de logs del sistema (`/raw_logs`) con sintaxis resaltada y monitoreo en tiempo real.
* **Modal de Emparejamiento QR (`CyberQrCard`):** Generación instantánea de código QR en pantalla con manejo estable de foco D-Pad para conectar dispositivos en la misma red local en segundos.

---

### 4. 🎛️ 5 Modos de Densidad de Pantalla & Snapping Magnético
* **Distribuciones Adaptativas (`TvLayoutMode`):**
  1. `COMPACT_HIGH_DENSITY`: Filas compactas de alta densidad para visualizar múltiples apps simultáneamente.
  2. `PANORAMIC_16_9`: Tarjetas panorámicas de formato amplio estilo cine y streaming.
  3. `COMPACT_STANDARD`: Equilibrio visual estándar para pantallas de sala de estar.
  4. `DUAL_PRIORITY`: Distribución jerárquica con fila principal destacada.
  5. `MODERN_GRID`: Cuadrícula moderna cyberpunk de acceso rápido.
* **Zero Partial Cards:** Desplazamiento magnético mediante `rememberSnapFlingBehavior` que evita tarjetas cortadas o asimétricas en los bordes de la pantalla.
* **Modo Minimalista:** Opción de alternar entre visualización con títulos de texto o modo solo iconos limpios de 54dp.

---

### 5. 👻 Reordenamiento Fantasma en Vivo (Ghost Drag & Drop)
* Al activar **"Reordenar"** desde el menú contextual:
  * Elevación suave (`scale 1.12f`), resplandor pulsante neón cyan/magenta y etiqueta flotante `◄ MOVER ►`.
  * **Intercambio en tiempo real:** Con `D-PAD Izquierda` o `D-PAD Derecha`, la app intercambia su posición con las vecinas y las tarjetas se reubican con animación física instantánea (`Modifier.animateItem()`).
  * Persistencia automática e inmediata del nuevo orden en disco.

---

### 6. 📡 Telemetría de Red en Tiempo Real & Monitor Global
* **Badge de Red Neón (`CyberNetworkBadge`):** Monitoreo continuo del estado de conexión (Wi-Fi con nivel de señal RSSI, Ethernet cableada y estado desconectado) en el Header HUD.
* **Monitor de Sistema Global (Overlay HUD):**
  * Desplegable sobre cualquier aplicación mediante la tecla `MENU` (Keycode 82) a través de `SamiBoxAccessibilityService`.
  * Métricas en tiempo real: FPS reales (`Choreographer`), uso diferencial de CPU (`/proc/stat`), RAM consumida/libre, temperatura de SoC, ancho de banda de red y uptime.
  * Cero fugas de memoria: La telemetría se suspende de inmediato cuando el panel está oculto.

---

### 7. 🎮 Mapeo de Teclas Físicas de Control Remoto (`TvRemoteKeyCodes`)
* Soporte para botones dedicados de mandos a distancia de TV Box:
  * **Botones de color:** Rojo, Verde, Amarillo y Azul.
  * **Teclas directas:** Guía TV (`KEYCODE_GUIDE`), Live TV (`KEYCODE_TV`), EPG, Menu (`KEYCODE_MENU`).

---

### 8. 🔄 Actualizaciones OTA Nativas
* Consulta directa con la API de **GitHub Releases**.
* Descarga en segundo plano y disparo automático del instalador nativo de paquetes (`PackageInstaller`).

---

## 🏗 Arquitectura y Stack Tecnológico

SamiBox TV sigue rigurosamente los principios de **Clean Architecture** y el patrón **MVVM**:

```text
com.launcher.samiboxtv/
├── data/          # Repositorios, red (NetworkMonitor), SharedPreferences y Room/DataStore
│   ├── datasource/
│   ├── network/
│   └── repository/
├── domain/        # Modelos puros, Use Cases e interfaces libres de dependencias de Android
│   ├── model/
│   ├── repository/
│   └── usecase/
├── presentation/  # UI Jetpack Compose for TV, ViewModels, Theme y Overlays HUD
│   ├── home/
│   ├── media/
│   ├── settings/
│   ├── components/
│   │   ├── cards/
│   │   └── hud/
│   ├── overlay/
│   └── theme/
├── util/          # Mapeo de control remoto, servidor HTTP DevLog, accesibilidad y extensiones
└── di/            # Contenedor de inyección manual (AppContainer & ViewModelFactory)
```

* **Lenguaje:** Kotlin 100%
* **UI Toolkit:** Jetpack Compose for TV (`androidx.tv.material3`)
* **Multimedia:** Jetpack Media3 (`androidx.media3:media3-exoplayer`, `media3-ui`)
* **Asincronía & Estado:** Kotlin Coroutines + StateFlow (`@Immutable` UI States)
* **Carga de Imágenes:** Coil Compose con caché y decodificación ajustada a pantalla
* **Inyección de Dependencias:** Inyección manual mediante `AppContainer` y Service Locator
* **Persistencia:** `SharedPreferences` optimizado para lectura y escritura atómica

---

## 🎨 Identidad Visual Cyberpunk

* **Fondo Principal:** `#040711` / `#070A13` (Carbono espacial profundo)
* **Cyan Neón:** `#00F0FF` / `#00F5FF`
* **Magenta / Pink Neón:** `#FF0055` / `#FF007F`
* **Ámbar / Oro Neón:** `#FFB800` / `#FFB300`
* **Verde Eléctrico:** `#00E676`
* **Tipografías:** *Share Tech Mono* (estilo terminal/HUD) y *Outfit*

---

## 🛠 Requisitos y Compilación

* **Android Studio:** Ladybug / Meerkat o superior.
* **JDK:** Java 17 o 21.
* **SDK Mínimo:** Android API 26 (Android 8.0 Oreo).
* **SDK Objetivo:** Android API 34+ (Android 14+).

### Compilación limpia desde la terminal:
```bash
./gradlew assembleDebug
```
El APK se genera en:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🗺️ Hoja de Ruta (Roadmap)

Consulta nuestro archivo [ROADMAP.md](ROADMAP.md) para conocer las fases completadas, funcionalidades en desarrollo y los próximos hitos del proyecto (EPG XMLTV, Picture-in-Picture, perfiles de usuario y sincronización en la nube).

---

## 📄 Licencia

Este proyecto está distribuido bajo la licencia **MIT**. Consulta el archivo `LICENSE` para mayores detalles.
