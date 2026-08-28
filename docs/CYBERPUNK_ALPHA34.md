# Alpha 34 Cyberpunk theme

Alpha 34 adds Cyberpunk as a fifth persistent `AppTheme`. Selecting it in Appearance stores the `cyberpunk` key through the existing theme preferences, so the choice survives process restarts and upgrades.

## Visual language

The NIGHT//SIGNAL system uses signal yellow as the primary action and rail colour, coal black for reading planes, emergency red for alerts and fault lines, and cyan only as a chromatic split. Surfaces use asymmetric cut corners, hard highlights, circuit contacts, scanlines, short RGB fragments, and compact industrial labels.

The full-screen substrate is procedural Compose drawing. Angular signal panels and circuit traces stay near the viewport edges; the centre remains dark and quiet for long-form reading. Glitch animation is driven by the existing shared material clock and obeys Typing Quiet and the Auto, Full, and Lite material-motion profiles.

## Readability and performance

Text is never displaced or duplicated. RGB separation is limited to the brand mark, material rims, and decorative fragments. Grain remains cached in the existing mirrored atlases, while the animated layer draws a bounded number of rectangles and paths rather than per-pixel effects.

Cyberpunk keeps its own dark Material 3 colour scheme and system-bar icon mode. Existing notes, PDFs, quotes, covers, and appearance settings require no migration.
