# Alpha 13 Material Optics

## Reference decomposition

The target material is built from three spatial scales that must remain independent:

1. **Macro light field** — two or three broad, borderless colour wells moving beneath the interface.
2. **Meso optics** — an elongated diagonal highlight, corner bloom, and asymmetric light absorption.
3. **Microstructure** — dense one-pixel spectral inclusions with rare bright caustic cores.

The perceived thickness comes from two rims: a bright outer contour and a thinner inner contour. The top and left edges transmit more white light; the lower edge carries colour or absorbs light. A drop shadow alone cannot represent this volume.

## Grain model

The grain is not monochrome noise and is not a collection of large dots. It is a continuous optical layer inside the material:

- high density and low per-pixel alpha;
- hues sampled from the active palette;
- sparse near-white caustic crystals;
- cold steel, violet, and petroleum-green inclusions in Graphite Oil;
- macro light drawn below the grain so movement changes its apparent colour;
- frost drawn above it so clarity changes its contrast.

Alpha 13 generates one deterministic `128 × 128 px` ARGB tile per theme/palette and repeats it with `ImageShader`. Every surface shares the cached brush. This produces thousands of inclusions with one draw call instead of executing particle loops on every frame and in every card.

## Performance policy

- one animated scene Canvas;
- no particle allocations or loops in draw frames;
- cached grain bitmap and tiled shader;
- bounded, moderate settings blur plus an API-independent optical veil;
- no touch-perspective state updates on the full settings panel while it scrolls;
- stable content plane separated from the animated material plane;
- ambient motion calms during typing.

## Settings crash mitigation

The previous full-screen `Unbounded` blur expanded the offscreen render layer while the large settings material also reacted to every pointer move. Alpha 13 uses a bounded `12 dp` blur and disables touch deformation for the settings material. The fog veil preserves separation on devices where native blur is unavailable or expensive.

