# Decisions

<!--
Architecture decisions specific to this project.
Append entries with: mobiai brain save decision.
Each entry should record: title, status (active|deprecated), platform,
area, date, decision, reason, files.
-->

## Clean Architecture and Package Modularization Standard

- id: clean-architecture-modularization
- type: architecture_decision
- status: active
- platform: android
- area: architecture
- date: 2026-05-27

### Decision / Problem / Pattern
The project has grown as a single-module `app` with a basic package structure (`data`, `ui`, and root classes). To comply with the strict project guidelines defined in `GEMINI.md`, the codebase must adopt a clean, highly modular architecture separating logic into layers:
- `core/`: Common utilities, themes, system level base classes.
- `domain/`: Business entities and Use Cases/Interactors (completely framework-independent).
- `data/`: Repositories, data sources (SharedPreferences, Package Manager, Local/Remote databases).
- `presentation/`: UI state, ViewModels (using StateFlow with immutable UI states), and Compose Screen components.
- `services/`: Accessibility services and background monitor tasks.
- `di/`: Central container for manual dependency injection.

### Reason / Root Cause / Solution
A flat hierarchy creates high coupling between UI components and background logic (e.g. `MainActivity` instantiating `MemoryUsageTester` and `OverlayWindowManager` directly). Transitioning to Clean Architecture ensures:
1. **Separation of Concerns:** Business logic (Use Cases) is completely independent of Android frameworks (Package Manager, Window Manager).
2. **Testability:** Business logic can be unit-tested without needing Android instrumentation or mock context.
3. **Cohesive State Management:** StateFlow states are fully immutable, driven by declarative UI.

