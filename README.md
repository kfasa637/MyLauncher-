# mALauncher

An independent Android launcher project built with Kotlin, Jetpack Compose and Android launcher APIs.

The goal is to recreate the interaction model and configurable home-screen experience of a modern category-based launcher, using original implementation and assets. This project does not contain Smart Launcher source code or proprietary resources.

## Current foundation

- Android HOME app registration
- Installed-app discovery and launch
- App drawer with search
- Basic package-based app categories
- Persistent launcher preferences
- Home-screen appearance controls: accent, grid columns, icon size, clock size and labels
- Feature boundaries for Android widgets and wallpaper effects
- GitHub Actions debug APK build

## Planned implementation stages

1. **Launcher foundation** — app discovery, drawer, navigation and persisted settings.
2. **Smart Launcher-style organization** — editable home pages, category management, folders, favorites and gesture navigation.
3. **Widget system** — Android AppWidgetHost integration, launcher smart widgets, resize and placement.
4. **Visual customization** — icon packs, typography, grids, page layouts, themes and backups.
5. **Wallpaper effects** — parallax/depth, blur and dimming controls.
6. **Floating widgets** — overlay implementation subject to Android permissions and OS restrictions.
7. **Stability and release** — device testing, performance, accessibility and signed release builds.

## Build

Run `gradle assembleDebug` with Gradle 8.9 and Android SDK installed.

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions builds a debug APK on pushes to `main`. Open the Actions run and download the `mALauncher-debug-apk` artifact after a successful build.

## Important

This is an evolving project. Features listed under planned implementation stages are not yet implemented. Android restricts some capabilities, including third-party widget hosting, wallpaper motion sensors and floating overlays; these will be implemented using supported Android APIs and permissions.
