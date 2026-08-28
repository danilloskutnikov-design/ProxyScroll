# Alpha 35 compact interface

Alpha 35 rebuilds the main application shell around the visual hierarchy of the supplied library reference while preserving ProxyScroll's materials and every existing data path.

## Shared navigation

- Notes, Library, Search, and Settings use fixed positions in one edge-to-edge bottom bar.
- The raised centre action is the only dominant control: it creates a note on Notes and imports a PDF on Library.
- Active destinations use colour and a thin signal rail instead of another filled card.
- The bar owns system navigation insets, so screens no longer reserve a second empty band.

## Notes

- The centred app bar and duplicate floating action were removed.
- A compact left-aligned title block now shares one row with Search, Trash, and Groups.
- Search and the group rail are progressive controls and appear only when requested.
- Notes use a 12 dp edge grid, tighter group headers, two-line previews, and smaller internal spacing.

## Library

- Header, continue-reading shelf, bookshelf, filters, and quote rail share the same 12 dp grid.
- Search and reading-status filters are independent progressive controls.
- Covers, quote cards, counters, and empty states use denser measurements without losing metadata or edit gestures.

## Settings

- The sheet is almost edge-to-edge and uses the same 12 dp horizontal grid.
- Theme selection is one horizontal rail instead of a three-row card matrix.
- Swatches and theme options are smaller; material controls and Shape Studio remain available.

The update does not change note, library, quote, cover, theme, or progress persistence. Existing Alpha data remains compatible.
