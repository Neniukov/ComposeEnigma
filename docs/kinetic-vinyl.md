# Kinetic Vinyl

A tactile Compose motion study: a coral paper sleeve, a grooved black record, and a cream turntable with a silver tonearm. Open **Kinetic Vinyl** from the collection to watch the record leave its sleeve and settle onto the platter.

- Hold the record to stop it. Drag around its edge and release to impart momentum in either direction.
- **Pause rotation** lifts the needle and lets the record coast to rest. **Spin the record** gently restores the selected speed.
- Select **33⅓** or **45** to change the motor speed without resetting the angle.
- The eject button lifts the tonearm before returning the record to its sleeve. **Drop the needle** loads it again.
- TalkBack exposes playback, eject, speed selection, and clockwise/counterclockwise spin actions.

This is a visual interaction study; it does not play audio. All artwork is drawn with Compose, with no external images, fonts, or rendering dependencies.

## Motion and rendering

`VinylMotion` owns ephemeral UI mechanics. Placement and tonearm animations have separate, cancellable owners so pausing during extraction does not restart the record's path. The tonearm waits for seating, and ejection waits for the tonearm to return to rest.

The holder uses `rememberSaveable` with a custom `Saver` to retain playback selection, speed, record angle, and the current placement and tonearm pose across activity or process recreation. Only booleans, strings, and floats are saved. Held gestures and angular momentum are cleared on restoration; interrupted placement and tonearm motion continue toward their saved targets.

The rotor integrates exponential acceleration and drag analytically. Gesture angles unwrap across the ±180° boundary; release velocity is measured in degrees per second and bounded before entering the simulation. A center dead zone prevents unstable angles near the spindle. The motor restores its target velocity after a scratch; when paused, momentum decays to an exact stop.

Frame-rate state is read in drawing and graphics layers. Static artwork and measured label text are retained independently of rotation. Animation runs only while the destination is active and its lifecycle is resumed. Leaving the foreground cancels held gestures and momentum without resetting the angle or replaying extraction. The rotor releases its frame clock once it settles.

The scene and catalog cover use a local left-to-right coordinate system so the record, platter, sleeve, and tonearm stay aligned in right-to-left interfaces. RPM labels retain their numeric order while the surrounding screen follows the interface direction.

## Source

- Screen, artwork, gestures, and motion: `app/src/main/java/com/neniukov/composeenigma/ui/vinyl/`
- Palette: `app/src/main/java/com/neniukov/composeenigma/ui/theme/VinylColors.kt`
- Tests: `app/src/test/java/com/neniukov/composeenigma/ui/vinyl/`

## Validation

Run `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease`.

Tests cover refresh-rate independence, angle wrapping, reverse momentum, motor speed changes, touch interruption, extraction and ejection ordering, rapid retargeting, lifecycle cancellation, invalid velocity input, an idle frame clock after settling, and save/restore of paused, playing, sleeved, held, and interrupted states. Previews cover portrait, compact, landscape, large text, right-to-left, sleeved, and mid-extraction states. The debug-only `VinylDirectionPreviews.kt` provides paired left-to-right and right-to-left renders of the scene and cover for visual comparison. Physical gesture feel and GPU performance require a running Android target.
