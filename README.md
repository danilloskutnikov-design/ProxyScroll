# ProxyScroll

ProxyScroll is an offline-first notes and reading app for Android. The current build is **0.24.0-alpha37**.

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
- Optical Glass material with screen-space transmission, magnification, blur, and a physical bevel
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
- One continuous environmental light field shared by the background and every refractive glass surface
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
- Material-specific mechanics: stable Optical Glass, restrained Graphite, and stiff OldScroll paper
- Large mirrored optical-grain atlases whose repeat period exceeds an individual card
- Chromatic micro-lenses with bright faces, tinted shadows, and sparse refractive halos
- Low-cost optical motion trails that follow the shared material light without blurring text
- Auto, Full, and Lite material-motion profiles with automatic low-RAM adaptation
- Liquid group orbs that filter notes with a radial colour wave from the touched position
- Persistent custom groups with user-defined names and colours
- Contextual bulk assignment: select cards, then touch a group orb
- Backward-compatible migration from fixed colour flags to built-in groups
- LiteLife: a fourth, minimal dark theme with flat readable planes and no grain or optical trails
- Group Studio for renaming and recolouring custom note groups without losing assignments
- Full group selection inside the editor, including custom groups and an explicit ungrouped state
- Persistent group filter restored after restarting the app, with safe fallback after deletion
- Cleaner LiteLife editor focus with restrained surfaces and no decorative typing trail
- Strict LiteLife geometry with zero-radius cards, inputs, buttons, overlays, and colour tiles
- Static LiteLife rendering with flat fills and no glow, deformation, optical drift, or press springs
- Brighter semantic text roles across Royal Graphite and LiteLife dark palettes
- On-demand group palette that stays hidden until the filter control is opened
- Soft liquid group drops with press deformation and a colour wave that spreads from the touch point
- Flat square group colours in LiteLife instead of volumetric liquid orbs
- ProxyScroll Labs as a dedicated settings tab for opt-in experimental features
- Sensor-fused Micro Stabilization that compensates bounded high-frequency shake in the notes feed
- Interaction-safe stabilization that pauses during scrolling and never moves the active editor
- Travel Cues that visualize filtered acceleration at the screen periphery without moving text
- Live sensor preview, persistent strength control, capability detection, and lifecycle-safe sensor registration
- Read-first navigation: existing notes open as calm, keyboard-free pages instead of immediately entering edit mode
- Full-screen reading typography with persistent font scale, line spacing, and page-width controls
- Pinch-to-resize reading text plus tap-to-edit with the cursor placed at the touched character
- IME-aware writing layout that widens the text plane and reduces padding while the keyboard is visible
- Two-step Back behavior that dismisses the keyboard before leaving the editor
- Reading-safe motion compensation that pauses while the page is actively scrolling
- Gesture-safe reading: one-finger scrolling is never captured by tap or pinch handling
- Formatting toolbar protected from three-button and gesture-navigation system insets
- Body, H1, H2, subtitle, and caption typography presets for selections and new text
- Persistent start, centre, end, and justified note alignment across editor, reader, and cards
- A dedicated Library tab with Android document-picker PDF import and persistent URI access
- Local PDF page rendering, previous/next navigation, error recovery, and remembered reading progress
- A tactile virtual bookshelf with dimensional shelf lighting, book spines, bookmarks, paper edges, and procedural cover materials
- Editable display titles and authors while preserving the original imported filename for safe re-imports
- Classic, cloth, paper, night, and minimal cover styles with persistent colour presets
- Optional user-selected cover artwork with persistent Android document access
- Local book quotes and reading notes linked to their source PDF and exact page
- Quote creation directly from the PDF reader, plus editing and deletion from the Library
- Continue-reading and shelf note counters, status filters, and searchable author metadata
- Cyberpunk: a fifth persistent theme built around signal yellow, coal black, emergency red, and cyan RGB split
- Asymmetric cut-corner cards, HUD-like insets, circuit traces, warning rails, scanlines, and restrained glitch fragments
- Animated chromatic faults stay on material edges and background rails so reading text remains stable
- A dedicated NIGHT//SIGNAL theme preview, material badge, angular settings sheet, and cyberpunk brand treatment
- Compact edge-to-edge Notes and Library layouts on a shared 12 dp screen grid
- Five-position bottom navigation with one raised central action for note creation or book import
- Progressive search, filters, and groups that stay out of the content plane until requested
- Denser note cards, reading shelves, quote cards, and settings without removing their actions
- A single-row theme picker and restrained material hierarchy with fewer nested surfaces
- Screen-space backdrop reprojection that visibly breaks light landmarks at glass boundaries
- Role-aware optics: thin cards, frosted inputs, a thick central lens, and privacy-glass overlays
- Hardware background blur on Android 12+ with a low-cost refractive fallback for older devices
- Double Fresnel bevels with restrained edge-only chromatic dispersion and contact shadows
- Stable text and geometry while touch changes transmission, displacement, and specular light
- Up-and-hold note gesture that reveals group filters without occupying the content plane
- A separately layered central navigation lens with correct gesture and 3-button system insets
- Smart Crop as the phone-first PDF default, with reduced chrome and automatic control hiding
- CPU-blurred page atmosphere across the whole reader, including every PDF colour profile

## Storage and compatibility

Notes, library metadata, cover choices, reading progress, quotes, and book notes remain local and offline. The Alpha stores plain searchable text plus formatting ranges through the `NotesRepository` contract. Notes and PDF entries created by earlier Alpha versions are migrated automatically.

The UI depends on the domain repository contract rather than its SharedPreferences implementation. A database, sync engine, encryption layer, or plugin-backed repository can replace it without changing the editor.

## Build

GitHub Actions builds two verified APKs on every push to `main`. Use `ProxyScroll-0.24.0-alpha37-update.apk` to update the normal package while keeping local data. If Android reports a signature conflict from an early Alpha, install `ProxyScroll-0.24.0-alpha37-sidecar.apk` beside it instead. Both APKs are contained in the `ProxyScroll-0.24.0-alpha37-installers` artifact and support Android 8.0 or newer.

Alpha APKs use a repository-local debug-only signing key so later Alpha builds can update in place without erasing local notes. This key must never be used for a production release.

For a local build with JDK 17, Android SDK 35, and Gradle 8.9:

    gradle :app:assembleDebug
