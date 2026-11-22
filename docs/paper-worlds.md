# Paper Worlds

An interactive pop-up book built from procedural paper geometry and rendered on a Jetpack Compose Canvas.

- **Open the world** opens both covers around their shared spine. Mountains, a paper sun, layered hills and pine trees rise with the pages; a tiny alpine house unfolds beside a lake and jetty.
- Drag horizontally across the book to control its opening directly. Releasing settles to the nearest endpoint. **Fold it away** reverses the same sequence, and another gesture or button press can interrupt it.
- The scene uses perspective projection, two hinged leaves, rotating cutouts, directional shading, clipped shadows, page edges and a woven cover with a foil mountain motif. Each leaf is painted together with its contents, so the foreground cover hides the far page. Inside artwork is revealed by the physical opening rather than a fade. Geometry and drawing paths are cached.
- The house is a rigid facade carrying hinged side walls and roof panels. Panel edges retain their lengths and the roof stays attached to the gable throughout folding. Cutouts lift progressively, with a slower critically damped spring that preserves gesture interruption and reversal.
- Windows and the door are printed details owned by their wall surface. They are clipped and painted immediately after that wall instead of being independently sorted by centroid depth, preventing windows from disappearing or swapping during folding.
- Animation progress is read in the Canvas draw phase. The frame-clock animation runs only while this destination is active and the app is resumed, and requests no frames once settled.
- A saveable, composition-owned motion holder retains the pose and opening target across recreation. Interrupted gestures restore released, toward the nearest endpoint.
- Buttons and named accessibility actions provide alternatives to dragging. The progress rail exposes the book opening to accessibility services. Landscape windows place the controls beside the book; compact windows and large text can scroll.

Source: `app/src/main/java/com/neniukov/composeenigma/ui/paper/`.

Code is split by responsibility:

- `PaperModel.kt` builds the static paper polygons and their printed details.
- `PaperGeometry.kt` defines model-space axes, shared house dimensions and rigid folding transforms.
- `PaperScene.kt` owns projection, shading and surface paint order.
- `PaperMotion.kt` owns the gesture target, saveable pose and lifecycle-bound animation. The UI reads a progress value without access to the mutable `Animatable`.
- `PaperScreen.kt` composes the adaptive layout, controls and accessibility actions.

Validation: `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleRelease`.

The motion tests cover gesture interruption, reversing direction, lifecycle cancellation and resume, idle frame suppression, input bounds and pose restoration. They also verify that a button can interrupt a drag without a late release overriding its target, and that backgrounding a held book releases the gesture before resuming toward the nearest endpoint. Geometry tests verify the shared book hinge, motion of both leaves, rigid panel edges, attached roof and side walls, folded footprints staying inside the pages, early cutout lift and finite geometry throughout opening. Compose previews cover closed, open, compact, landscape and large-text configurations.

Checked on a connected Pixel 9 with a debug build: catalog navigation, opening and closing by button and drag, rapid reversals, portrait and landscape layouts, rotation preserving the open pose, and restoration after a background process kill through the launcher. The revised folding and window fix were recorded and inspected through opening, closing and the fully open state, followed by drag and rapid reversal checks. Opening, drag-to-close and rapid reversals were checked again after the code review and formatting pass. All 55 unit tests, including the 19 Paper Worlds tests, Android Lint, and debug and release builds pass. Lint reports only dependency update notices.
