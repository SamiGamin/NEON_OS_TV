# 🗺️ Hoja de Ruta — SamiBox TV (NeonOS TV)

Este documento detalla el estado del desarrollo, las fases consolidadas y los hitos futuros planificados para **SamiBox TV**, el launcher cyberpunk de alto rendimiento para Android TV y TV Boxes.

---

## 🎯 Visión del Proyecto
Construir el launcher y centro de medios más rápido, fluido y visualmente impactante para Android TV, optimizado para ejecutarse en hardware de recursos contenidos (**1 GB a 2 GB de RAM**) con tasa de refresco constante de 60 FPS, cero desenfoques costosos, navegación 100% amigable con control remoto (D-Pad) y un ecosistema Cyberpunk Neon inmersivo.

---

## 📌 Estado de las Fases

| Fase | Título | Estado | Versión |
| :--- | :--- | :---: | :---: |
| **Fase 1** | Cimientos, Arquitectura TV y Navegación D-Pad | ✅ Completada | v1.0.0 |
| **Fase 2** | Media Hub, Telemetría Global y Gestión de Apps | ✅ Completada | v1.0.10 |
| **Fase 3** | Inmersión Cyberpunk, Backdrop Ambiental y Consola Web | ✅ Completada | **v1.1.0 (Actual)** |
| **Fase 4** | Guía EPG XMLTV, Picture-in-Picture (PIP) y Canales | 🔄 Planificada / Próxima | v1.2.0 |
| **Fase 5** | Temas Neón Personalizables, Perfiles y Cloud Sync | ⏳ Futura | v2.0.0 |

---

## 🚀 Detalle de Fases y Funcionalidades

### ✅ Fase 1: Arquitectura Base y Experiencia TV (Completada — v1.0.0)
- [x] Migración integral a **Jetpack Compose for TV (`androidx.tv.material3`)**.
- [x] Arquitectura limpia **Clean Architecture + MVVM** con capas estrictamente desacopladas.
- [x] Filas dinámicas horizontales organizadas por categorías (*STREAMING*, *GAMING*, *APPS*).
- [x] Reordenamiento fantasma interactivo en vivo con `Modifier.animateItem()` y soporte D-Pad.
- [x] Menú contextual lateral deslizable (`AppContextSideDrawer`) por pulsación larga.
- [x] Detección de arranque en inicio del sistema (`BOOT_COMPLETED`) y selector de launcher predeterminado.
- [x] Sistema de actualización OTA en segundo plano conectado a la API de GitHub Releases.

---

### ✅ Fase 2: Centro Multimedia y Monitor Global (Completada — v1.0.10)
- [x] **Cyber Media Hub:** Exploración de almacenamiento interno y discos/memorias USB externos.
- [x] Reproductor multimedia integrado con **Jetpack Media3 (ExoPlayer)** y soporte para formatos `.mp4`, `.mkv`, `.avi`, `.ts`, `.mp3`, `.flac`.
- [x] Controles OSD de reproducción adaptados a control remoto con auto-ocultamiento a los 4 segundos.
- [x] **Monitor de Sistema Global (HUD Flotante):**
  - [x] Activación global sobre cualquier app mediante tecla `MENU` (Keycode 82) vía servicio de accesibilidad.
  - [x] Métricas en tiempo real: FPS reales (`Choreographer`), CPU diferencial, RAM, temperatura y red.
  - [x] Detección y prevención de fugas de memoria con desregistro automático de observers.

---

### ✅ Fase 3: Inmersión Cyberpunk, Backdrop y Consola Web (Completada — v1.1.0)
- [x] **Fondo Ambiental Dinámico (Ambient Backdrop):**
  - [x] Proyección en pantalla completa de la carátula de la app o nodo activo al posicionar el foco D-Pad.
  - [x] Debounce inteligente de 180 ms con `LaunchedEffect` para garantizar 60 FPS en TV Boxes de 1-2 GB de RAM.
  - [x] Transición suave con `Crossfade (350 ms)` y máscara de degradado vertical profundo (`#040711`).
  - [x] Proyección de título central holográfico en tipografía `Share Tech Mono` con espaciado amplio.
