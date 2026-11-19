# Journey

An editorial Compose motion study: a travel card unfolds into a Dolomites destination view, with a growing mountain photograph and a rising detail sheet.

- Open **Journey** from the collection, then tap the mountain card to explore the destination.
- Use the back arrow to collapse the destination. System Back first closes an expanded destination or stops autoplay; another Back returns to the collection.
- Tap the heart or **Save this escape** to toggle the same saved selection. The detail button shows **Escape saved** while selected.
- **Play the journey** starts a repeating open-and-close demonstration. **Stop the journey** stops autoplay at its current state. Manually opening or closing the destination also stops autoplay.
- Both the collection view and destination details scroll when the available height or text size requires it.

## Motion and rendering

`JourneyRoute` owns the expanded state and autoplay. A single `updateTransition` produces progress from 0 to 1 over 1,150 ms using the cubic Bézier easing defined in `JourneyMotion`.

The same hero remains mounted as its bounds interpolate from the rounded card into the full-width destination header. Its corner radius decreases, the photograph zooms and lifts, and the destination title grows. The detail sheet rises from below the viewport while its introduction, statistics, trail illustration, and save action appear in staggered phases. The home chrome, coordinates, caption, and arrow fade away as the destination opens.

Animated progress is passed as `State<Float>` and read in layout, graphics layers, and drawing. The bounds transformation accounts for the home scroll offset. Primary open and detail actions wait until the transition settles, and inactive home/detail content clears its accessibility semantics.

Autoplay has a 1,600 ms lead-in, then alternates opening and closing targets at intervals of 3,800 ms and 3,000 ms respectively. Its coroutine runs only while the destination is active and its lifecycle is resumed. Returning to the foreground while autoplay remains selected restarts the demonstration from the collapsed state and repeats the lead-in.

The expanded state uses `rememberSaveable`. The saved selection is shared through `MotionApp` as saveable UI state; autoplay is local to the current composition.

## Source

- Route, screen, hero, details, motion phases, and layout modifiers: `app/src/main/java/com/neniukov/composeenigma/ui/journey/`
- Navigation key: `app/src/main/java/com/neniukov/composeenigma/ui/journey/navigation/JourneyNavKey.kt`
- Palette: `JourneyColors` in `app/src/main/java/com/neniukov/composeenigma/ui/theme/Color.kt`
- Photograph: `app/src/main/res/drawable-nodpi/dolomites.png`
- Text and accessibility labels: `app/src/main/res/values/strings.xml`

## Validation

Run `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease` for the existing app tests, builds, and lint checks. Journey has no dedicated unit tests; its UI is covered by preview definitions and can be checked manually on a running Android target.

`JourneyPreviews.kt` defines collapsed, intermediate, expanded, compact, landscape, and large-text states. The debug-only `MotionFrames.kt` samples both opening and closing transitions for frame-by-frame inspection.

For an interaction check, open the card from a scrolled home view, toggle saving from both views, close with the back arrow and System Back, start and stop autoplay, background and resume the app, and recreate the activity to check restoration. Preview rendering alone does not verify these interactions or GPU performance.
