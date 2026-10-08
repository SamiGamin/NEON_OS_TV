# GEMINI.md — Sistema Maestro de Reglas (SamiBox TV Launcher)

## 1. REGLAS DE COMPORTAMIENTO DEL AGENTE (ESTRICTO)
Para garantizar ciclos rápidos, cero bloqueos y eficiencia total en el flujo de trabajo:
- **NO automatizar pruebas por ADB:** Prohibido ejecutar comandos `adb shell input keyevent`, `adb install` o manipular emuladores/dispositivos a menos que el usuario lo pida explícitamente.
- **NO tomar ni analizar capturas de pantalla:** No generar archivos `.png` temporales ni saturar la ventana de contexto inspeccionando pantallas.
- **Modificación y análisis directo de código:** Limitarse estrictamente a inspeccionar, editar, refactorizar o crear archivos fuente (`.kt`, `.xml`, `.gradle.kts`).
- **Verificación local limpia:** Si se requiere validar compilación, ejecutar únicamente `./gradlew assembleDebug` o tareas puntuales de gradle sin levantar demonios gráficos ni dispositivos.
- **Confirmación para cambios importantes / testing:** Ante cualquier cambio crítico de arquitectura o antes de ejecutar suites de pruebas (testing), preguntar de forma previa y explícita al usuario si desea que se lleven a cabo.
- **Enfoque de Skills MobiAI:** Usar las capacidades y skills de MobiAI para Android centrándose exclusivamente en arquitectura Compose y análisis estático de código.

---

## 2. STACK TECNOLÓGICO OFICIAL
- **Plataforma:** Android TV (SDK min 26+, target actual).
- **Lenguaje:** Kotlin (100%).
- **UI:** Jetpack Compose for TV (`androidx.tv.material3`). No mezclar con Material 3 estándar salvo utilidades core indispensables.
- **Asincronía & Flujo:** Coroutines + StateFlow (UI State inmutable con `@Immutable`).
- **Inyección de Dependencias:** Inyección manual / Service Locator / Factory.
- **Almacenamiento Local:** DataStore / Room / SharedPreferences optimizado.
- **Actualizaciones (OTA):** GitHub Releases API con descarga en segundo plano y lanzamiento de `PackageInstaller` nativo.

---

## 3. PRINCIPIOS DE ARQUITECTURA & RENDIMIENTO PARA TV
- **Arquitectura:** Clean Architecture + MVVM (Domain libre de dependencias de Android).
- **Rendimiento para TV Box de bajos recursos (1GB - 2GB RAM):**
  - **Overdraw cero:** Prohibido el uso de shaders de desenfoque en tiempo real (`blur`) o capas transparentes innecesarias.
  - **Uso estricto de Caché:** Iconos decodificados al tamaño exacto de pantalla con Coil/Glide.
  - **Jerarquía plana:** Diseños D-Pad amigables con navegación rápida por teclado físico / control remoto.
  - **Aislamiento de recomposiciones:** El reloj y los cambios de foco no deben provocar recomposiciones masivas del feed principal.
  - **Consultas pesadas fuera de Main:** Lecturas de `PackageManager` deben correr siempre bajo `Dispatchers.IO`.

---

## 4. ESTRUCTURA DE PAQUETES
```text
com.launcher.samiboxtv/
├── data/          # Implementación de repositorios, APIs locales y persistencia
├── domain/        # Modelos puros, Use Cases e interfaces de repositorios
├── presentation/  # UI Compose for TV, ViewModels, Theme y Overlays HUD
│   ├── home/
│   ├── settings/
│   ├── components/
│   └── theme/
├── util/          # Helpers de sistema, instalador de APKs y extensiones
└── di/            # Módulos de inyección y contenedores de dependencias
```