- [x] **Tarjetas Virtuales Cyberpunk:**
  - [x] Soporte para nodo "LIVE TV" (IPTV) con carátula panorámica, fallback procedimental neón y badge `● LIVE`.
  - [x] Soporte para nodo "MEDIA HUB" con carátula panorámica y vector neón.
  - [x] Tarjeta "+ GESTIONAR APPS" (`baner_app.jpg`) integrada al backdrop ambiental y soporte para ocultar etiquetas en modo minimalista.
- [x] **5 Modos de Diseño de Pantalla (`TvLayoutMode`):**
  - [x] Modos: Compact High Density, Panoramic 16:9, Compact Standard, Dual Priority y Modern Grid.
  - [x] Snapping magnético (`Zero Partial Cards`) mediante `rememberSnapFlingBehavior`.
- [x] **Servidor Web Embebido & Ingesta Remota (`DevLogManager` en Puerto 8080):**
  - [x] Servidor HTTP ligero con consola split-view cyberpunk.
  - [x] Ingesta remota de listas M3U/M3U8 directamente desde el navegador de PC o móvil.
  - [x] Stream de logs y telemetría en tiempo real (`/raw_logs`).
  - [x] Modal QR (`CyberQrCard`) con emparejamiento rápido vía D-Pad.
- [x] **Telemetría de Red Neón (`CyberNetworkBadge` & `NetworkMonitor`):**
  - [x] Monitoreo reactivo de Wi-Fi (con nivel RSSI), Ethernet cableada y estado desconectado.
- [x] **Mapeo Físico de Teclas de Control Remoto (`TvRemoteKeyCodes`):**
  - [x] Mapeo de teclas de colores (Azul, Amarillo, Rojo, Verde), Guía TV y Live TV.

---

### 🔄 Fase 4: Guía EPG XMLTV, PIP y Canales (Próxima — v1.2.0)
- [ ] **Motor EPG Nativo (XMLTV Parser):**
  - [ ] Descarga y parseo en segundo plano (`Dispatchers.IO`) de guías de programación XMLTV / GZ.
  - [ ] Base de datos ultraligera Room para caché local de parrilla televisiva.
  - [ ] Visualización de programa actual y siguiente con barra de progreso temporal.
- [ ] **Modo Picture-in-Picture (PIP):**
  - [ ] Continuidad de reproducción de TV o medios en ventana flotante al navegar por el Home launcher.
- [ ] **Organización Avanzada de Canales IPTV:**
  - [ ] Filtrado por grupos y países.
  - [ ] Marcado de canales favoritos con acceso rápido desde el Home.
- [ ] **Exportación / Importación de Ajustes:**
  - [ ] Copia de seguridad local de categorías, orden de apps y configuración general en archivo JSON.

---

### ⏳ Fase 5: Personalización Avanzada y Cloud Sync (Futuro — v2.0.0)
- [ ] **Selector de Paletas Cyberpunk:**
  - [ ] Temas alternativos: *Neon Amber*, *Matrix Emerald*, *Vaporwave Violet*, *Acid Cyberpunk*.
- [ ] **Perfiles de Usuario:**
  - [ ] Perfil infantil / Restricción por PIN para apps específicas o categorías.
- [ ] **Sincronización en la Nube:**
  - [ ] Sincronización opcional de listas y configuraciones mediante WebDAV o servidor auto-alojado.
- [ ] **Mapeo Automático de Mandos Gamepad:**
  - [ ] Detección automática de controles Bluetooth (Xbox, PlayStation, genéricos) para navegación rápida.

---

## ⚙️ Reglas de Rendimiento para Contribuidores
Cualquier nueva característica debe respetar los principios estrictos de optimización:
1. **Sin Desenfoques en Tiempo Real:** Prohibido el uso de shaders `Modifier.blur()` que degraden el fillrate de GPUs Mali/PowerVR.
2. **Consultas Asíncronas:** Todo acceso a `PackageManager`, almacenamiento o red debe ejecutarse bajo `Dispatchers.IO`.
3. **Imágenes en Caché:** Todo recurso gráfico debe cargarse a través de Coil respetando las dimensiones exactas de visualización.
4. **Pruebas Locales:** La compilación debe validarse siempre mediante `./gradlew assembleDebug` previo a cualquier commit.
