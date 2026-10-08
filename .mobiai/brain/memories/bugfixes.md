# Bugfixes

<!--
Bugfixes and workarounds worth remembering for this project.
Append entries with: mobiai brain save bugfix.
Mark temporary workarounds as status: temporary so the agent does not
treat them as permanent decisions.
-->

## Memory Leak: System Overlay Lifecycle is never Stopped (RESOLVED)

- id: overlay-lifecycle-leak
- type: platform_workaround
- status: deprecated
- platform: android
- area: system-overlay
- date: 2026-05-27

### Decision / Problem / Pattern
In `OverlayWindowManager.kt`, a custom `OverlayLifecycleOwner` was instantiated to manage a ComposeView's lifecycle when rendered outside of a standard Activity (via `WindowManager` directly).
However, the `stop()` method of the `OverlayLifecycleOwner` was never invoked, neither in `OverlayWindowManager.hide()` nor in `MainActivity.onDestroy()`. This kept the Compose recomposer, observers, and scope active indefinitely, leaking RAM.

### Reason / Root Cause / Solution
**Resolved:** Saved the `lifecycleOwner` as a private property of `OverlayWindowManager`, and invoked `lifecycleOwner?.stop()` in `hide()`, cleaning up recomposition context and observers perfectly.

### Files
- [OverlayWindowManager.kt](file:///E:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/OverlayWindowManager.kt)

---

## Coroutine Leak: MemoryUsageTester Loop is never Cancelled (RESOLVED)

- id: coroutine-leak-memory-tester
- type: platform_workaround
- status: deprecated
- platform: android
- area: background-services
- date: 2026-05-27

### Decision / Problem / Pattern
In `MemoryUsageTester.kt`, a background monitoring loop was started via a newly instantiated `CoroutineScope(Dispatchers.IO)` with no parent Job, meaning it was treated as a global scope. Since it was never cancelled, the `while(isActive)` loop ran forever in the background even after `MainActivity` was destroyed.

### Reason / Root Cause / Solution
**Resolved:** Added a private `Job` property inside `MemoryUsageTester` and exposed `stopMonitoring()` which calls `job?.cancel()`. This function is invoked from `MainActivity.onDestroy()` to ensure clean thread termination.

### Files
- [MemoryUsageTester.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/core/utils/MemoryUsageTester.kt)
- [MainActivity.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/MainActivity.kt)

---

## Double / Inset Focus Border on TV Cards & Telemetry HUD (RESOLVED)

- id: tv-focus-inset-border-fix
- type: bug_fix
- status: active
- platform: android
- area: ui-compose-tv
- date: 2026-10-07

### Decision / Problem / Pattern
On TV Cards (`TvCyberBannerCard`, `TelemetryHudCard`), when receiving D-Pad focus with scale animation, an inner cyan rectangle was drawn inside the card rather than illuminating the outer border. This occurred because `.border()` was placed *after* `.padding()`, or inner child composables had their own duplicated focus/border states.

### Reason / Root Cause / Solution
**Resolved:**
1. Unified container modifiers: `.clip(shape) -> .background(...) -> .border(...) -> .padding(...)`. Padding must strictly follow the border.
2. In `TvCyberBannerCard`, used Compose TV's native `CardDefaults.border(...)` with `BorderStroke(2.dp, CyberCyan)` on focus and `scale = CardDefaults.scale(focusedScale = 1.06f)` so the border seamlessly scales around the outer perimeter without inner rectangles.

### Files
- [HomeScreen.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/presentation/home/HomeScreen.kt)

---

## ActivityManager Process Table Restriction in Modern Android (RESOLVED)

- id: rootless-rss-process-scanner-fix
- type: bug_fix
- status: active
- platform: android
- area: system-telemetry
- date: 2026-10-07

### Decision / Problem / Pattern
On Android 5.1+ through 14+, `ActivityManager.getRunningAppProcesses()` only returns the caller application's own process for security/sandboxing reasons. As a result, the RAM telemetry view could only display SamiBoxTV's RAM consumption, leaving the user unaware of what background apps (Google Services, TV setup, media apps) were consuming memory.

### Reason / Root Cause / Solution
**Resolved:** Implemented a rootless native Linux process table scanner in `SystemTelemetryDataSourceImpl`:
1. Executes `ps -A -o PID,NAME,RSS` (or fallback `/system/bin/ps`) via `ProcessBuilder`.
2. Parses process names, maps them to installed package names using `PackageManager.getInstalledApplications()`, and reads the real physical Resident Set Size (`RSS`) in Kilobytes.
3. Groups and sorts processes by RAM usage descending, showing full hardware memory footprint accurately without requiring device root.

### Files
- [SystemTelemetryDataSource.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/data/datasource/SystemTelemetryDataSource.kt)
- [SystemMonitorLog.kt](file:///F:/AndroidStudio/SamiBoxTV/app/src/main/java/com/launcher/samiboxtv/presentation/overlay/SystemMonitorLog.kt)

