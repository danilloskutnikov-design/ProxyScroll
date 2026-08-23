# ProxyScroll

ProxyScroll is an offline-first notes app for Android. The current build is 0.2.0-alpha02.

## Alpha features

- Create and edit text notes
- Search by title or body
- Pin important notes
- Delete notes
- Local offline storage
- Smooth in-app theme switching
- Liquid Glass theme with luminous translucent surfaces
- Royal Graphite theme with a cold rain-darkened graphite texture
- Persistent appearance settings

## Architecture

The UI depends on the NotesRepository domain contract rather than a storage implementation. The Alpha uses a small SharedPreferences adapter; a database, sync engine, encryption layer, or plugin-backed repository can replace it without changing the Compose UI.

## Build

GitHub Actions builds the debug APK on every push to main. Open the latest **Build Alpha APK** workflow run, download the ProxyScroll-0.2.0-alpha02-debug artifact, unzip it, and install the APK on Android 8.0 or newer.

For a local build with JDK 17, Android SDK 35, and Gradle 8.9:

    gradle :app:assembleDebug
