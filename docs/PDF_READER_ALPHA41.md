# ProxyScroll Alpha 41 — Reader core repair

Alpha 41 intentionally fixes the PDF reader pipeline before adding more reader modes.

## Smart Resizer

Smart Resizer now has one responsibility: produce the bitmap that should be read.

1. Render the physical PDF page.
2. In Smart Resizer, if the physical page is landscape (`width / height >= 1.18`), expose it as two virtual reader pages: left half, then right half.
3. Analyze each virtual page independently.
4. Estimate the paper/background luminance from the rendered bitmap.
5. Detect stable printed/scanned content using row and column projections while rejecting solid scanner borders.
6. Create a new cropped bitmap with a small safety margin.
7. The UI centers and zooms this cropped bitmap; crop analysis never controls page position.

Original mode remains physically faithful and never splits or crops the source page.

## Zoom

Paged Original and Smart Resizer now use the same pinch/pan viewer. Horizontal page swiping is disabled while zoomed. Continuous reading also supports local pinch zoom without converting the whole reader into a nested scroll view.

## Linked PDF notes

A tap on a PDF note card opens the associated document at the stored physical PDF page. A long press opens editing. PDF note titles are now persisted in addition to excerpt and comment text.

## Android PDF integration

ProxyScroll registers for Android PDF VIEW and SEND intents. PDFs opened through another app are copied into app-private storage when possible, added to the library, and opened immediately. The internal copy is exposed through a FileProvider so the original PDF can be exported/shared from ProxyScroll without re-encoding it.