### Files
- [MainActivity.kt](file:///E:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/MainActivity.kt)
- [MainViewModel.kt](file:///E:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/MainViewModel.kt)
- [AppRepository.kt](file:///E:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/data/AppRepository.kt)

---

## Manual Dependency Injection Strategy (No Hilt)

- id: dependency-injection-manual
- type: architecture_decision
- status: active
- platform: android
- area: di
- date: 2026-05-27

### Decision / Problem / Pattern
It was decided to completely avoid the Hilt DI framework for this project. Instead, the codebase will implement manual dependency injection using constructor injection, factories, or a simple Service Locator pattern where direct injection is not possible (such as in AccessibilityServices or system broadcast receivers).

### Reason / Root Cause / Solution
Avoiding Hilt simplifies build setup, reduces compilation overhead, avoids additional annotation processing dependencies (like KSP), and keeps the project lightweight and direct. Manual DI will be achieved by:
1. Instantiating singletons (such as `OverlayWindowManager` and `AppPreferences`) in a central application class (`SamiBoxApplication`) or a custom simple Service Locator / Container.
2. Injecting these dependencies via constructors in ViewModels using a custom `ViewModelProvider.Factory`.
3. Accessing the central application container where constructor injection is impossible.

### Files
- [MainActivity.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/MainActivity.kt)
- [HomeViewModel.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/presentation/home/HomeViewModel.kt)
- [SamiBoxAccessibilityService.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/services/accessibility/SamiBoxAccessibilityService.kt)

---

## Compose TV State Stability and Immutability Optimization

- id: compose-stability-immutability
- type: architecture_decision
- status: active
- platform: android
- area: performance-compose
- date: 2026-10-07

### Decision / Problem / Pattern
In Compose TV apps running on resource-constrained TV boxes (1GB-2GB RAM, low-power quad-core SoCs), real-time telemetry flows (CPU, RAM, Uptime) emit new state every 1.5-3 seconds. Because data classes contained `Drawable` references (`iconDrawable`, `bannerDrawable`), Compose treated them as `@Unstable`, triggering expensive recomposition of all 20+ app banner cards on every tick.

### Reason / Root Cause / Solution
1. Explicitly annotated `AppItem`, `HomeUiState`, `SystemTelemetry`, `NetworkStatus`, `ProcessInfo`, and `UpdateInfo` with `@Immutable` (`androidx.compose.runtime.Immutable`).
2. Adopted `collectAsStateWithLifecycle()` in `HomeScreen.kt` so state observation and recomposition halt automatically when the launcher is in the background (e.g., when the user opens Netflix, YouTube, or Kodi).
3. Replaced Coil's asynchronous image loader with static vector `painterResource` for local icons like the Add App button.

### Files
- [AppItem.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/domain/model/AppItem.kt)
- [HomeUiState.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/presentation/home/HomeUiState.kt)
- [HomeScreen.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/presentation/home/HomeScreen.kt)
- [SamiBoxApplication.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/SamiBoxApplication.kt)

---

## Coil Memory Cache Limiting for Android TV Devices

- id: coil-memory-optimization-tv
- type: architecture_decision
- status: active
- platform: android
- area: memory-optimization
- date: 2026-10-07

### Decision / Problem / Pattern
By default, Coil allocates up to 25-50% of the app heap for image memory caching. On Android TV devices with small heaps, loading app icons and 16:9 banners can trigger high Garbage Collector pressure, leading to frame drops during horizontal D-Pad scrolling.

### Reason / Root Cause / Solution
Implemented `ImageLoaderFactory` directly on `SamiBoxApplication`:
- Capped `MemoryCache` to a conservative 15% of available heap (`maxSizePercent(0.15)`).
- Enabled hardware bitmaps (`allowHardware(true)`) for GPU-accelerated drawing.
- Disabled crossfade animations (`crossfade(false)`) to eliminate frame blending overhead on TV GPUs.

### Files
- [SamiBoxApplication.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/SamiBoxApplication.kt)

---

## Canonical Android TV Settings Panel & Dynamic App Card Formatting

- id: android-tv-settings-and-categories
- type: architecture_decision
- status: active
- platform: android
- area: settings-ui-and-preferences
- date: 2026-10-07

### Decision / Problem / Pattern
Users need to customize the launcher behavior directly from the TV interface via remote control:
1. Reordering or toggling quick-access favorites.
2. Creating and assigning custom thematic app rows/categories (e.g., STREAMING, GAMING, IPTV).
3. Switching between 16:9 widescreen banners, 1:1 modern square grids (Google TV style), and compact views.
4. Accessing shortcuts to set SamiBoxTV as default launcher, opening system Android TV settings, and triggering RAM cleanup.

### Reason / Root Cause / Solution
Built a split-pane lateral drawer styled identically to canonical Android TV / Google TV system settings with:
- Dark glass cyberpunk aesthetics with D-Pad focus borders.
- Two-column layout: lateral categories menu on the left, context options on the right.
- Directional remote input with automatic focus synchronization (`initialFocusRequester`, `tvClickable` extension with `KeyEventType.KeyDown`).
- Clean Architecture persistence pipeline: `PreferencesDataSource` -> `PreferencesRepository` -> `GetLauncherSettingsUseCase` / `SaveCardStyleUseCase` / `ManageCategoriesUseCase` -> `HomeViewModel` (StateFlow) -> `HomeScreen`.

### Files
- [TvSettingsPanel.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/presentation/settings/TvSettingsPanel.kt)
- [AppCardStyle.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/domain/model/AppCardStyle.kt)
- [SettingsSection.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/domain/model/SettingsSection.kt)
- [PreferencesDataSource.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/data/datasource/PreferencesDataSource.kt)
- [PreferencesRepositoryImpl.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/data/repository/PreferencesRepositoryImpl.kt)
- [HomeScreen.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/presentation/home/HomeScreen.kt)
- [HomeScreen.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/presentation/home/HomeScreen.kt)
- [HomeViewModel.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/presentation/home/HomeViewModel.kt)

---

## Agent Workflow & Testing Constraints: Pure Local Compose Architecture and Code Analysis

- id: agent-workflow-compose-and-testing-confirmation
- type: workflow_constraint
- status: active
- platform: android
- area: agent-workflow-and-testing
- date: 2026-10-08

### Decision / Problem / Pattern
To avoid unnecessary execution delays, context saturation, and background device interference:
1. Use MobiAI skills strictly focused on Compose architecture and static code analysis.
2. Under no circumstances run ADB shell commands (e.g., input keyevents, package installs), automated device tests, or screen captures (`screencap`, temporary `.png` files).
3. The assistant must limit operations to directly inspecting, editing, and compiling local files (`./gradlew assembleDebug` or targeted Gradle compilation).
4. For any major, critical changes or before running any testing suite, the assistant MUST explicitly ask the user for confirmation beforehand.

### Reason / Root Cause / Solution
Ensures ultra-fast, clean, local-only developer cycles without locking devices or polluting context windows.

### Files
- [GEMINI.md](file:///F:/AndroidStudio/SamiBoxTV/GEMINI.md)
- [.mobiai/brain/config.json](file:///F:/AndroidStudio/SamiBoxTV/.mobiai/brain/config.json)
