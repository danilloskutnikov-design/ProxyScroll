# Alpha 33 library model

Alpha 33 turns the PDF list into a tactile bookshelf while keeping all user data local.

## Persistent book metadata

Each `LibraryDocument` now keeps the imported filename separately from its display title. Re-importing the same URI refreshes the source name without replacing a title chosen by the user. Author text, reading status, cover material, colour, and an optional persisted image URI are stored with the existing progress record.

Existing `pdf_documents_v1` JSON is read with compatible defaults. No destructive migration is required.

## Quotes and notes

`BookQuote` stores an excerpt, an optional personal note, the owning document ID, and a zero-based PDF page. Quotes use the separate `book_quotes_v1` preference key. Removing a document also removes its associated quotes, but never deletes the source PDF from the device.

The PDF reader creates a quote at the current page. The Library shows, edits, and deletes these entries and derives cover and hero counters from the quote collection.

## Cover rendering

The default covers are procedural Compose artwork rather than bundled bitmaps. Classic, cloth, paper, night, and minimal styles combine deterministic microtexture, edge shading, spine depth, bookmarks, and the selected base colour. A user-selected image can replace the artwork while the physical book treatment remains visible.
