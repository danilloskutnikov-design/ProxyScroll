# ProxyScroll Alpha 38 — Adaptive PDF Flow

Alpha 38 continues the reader work from PRs #39–#40 and focuses on the physical reading viewport rather than adding another visual layer.

## Rendering pipeline

1. Render the source PDF page once at a bounded display resolution.
2. Sample a thin band around all four page edges.
3. If the edge is uniform, extend that exact paper color to the whole display. Near-white paper is normalized to true white.
4. If the edge is visually complex, create a tiny CPU-blurred backdrop and stretch it behind the safe reading viewport.
5. Analyze an OCR-free luminance mask for content bounds and gutters.
6. Smart Resizer removes dead margins. A strict landscape + persistent-center-gutter test can split scanned book spreads into left/right virtual pages.
7. Smart Reflow can additionally sequence true text columns.

## Safe reading viewport

The atmosphere remains edge-to-edge so the status/navigation-bar region belongs visually to the document. The actual PDF paper is rendered inside `statusBarsPadding()` + `navigationBarsPadding()`, so text no longer sits under the phone's system UI.

## Navigation

The reader now exposes two navigation modes:

- **Листание** — horizontal page paging, with pinch zoom/pan for Original layout.
- **Прокрутка** — one continuous vertical PDF stream. Smart Resizer/Reflow regions are emitted directly into the stream instead of nesting another document pager.

Both modes share page progress, saved position, reading filters, quote creation and the same page-render cache.

## Spread safety

A center gutter alone is not enough to split a page. Spread splitting requires a wide/landscape source page, a persistent low-ink center band, meaningful content on both halves, and a confidence threshold. Portrait two-column documents therefore stay in the column-analysis path instead of being misclassified as scanned book spreads.
