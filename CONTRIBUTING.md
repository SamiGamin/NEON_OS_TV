# Guía de Contribución para SamiBox TV

¡Gracias por mostrar interés en colaborar con **SamiBox TV**! El proyecto es de código abierto y agradece las aportaciones de desarrolladores Android, diseñadores y entusiastas de Android TV.

Para mantener una base de código limpia, escalable y con rendimiento óptimo en dispositivos Android TV de bajos recursos (1GB - 2GB RAM), sigue estas pautas obligatorias:

---

## 1. 👾 Filosofía de Diseño & UI
SamiBox TV sigue una estética **Cyberpunk Neon** de alto contraste:
* **Paleta de Colores Oficial:**
  * Fondo: `#070A13` (Carbono espacial oscuro).
  * Cyan Neón: `#00F5FF` (Bordes de foco, acentos primarios).
  * Magenta Neón: `#FF0055` (Acciones críticas, modo fantasma, advertencias).
  * Ámbar Neón: `#FFB300` (Advertencias, favoritos, destacados).
* **Tipografías:** Usar estrictamente `ShareTechMonoFontFamily` (para datos, terminales y títulos) y `OutfitFontFamily` (para textos legibles y subtítulos).
* **UI Toolkit:** Obligatorio utilizar `androidx.tv.material3` (`TvMaterial3`). No mezclar con componentes de Material 3 para teléfonos móviles salvo utilidades core indispensables.
* **Navegación 100% D-Pad:** Todo componente interactivo debe tener un estado de foco visible, borde neón nítido y ser completamente navegable mediante control remoto físico.

---

## 2. ⚡ Rendimiento Extremo para Android TV (1GB - 2GB RAM)
* **Cero Overdraw:** Prohibido el uso de shaders de desenfoque en tiempo real (`blur`) o capas transparentes innecesarias.
* **Todo I/O fuera del hilo principal:** Consultas a `PackageManager`, operaciones de almacenamiento USB, lectura de archivos y base de datos deben ejecutarse estrictamente bajo `Dispatchers.IO`.
* **Caché y Decodificación Eficiente:** Usar `Coil` (`rememberAsyncImagePainter`) para carga asíncrona de iconos e imágenes sin saturar la memoria heap de la TV Box.
* **Aislamiento de Recomposiciones:** Los cambios de foco, cronómetros y telemetría no deben provocar recomposiciones de toda la pantalla o de listas completas.

---

## 3. 🏗 Arquitectura del Proyecto
* **Clean Architecture + MVVM:**
  * `domain/`: Reglas de negocio puras, modelos libres de dependencias de Android y Casos de Uso (`UseCase`).
  * `data/`: Implementación de repositorios, almacenamiento local (`SharedPreferences`, persistencia) y escaneo de hardware/medios.
  * `presentation/`: Pantallas Compose for TV, ViewModels y UI States inmutables (`@Immutable`).
  * `di/`: Inyección de dependencias manual mediante `AppContainer` y `ViewModelFactory`.
  * `util/`: Clases de soporte, instalador nativo, helpers de accesibilidad y extensiones.

---

## 4. 👨‍💻 Flujo de Trabajo para Pull Requests (PR)
1. Haz un **Fork** de este repositorio en tu cuenta de GitHub.
2. Crea una rama descriptiva para tu característica:
   ```bash
   git checkout -b feature/cyber-screensaver
   ```
3. Realiza tus cambios asegurándote de no romper la compilación limpia:
   ```bash
   ./gradlew assembleDebug
   ```
4. Haz tus commits con mensajes claros y concisos siguiendo Conventional Commits (`feat:`, `fix:`, `refactor:`, `docs:`).
5. Abre un **Pull Request (PR)** hacia la rama `master` de este repositorio.

¡Gracias por ayudar a construir el launcher para Android TV más rápido, futurista y fluido de la comunidad!
