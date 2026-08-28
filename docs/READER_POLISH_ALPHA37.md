# Alpha 37 Reader Polish

Alpha 37 makes the phone reading path the default and fixes the bottom navigation geometry on devices with gesture or classic three-button system navigation.

## Notes gesture

Dragging the Notes screen upward by roughly one finger height and holding briefly opens the group filter rail. A quick scroll does not activate it; the finger has to remain nearly still for 280 milliseconds after crossing the threshold. The existing filter button remains available.

## Navigation safe area

The raised central action is no longer nested inside the clipped bottom slab. The slab and lens are sibling layers, and the bar measures the actual navigation-bar inset. This preserves the whole lens rim and gives lists enough bottom content space.

## PDF atmosphere

Every rendered PDF page now produces a tiny three-pass CPU-blurred backdrop. The backdrop is stretched behind the page across the full reader and receives the same Original, Sepia, Night, Warm, or Contrast colour matrix as the page. Uniform pages become a quiet continuous field; colourful pages become a soft ambient extension without exposing distracting detail. Because the blur is generated with the page, it also works on Android versions without hardware background blur.

## Phone-first reading

Smart Crop is the default PDF layout. It removes dead margins and presents the readable area at phone width. Reader controls hide automatically after a short pause, and Smart Crop/Reflow release their top and bottom chrome space while the controls are hidden. Diagnostic analysis cards were removed from the reading stream, and page fragments now use a restrained paper edge and shadow instead of another glass container.
