# Alpha 42 — Living Glass

## Material

`LivingGlass.kt` owns the Liquid Glass surface. The screen-space environment is refracted at rounded boundaries using AGSL on API 33+, with a blurred input RenderEffect. Surface text is a separate draw layer. White frost has distinct values for note cards, inputs, action buttons and overlays, so readability is independent from backdrop color.

The dock receives an explicit `GlassBackdrop`, recorded only by its sibling scrolling body. The source never reads its own recording. This avoids feedback cycles while showing actual note cards and book covers behind the dock. API 31–32 use blur; older systems and Lite material quality retain a clean fallback. Native shader creation is guarded.

Touch observation runs in the initial pointer pass and consumes no input. It cancels when movement crosses touch slop or a second pointer appears. The highlight follows contact, while existing click, long press, scrolling and pinch handlers own the gesture. Press motion uses a damped spring and uniform scaling.

## Comfort

The dock floats above navigation insets; the center action is outside its clipping shape. Lists extend behind it and reserve equivalent bottom content padding. Note excerpts use a consistent font size regardless of rich-text heading spans; full formatting is retained in the reader and editor. Pin and formatting buttons use 48 dp targets. Settings keep dismissal and tabs visible while scrolling. Filter controls precede the bookshelf. The editor keeps its horizontal text measure stable on focus.

Ambient light and sparse background dust settle during reading and typing. A zero motion scale stops observation of the infinite phase. The system animator setting disables the material motion profile. Existing appearance presets and persistence keys remain intact.

## Verification

The build workflow assembles both the update and preview installers, aligns them, signs with the established Alpha key and verifies signatures and manifests.

`VisualSmokeTest` runs on an Android 15 emulator. It directly compiles the native glass effect, records screenshots of the actual application, exercises navigation and note persistence, opens a generated PDF through Android intents, and checks the LiteLife fallback. Device screenshots and runtime logs are collected by the Visual device check workflow.

Android 8–12 fallback branches are guarded in code; device-specific smoothness and vibration strength need phone testing.

## Rendering references

- https://developer.android.com/develop/ui/compose/graphics/draw/modifiers
- https://developer.android.com/reference/android/graphics/RenderEffect
- https://developer.android.com/reference/android/graphics/RuntimeShader
