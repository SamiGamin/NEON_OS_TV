# 👾 NEONOS TV Launcher

**SamiBox TV** es un launcher de alto rendimiento y código abierto diseñado exclusivamente para **Android TV**, Google TV y TV Boxes genéricas (SoCs Amlogic, Allwinner, Rockchip). Desarrollado al 100% con **Jetpack Compose for TV (`androidx.tv.material3`)** y **Clean Architecture**, combina una estética **Cyberpunk Neon** futurista con herramientas avanzadas de diagnóstico de hardware en tiempo real y reproducción multimedia integrada.

El proyecto está optimizado para funcionar con extrema fluidez en dispositivos de bajos recursos (**1 GB a 2 GB de RAM**), priorizando overdraw cero, ausencia de desenfoques pesados en tiempo real, recolección asíncrona de recursos en segundo plano y una navegación 100% nativa mediante **D-Pad / Control Remoto**.

---

## ✨ Características Principales

### 1. 🚀 Pantalla Principal & Filas Dinámicas por Categorías
* **Diseño adaptado para TV:** Filas horizontales (`LazyRow`) organizadas por categorías temáticas (*STREAMING*, *GAMING*, *APPS* y categorías personalizadas).
* **Gestión de categorías desde Ajustes:** Posibilidad de crear nuevas filas personalizadas, asignarles nombres temáticos y organizar las aplicaciones instaladas.
* **Formato de tarjetas personalizable:** Soporte para banners panorámicos `16:9`, modo compacto o tarjetas cuadradas `1:1`.
* **Modo Minimalista (Mostrar/Ocultar Nombres):** Opción en ajustes para ocultar los nombres de las aplicaciones y lucir tarjetas limpias de solo iconos (`54dp`), o mantener los nombres con efecto marquesina animado al enfocar.

### 2. 👻 Reordenamiento Fantasma en Vivo (Estilo Projectivy Launcher)
* Al activar **"Reordenar"** desde el menú contextual, la tarjeta entra en un **modo interactivo fantasma**:
  * Elevación suave (`scale 1.12f`), resplandor pulsante neón cyan/magenta y etiqueta flotante `◄ MOVER ►`.
  * **Intercambio en tiempo real:** Presionando `D-PAD Izquierda` o `D-PAD Derecha`, la app intercambia su posición con las vecinas y las tarjetas se desplazan suavemente con animación física (`Modifier.animateItem()`).
  * Al pulsar `OK / ENTER` o `BACK`, la app se fija en su nueva posición y el orden se almacena de forma persistente.

