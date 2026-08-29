# Alpha 41 focused test matrix

- Portrait scanned PDF with wide white margins: Smart Resizer removes visible dead margins and remains centered.
- Portrait page with dark/scanner edge: border is rejected instead of becoming fake content.
- Landscape PDF page: Smart Resizer exposes left and right virtual pages in that order; each half crops independently.
- Original mode: no split/crop.
- Paged Original: pinch zoom + pan; page swipe resumes at 1x.
- Paged Smart Resizer: same pinch zoom + pan behavior.
- Continuous mode: document remains vertically scrollable at 1x and individual page can be pinched.
- PDF note card tap: opens its book at the linked source PDF page.
- PDF note long press: opens editor; title/excerpt/comment persist.
- Android Open with: selecting ProxyScroll for a PDF imports a durable internal copy and opens it.
- Android Share to ProxyScroll: imports and opens PDF.
- Reader/library export: Android share sheet receives the original PDF URI with read permission.
