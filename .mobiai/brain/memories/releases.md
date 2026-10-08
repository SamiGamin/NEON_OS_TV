# Releases

<!--
Release notes, checklists and lessons learned during shipping.
-->

## Release v1.0.3 — System Telemetry, RAM Cleaner & OTA Updates

- id: release-v1-0-3
- type: release_note
- status: active
- platform: android
- area: shipping
- date: 2026-10-07
- version: 1.0.3
- version_code: 4

### Features & Improvements
- **Real-time Hardware Telemetry HUD**: Monitors RAM physical consumption, CPU usage, CPU temperature, storage space, and uptime with live visual indicators.
- **Rootless RSS RAM Process Manager**: Native Linux process table scanner that displays memory consumption across all active apps and system services.
- **One-Click RAM Cleaner**: Force-stops background tasks and clears cached processes with feedback in UI.
- **GitHub Releases OTA Update Checker**: Automates version verification against private/public GitHub Releases with Bearer Token support.
- **Compose TV Immutability**: Annotated all UI and Domain state models with `@Immutable` and utilized `collectAsStateWithLifecycle` to prevent unnecessary recompositions on TV boxes.
- **Downloader / AFTVnews Integration**: Release asset published at `https://github.com/SamiGamin/SamiBoxTV/releases/download/v1.0.3/SamiBoxTV-v1.0.3-release.apk`.

