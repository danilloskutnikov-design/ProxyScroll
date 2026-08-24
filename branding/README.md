# ProxyScroll identity assets

## Masters

- `vector/proxyscroll-mark.svg` — standalone continuous P/S scroll ribbon. Clean vector geometry for the app icon and shader mask.
- `vector/proxyscroll-wordmark.svg` — standalone `ProxyScroll` wordmark converted to outlines; no runtime font dependency.
- `proxyscroll-logo-optical-reference-v1.png` — full-resolution transparent raster reference preserving the generated glass, light, micro-grain, and caustic treatment.
- `proxyscroll-logo-optical-reference-v1.webp` — compact repository preview of the same optical reference.

## Future shader stack

The flat vector alpha is the geometry source. Render the material in this order:

1. scene colour wells;
2. transmitted aurora fill;
3. fine spectral grain;
4. soft internal frost;
5. graphite depth on the lower fold;
6. bright outer rim;
7. thin inner rim;
8. touch-following caustic highlight.

The wordmark should normally remain on a stable, undistorted content plane. A restrained colour gradient is allowed, but refraction and displacement belong to the symbol only.