### 3. 📑 Menú Lateral Contextual (`AppContextSideDrawer`)
* Acceso instantáneo manteniendo pulsado `OK / ENTER` sobre cualquier aplicación de la pantalla principal.
* Panel deslizable a la derecha (400dp) con autofoco inmediato de D-Pad.
* Acciones disponibles:
  * **Abrir aplicación.**
  * **Reordenar aplicación** (modo fantasma interactivo).
  * **Marcar / Desmarcar como Favorito.**
  * **Cambiar de Categoría.**
  * **Ajustes de Android** (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`).
  * **Desinstalar aplicación** de forma nativa (`Intent.ACTION_DELETE`).

### 4. 🎬 Cyber Media Hub (Explorador & Reproductor de Medios Nativo)
* **Acceso directo en el Home:** Tarjeta temática neón que abre el centro multimedia integrado.
* **Detección de almacenamiento:** Identificación automática de la memoria interna y unidades USB externas (`/storage/XXXX-XXXX`) con cálculo de espacio libre y total.
* **Exploración reactiva:** Escaneo en segundo plano (`Dispatchers.IO`) de archivos de video (`.mp4`, `.mkv`, `.avi`, `.ts`) y música (`.mp3`, `.flac`, `.wav`, `.m4a`).
* **Reproductor integrado con Jetpack Media3 (ExoPlayer):**
  * Reproducción nativa en pantalla completa sin depender de apps de terceros.
  * OSD Cyberpunk HUD adaptado al control remoto: Play/Pausa, avance y retroceso de 10s con D-Pad, barra de progreso interactiva e información del medio.
  * Ocultación automática del HUD tras 4 segundos de inactividad o mediante tecla `BACK`.

### 5. ⚡ Monitor de Sistema Global (System Info Overlay HUD)
* Panel de telemetría flotante estilo "Developer Mode" visible sobre cualquier app (Netflix, YouTube, juegos, etc.).
* Métricas en tiempo real:
  * **FPS reales** mediante `Choreographer`.
  * **Uso diferencial de CPU** leyendo `/proc/stat`.
  * **Uso de RAM** con barras e indicadores de advertencia por color.
  * **Temperatura térmica del procesador**.
  * **Velocidad de red** en tiempo real (subida y bajada).
  * **Uptime y espacio de almacenamiento**.
* **Activación por control remoto:** Se despliega y oculta globalmente pulsando la tecla `MENU` (Keycode 82) gracias a `SamiBoxAccessibilityService`.
* **Cero fugas de memoria:** La observación de telemetría se detiene por completo cuando el panel o el log del sistema están cerrados.

### 6. 🔄 Actualizaciones OTA Nativas (Over The Air)
* Verificación directa con la API de **GitHub Releases**.
* Descarga de APK en segundo plano con notificación de progreso.
* Ejecución automática de la instalación mediante el instalador nativo del sistema (`PackageInstaller`).

### 7. ⚙️ Ajustes del Sistema & Configuración
* Selector agresivo para **Establecer como Launcher Predeterminado** compatible con Android 8 hasta Android 14 (abriendo de forma directa las pantallas de selección de Home nativas).
* Resumen de especificaciones del dispositivo (Modelo, Fabricante, Versión de Android, RAM total y núcleos de CPU).

---

## 🏗 Arquitectura y Stack Tecnológico

SamiBox TV sigue los principios de **Clean Architecture** junto con el patrón **MVVM**:

```text
com.launcher.samiboxtv/
├── data/          # Implementación de repositorios, SharedPreferences y Room/DataStore
│   ├── datasource/
│   └── repository/
├── domain/        # Modelos puros, Use Cases e interfaces libres del framework Android
│   ├── model/
│   ├── repository/
│   └── usecase/
├── presentation/  # UI Jetpack Compose for TV, ViewModels, Theme y Overlays HUD
│   ├── home/
│   ├── media/
│   ├── settings/
│   ├── components/
│   ├── overlay/
│   └── theme/
├── util/          # Helpers de sistema, accesibilidad, instalador de APKs y extensiones
└── di/            # Inyección de dependencias manual (AppContainer & ViewModelFactory)
```

* **Lenguaje:** Kotlin 100%
* **UI Toolkit:** Jetpack Compose for TV (`androidx.tv.material3`)
* **Multimedia:** Jetpack Media3 (`androidx.media3:media3-exoplayer`, `media3-ui`)
* **Asincronía & Estado:** Kotlin Coroutines + StateFlow (`@Immutable` UI States)
* **Carga de Imágenes:** Coil Compose con decodificación eficiente y caché
* **Inyección de Dependencias:** Inyección de dependencias manual mediante `AppContainer` y `ViewModelFactory`
* **Persistencia:** `SharedPreferences` optimizado para TV Box

---

## 🎨 Identidad Visual y Prompts Cyberpunk

SamiBox TV utiliza una paleta de colores de alto contraste sobre un fondo oscuro profundo:
* **Fondo Principal:** `#070A13` (Carbono espacial oscuro)
* **Cyan Neón:** `#00F5FF`
* **Magenta Neón:** `#FF0055`
* **Ámbar Neón:** `#FFB300`
* **Tipografías:** *Share Tech Mono* (estilo terminal/HUD) y *Outfit*

### 📱 Prompts Oficiales para Generación de Arte por IA:
* **Icono de la Aplicación:**
  ```text
  Sleek modern cyberpunk TV launcher logo icon, glowing holographic neon cyan and magenta lines, sharp minimalist abstract geometric 'S' symbol inside a dark cockpit interface, high-tech HUD elements, premium app icon, high contrast, clean vector style, game asset style, flat design, photorealistic lighting, 8k resolution
  ```
* **Banner Promocional 16:9:**
  ```text
  Futuristic cyberpunk Android TV launcher showcase banner, neon cyan and hot pink glowing grid lines on a deep black space background, high-tech HUD metrics, virtual terminal screen, glowing digital clock displaying '12:00' in electric orange, sleek minimalist smart TV app cards floating, premium cinematic lighting, volumetric atmosphere, octane render, 8k resolution, wide aspect ratio 16:9, cyberpunk tech theme
  ```

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
El APK resultante se genera en `app/build/outputs/apk/debug/app-debug.apk`.

### Permisos Especiales para TV Box:
1. **Accesibilidad:** Para permitir la intercepción global de la tecla `MENU` (Keycode 82) y el despliegue del HUD de diagnóstico sobre cualquier app, activar **SamiBox TV** en `Ajustes > Accesibilidad`.
2. **Superposición de pantalla:** Conceder el permiso `SYSTEM_ALERT_WINDOW` ("Aparecer encima") para el renderizado del overlay.
3. **Almacenamiento:** Para el Cyber Media Hub, conceder permisos de lectura de almacenamiento local y USB cuando el sistema lo solicite.

---

## 📄 Licencia

Este proyecto está distribuido bajo la licencia **MIT**. Consulta el archivo `LICENSE` para mayores detalles.
