# 🚀 SamiBox TV — Hoja de Ruta (Roadmap) y Visión Futura

Este documento define la evolución técnica y funcional de **SamiBox TV Launcher**, consolidando los hitos ya implementados y trazando las nuevas características prioritarias para futuras versiones.

---

## ✅ 1. Logros Implementados & Mejoras Consolidadas

Las siguientes funciones fueron completadas e integradas con éxito en el launcher:

* **[✓] Modo Reordenamiento Fantasma en Vivo (Estilo Projectivy):**
  * Activación desde el panel contextual lateral.
  * Tarjeta interactiva con escala, resplandor pulsante neón (`◄ MOVER ►`) y animación física fluida (`Modifier.animateItem()`).
  * Intercambio en tiempo real con D-Pad Izquierda/Derecha y persistencia local inmediata.
* **[✓] Filas y Categorías Temáticas Dinámicas:**
  * Organización horizontal por secciones (*STREAMING*, *GAMING*, *APPS* y categorías personalizadas).
  * Panel de gestión de categorías en Ajustes para crear y eliminar filas libremente.
* **[✓] Menú Lateral Contextual (`AppContextSideDrawer`):**
  * Panel deslizable a la derecha (400dp) con autofoco D-Pad.
  * Desinstalación nativa de aplicaciones (`Intent.ACTION_DELETE`), acceso a ajustes del sistema, favoritos y categorización.
* **[✓] Personalización Visual y Modo Minimalista:**
  * Selector de proporciones de tarjeta (16:9, Cuadrado 1:1, Compacto).
  * Conmutador para **Mostrar/Ocultar Nombres de Apps** (modo minimalista de solo iconos a 54dp con arte limpio).
* **[✓] Cyber Media Hub (Explorador & Reproductor de Medios Nativo):**
  * Detección automática de memorias internas y discos USB externos (`/storage/XXXX-XXXX`) con cálculo de espacio.
  * Escaneo reactivo de videos (.mp4, .mkv, .avi, .ts) y música (.mp3, .flac, .wav, .m4a) en `Dispatchers.IO`.
  * Reproductor nativo en pantalla completa con **Jetpack Media3 (ExoPlayer)** y HUD OSD adaptado a control remoto.
* **[✓] Monitor de Telemetría Global (System Info Overlay):**
  * Activación/cierre global con la tecla `MENU` (Keycode 82) vía `AccessibilityService`.
  * Medición de FPS en tiempo real con Choreographer, CPU diferencial (`/proc/stat`), uso de RAM, temperatura, velocidad de red y almacenamiento.
  * Suspensión total de telemetría cuando el HUD está inactivo para prevenir consumo de recursos.
* **[✓] Actualizaciones OTA Nativas:**
  * Consulta directa con GitHub Releases, descarga en segundo plano y ejecución de `PackageInstaller`.
* **[✓] Selector de Launcher Predeterminado:**
  * Detección reforzada y apertura de selector de Home nativo para Android 8 hasta Android 14.

---

## 🔮 2. Nuevas Metas y Próximas Características

### 🎨 A. Personalización Estética & Fondos Cyberpunk
- **Selector de Fondos Personalizados:**
  - Permitir al usuario elegir una imagen estática almacenada en el dispositivo o en una unidad USB (integrado con el Media Hub) como fondo de pantalla del Launcher.
  - Opciones de gradientes o rejillas matriciales cyber con costo computacional cero (sin renders pesados ni overdraw).
- **Control de Brillo y Atenuación de Fondo:**
  - Control deslizante para oscurecer el fondo de pantalla y garantizar máximo contraste con las tarjetas de apps.

### 🔊 B. Efectos de Sonido Cyberpunk (SFX)
- **Feedback Auditivo para D-Pad:**
  - Integrar efectos de sonido sutiles y futuristas (beeps de baja frecuencia, clics digitales de alta fidelidad) al desplazarse entre tarjetas y categorías.
  - Sonido de confirmación al ejecutar aplicaciones o confirmar reordenamiento.
  - Opción en Ajustes para habilitar/deshabilitar sonidos y ajustar volumen independientemente del sistema.

### 🛡 C. Protector de Pantalla / Screensaver Cyberpunk (Daydream)
- **Modo de Reposo HUD:**
  - Activación tras 5 o 10 minutos de inactividad en la pantalla principal.
  - Reloj digital gigante estilo holográfico neón, fecha y métricas básicas de hardware.
  - Algoritmo anti-quemado (*pixel shift*) para televisores OLED y paneles LED: traslación lenta de elementos para evitar retención de imagen.

### 🔍 D. Búsqueda Global Rápida (Global Search)
- **Buscador de Apps y Medios:**
  - Botón de búsqueda accesible en la cabecera superior.
  - Teclado virtual optimizado para control remoto con sugerencias en tiempo real.
  - Capacidad de filtrar aplicaciones instaladas y archivos multimedia de discos USB simultáneamente.

### 🎮 E. Mapeo Avanzado de Teclas del Control Remoto
- **Asignación de Teclas Multimedia y Especiales:**
  - Aprovechar teclas adicionales como `KEYCODE_CHANNEL_UP` (166) y `KEYCODE_CHANNEL_DOWN` (167) para saltar rápidamente entre categorías de la pantalla principal.
  - Atajos numéricos (teclas 0 a 9) para lanzar aplicaciones favoritas asignadas con una sola pulsación.
  - Atajos de botones de color (Rojo, Verde, Amarillo, Azul) para funciones directas: abrir Media Hub, abrir Ajustes o alternar Overlay.

### 🔒 F. Perfiles y Modo Kiosko / Control Parental
- **Protección con PIN:**
  - Bloquear el acceso a la sección de Ajustes y a la desinstalación de aplicaciones mediante un código PIN numérico de 4 dígitos.
  - Opción de ocultar aplicaciones específicas para niños o invitados.

### 🌦 G. Widgets de Cabecera (Header HUD)
- **Widget de Clima Local:**
  - Consulta ligera de clima y temperatura de la ciudad actual en la barra superior.
- **Feeds RSS de Noticias:**
  - Marquesina sutil opcional con titulares de tecnología o videojuegos configurables.

---

## ⚡ 3. Directrices de Rendimiento Obligatorias para Nuevas Funciones
1. **Dispositivos Objetivo de 1GB a 2GB de RAM:** Ninguna nueva función debe elevar el consumo base de RAM por encima de 90 MB.
2. **Cero Overdraw:** Prohibido el uso de capas de desenfoque (`blur`) en tiempo real o sombras complejas con múltiples pasadas de GPU.
3. **Todo el I/O fuera de Main:** Lecturas de almacenamiento USB, llamadas de red de widgets o accesos a bases de datos deben ejecutarse estrictamente bajo `Dispatchers.IO`.
4. **Navegación 100% D-Pad:** Cada pantalla o diálogo nuevo debe contar con orden de foco determinista y accesible con un control remoto estándar de 5 botones (Arriba, Abajo, Izquierda, Derecha, OK).