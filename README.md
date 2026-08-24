# ProxyScroll

ProxyScroll is an offline-first notes app for Android. The current build is **0.5.1-alpha07**.

## Alpha features

- Full-screen note editor with automatic local saving
- Automatic three-word titles when the title field is left empty
- Bold, underline, strikethrough, and font sizes from 10 to 72 sp
- Font-size adjustment in two-point steps or by an exact custom value
- Selection Lens with native handles and smart word, sentence, and paragraph expansion
- Range-based rich-text rendering that keeps IME input independent from formatting
- Rich formatting preserved in note previews and local storage
- Three input-motion modes: Direct, Gentle, and Flowing
- Search by title or body
- Pin and delete notes
- Liquid Glass material with translucent rims, refraction, and slow ambient light
- Royal Graphite material generated procedurally from cold sheen, layers, and micro-grooves
- Smooth theme and typography transitions
- Staggered note appearance, animated placement, and physical press feedback
- Persistent appearance and input settings
- Shape Studio with an animated live preview
- Global corner character from angular to soft, plus separate card, input, and button controls
- Focus-first editor surface and compact note cards
- Coalesced Undo and Redo history tuned for continuous typing
- Inline Selection Lens anchored near the active fragment
- One-tap formatting reset for selected text
- Lifecycle-safe saving when the app moves to the background
- Recoverable deletion with an Undo snackbar

## Storage and compatibility

Notes remain local and offline. The Alpha stores plain searchable text plus formatting ranges through the `NotesRepository` contract. Notes created by earlier Alpha versions are migrated automatically with unformatted body text.

The UI depends on the domain repository contract rather than its SharedPreferences implementation. A database, sync engine, encryption layer, or plugin-backed repository can replace it without changing the editor.

## Build

GitHub Actions builds the debug APK on every push to `main`. Open the latest **Build Alpha APK** workflow run, download the `ProxyScroll-0.5.1-alpha07-debug` artifact, unzip it, and install the APK on Android 8.0 or newer.

Alpha APKs use a repository-local debug-only signing key so later Alpha builds can update in place without erasing local notes. This key must never be used for a production release.

For a local build with JDK 17, Android SDK 35, and Gradle 8.9:

    gradle :app:assembleDebug
