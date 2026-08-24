# ProxyScroll

ProxyScroll is an offline-first notes app for Android. The current build is **0.10.0-alpha23**.

## Alpha features

- Full-screen note editor with automatic local saving
- Automatic three-word titles when the title field is left empty
- Bold, underline, strikethrough, and font sizes from 10 to 72 sp
- Font-size adjustment in two-point steps or by an exact custom value
- Selection controls below the content plane, with native handles and smart word, sentence, and paragraph expansion
- Range-based rich-text rendering that keeps IME input independent from formatting
- Rich formatting preserved in note previews and local storage
- Three input-motion modes: Direct, Gentle, and Flowing
- Search by title or body
- Pin and delete notes
- Liquid Glass material with translucent rims, refraction, and slow ambient light
- Royal Graphite material generated procedurally from cold sheen, layers, and micro-grooves
- OldScroll material with ivory paper, directional fibres, warm dust, worn edges, and angular geometry
- Ordered multi-selection with visible selection order, select-all, bulk pinning, colour, and Trash actions
- Permanent human-readable note indices that survive sorting, deletion, and restoration
- Seven-day Trash with automatic expiry, restoration, permanent deletion, and empty-all confirmation
- Theme-first geometry presets with optional manual Shape Studio override
- OldScroll notebook ruling embedded beneath the paper grain and ageing layers
- One shared low-cost optical clock that synchronizes background light, surfaces, and the ProxyScroll brand
- Material-specific ambient response: refractive caustics, wet graphite reflections, or diffuse parchment light
- Smooth theme and typography transitions
- Scroll-stable note cards with animated placement and physical press feedback
- Persistent appearance and input settings
- Shape Studio with an animated live preview
- Global corner character from angular to soft, plus separate card, input, and button controls
- Focus-first editor surface and compact note cards
- Coalesced Undo and Redo history tuned for continuous typing
- Compact selection context that never covers the active fragment
- One-tap formatting reset for selected text
- Lifecycle-safe saving when the app moves to the background
- Recoverable deletion with an Undo snackbar
- Adaptive settings fog that blurs and lowers the contrast of content beneath the sheet
- Separate content, vibrant inset, and navigation material layers
- Refined Liquid Glass translucency with role-aware optical depth
- Rebuilt Royal Graphite with cold wet sheen, sparse rain light, and no repetitive grid texture
- Stained Liquid Glass with one continuous three-well color field shared by the whole screen
- Aurora Opal, Coral Glacier, and Nordic Bloom palettes with persistent intensity controls
- Graphite Oil optics with restrained steel, petroleum, and northern-green inclusions
- Material depth and ambient-light motion presets with a real live preview
- A stable content plane over a subtly perspective-reactive material plane
- Typing Quiet mode that calms ambient material motion while the editor receives input
- Debounced appearance persistence so live sliders do not write settings on every pixel
- Stateful material deformation: squeeze, bulge, perspective, and spring recovery
- Interactive clarity: glass loses frost under touch or focus and slowly clouds again at rest
- Finger-tracked specular lighting with a brighter inner rim and role-aware compression
- Focus-aware search and editor materials with an undistorted editor content plane
- In-app settings layer that cannot leave an invisible modal gesture blocker behind
- Scroll-safe settings sheet with explicit dismissal instead of accidental swipe closure
- Living subglass bloom that remains in motion behind the settings material
- Spectral micro-grain with palette-tinted halos and sparse caustic cores
- Cached high-frequency spectral grain shader with no per-particle frame loops
- Bounded settings blur and a non-interactive material plane for safer low-end GPU rendering
- Persistent colour flags with automatic note grouping and material flag accents
- Stable editor content plane with focus-driven liquid corner morphing
- IME-safe subtext optical trail with a wide low-energy glow that never paints over glyphs
- Opaque privacy frost that makes background content unreadable behind settings
- Scale, shape, and directional transition morphing between notes, editor, and settings
- Long-press colour editing directly from a note card, plus a compact editor palette
- Keyboard focus mode with a larger, higher-contrast writing plane and reduced chrome
- Scroll-safe card material without per-item viewport re-entry animations or pointer optics
- 0.7 Material Motion runtime that quiets ambient optics during active scrolling and restores them softly at rest
- Explicit V2 and V3 APK signatures verified by Android build tools in CI for the Android 8+ minimum target
- Sidecar installer for signature-conflicted early Alpha installations, without deleting the existing app
- Explicit settings z-order that keeps privacy fog below the sheet during live theme changes
- Stable settings chrome while Shape Studio morphs only the live preview and target controls
- Material-specific mechanics: elastic Liquid Glass, restrained Graphite, and stiff OldScroll paper
- Large mirrored optical-grain atlases whose repeat period exceeds an individual card
- Chromatic micro-lenses with bright faces, tinted shadows, and sparse refractive halos
- Low-cost optical motion trails that follow the shared material light without blurring text
- Auto, Full, and Lite material-motion profiles with automatic low-RAM adaptation
- Liquid group orbs that filter notes with a radial colour wave from the touched position
- Persistent custom groups with user-defined names and colours
- Contextual bulk assignment: select cards, then touch a group orb
- Backward-compatible migration from fixed colour flags to built-in groups
- LiteLife: a fourth, minimal dark theme with flat readable planes and no grain or optical trails

## Storage and compatibility

Notes remain local and offline. The Alpha stores plain searchable text plus formatting ranges through the `NotesRepository` contract. Notes created by earlier Alpha versions are migrated automatically with unformatted body text.

The UI depends on the domain repository contract rather than its SharedPreferences implementation. A database, sync engine, encryption layer, or plugin-backed repository can replace it without changing the editor.

## Build

GitHub Actions builds two verified APKs on every push to `main`. Use `ProxyScroll-0.10.0-alpha23-update.apk` to update the normal package while keeping local notes. If Android reports a signature conflict from an early Alpha, install `ProxyScroll-0.10.0-alpha23-sidecar.apk` beside it instead. Both APKs are contained in the `ProxyScroll-0.10.0-alpha23-installers` artifact and support Android 8.0 or newer.

Alpha APKs use a repository-local debug-only signing key so later Alpha builds can update in place without erasing local notes. This key must never be used for a production release.

For a local build with JDK 17, Android SDK 35, and Gradle 8.9:

    gradle :app:assembleDebug
