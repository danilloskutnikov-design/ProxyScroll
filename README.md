# ProxyScroll

ProxyScroll is an offline-first notes app for Android. The current build is **0.3.0-alpha03**.

## Alpha features

- Full-screen note editor with automatic local saving
- Bold, underline, strikethrough, and four text sizes
- Rich formatting preserved in note previews and local storage
- Three input-motion modes: Direct, Gentle, and Flowing
- Search by title or body
- Pin and delete notes
- Liquid Glass material with translucent rims, refraction, and slow ambient light
- Royal Graphite material generated procedurally from cold sheen, layers, and micro-grooves
- Smooth theme and typography transitions
- Persistent appearance and input settings

## Storage and compatibility

Notes remain local and offline. The Alpha stores plain searchable text plus formatting ranges through the `NotesRepository` contract. Notes created by earlier Alpha versions are migrated automatically with unformatted body text.

The UI depends on the domain repository contract rather than its SharedPreferences implementation. A database, sync engine, encryption layer, or plugin-backed repository can replace it without changing the editor.

## Build

GitHub Actions builds the debug APK on every push to `main`. Open the latest **Build Alpha APK** workflow run, download the `ProxyScroll-0.3.0-alpha03-debug` artifact, unzip it, and install the APK on Android 8.0 or newer.

For a local build with JDK 17, Android SDK 35, and Gradle 8.9:

    gradle :app:assembleDebug
