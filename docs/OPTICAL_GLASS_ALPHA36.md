# Alpha 36 Optical Glass

Alpha 36 replaces the decorative Liquid Glass approximation with a shared screen-space optical model.

## Transmission and refraction

The background and glass surfaces now render the same environmental scene. Each surface reprojects that scene from its real window position, so broad light ribbons, colour wells, and optical landmarks remain continuous across the screen but visibly shift at the glass boundary.

Role-specific magnification creates the material hierarchy:

- cards use thin 1.8% magnification;
- inputs use a softer 2.4% frosted sample;
- the central action behaves as a thick 4% optical lens;
- overlays use dense 3% privacy glass.

Finger position adds a bounded displacement to the sampled scene. The surface geometry and text remain stable.

## Blur and fallback

Android 12 and newer use a hardware-backed blur layer for the reprojected backdrop. Blur strength depends on role, material depth, focus, and press clarity. The Lite material-motion profile and older Android versions use the same transmitted scene without the expensive blur plus a small neutral readability veil.

## Physical edge

Optical Glass uses a double bevel rather than a luminous outline:

- a bright top-left Fresnel edge;
- a darker bottom-right thickness edge;
- a second inner rim;
- sub-pixel cyan and violet dispersion restricted to the bevel;
- a restrained contact shadow.

The coloured material grain and large internal glow wells were removed from glass surfaces. Colour now belongs to the environment behind the glass.

## Interaction and hierarchy

Glass no longer scales, bends its corners, or wobbles as a whole. Touch temporarily increases transmission, moves the sampled scene, and relocates the specular response. The shared bottom navigation is a full-width optical slab, while inset controls reuse a shallow recessed version of the same backdrop.

The storage keys and data model are unchanged. Existing notes, PDFs, quotes, covers, reading progress, and theme preferences remain compatible.
