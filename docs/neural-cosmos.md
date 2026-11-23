# Neural Cosmos

An AI-inspired motion study: a luminous constellation rendered with Kotlin and Jetpack Compose Canvas.

## Interaction

- Drag in either direction to turn the constellation. Damped rotation keeps the movement fluid after release.
- Tap the scene to send a wave from a nearby projected node, preferring the front hemisphere. **Send a signal** chooses another source and restarts the wave.
- The header control pauses both the ambient orbit and the current signal. Dragging still works while paused; sending another signal resumes playback.
- Named accessibility actions rotate the constellation and send signals without dragging.

## Geometry and drawing

`CosmosGeometry.kt` distributes 144 nodes on a Fibonacci sphere. Each node connects to four nearest neighbours; duplicate undirected edges are removed. The resulting graph is connected. Three inclined circular orbits surround a compact core of two closed filaments.

The resting sphere draws every connection in the graph, keeping all neighbouring nodes linked. Front-facing nodes and links keep full visibility; centre attenuation increases smoothly with depth in the rear hemisphere. Signals brighten the existing connections as the wave reaches them. The smaller amber core is separated from the cool constellation by its size, colour and the surrounding space.

The renderer applies yaw and pitch before perspective projection. Paths split into front and back sections, placing the luminous core between the hemispheres. Node brightness and size convey depth. A signal travels across chord distance from its source and highlights the web's nodes and connections. Its propagation is visible through the network illumination, without a separate wave contour.

Geometry, drawing paths and projection buffers are owned by the renderer. The catalog thumbnail renders a fixed pose without a frame loop. Theme colors live in `CosmosColors.kt`.

## State and lifecycle

`CosmosMotion.kt` owns the camera, phase, playback flag and signal. Frame-rate properties are read during drawing; the status label observes only whether a signal is active. A composition-scoped frame clock runs while this destination is active and its lifecycle is resumed. A paused, settled scene requests no frames.

The saver restores the camera pose, ambient phase, playback mode, source node and signal progress. A held gesture is released on cancellation or restoration. Resuming starts a fresh frame-time baseline, and time steps are capped at 32 milliseconds to avoid jumping after a stall.

The screen adapts to the available width and height. Landscape places the controls beside the scene; compact windows and larger text can scroll.

## Validation

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleRelease
```

Geometry tests cover graph connectivity and uniqueness, rigid rotation, perspective, finite orbital paths, picking a rotated front node and signal propagation. Motion tests cover pause, rotation while paused, idle frame suppression, repeated signals, invalid input, pitch bounds, lifecycle cancellation, restoration and long frame gaps.

Checked on a connected Pixel 9: portrait, landscape, 1.7× text, scrolling to controls, rotation while paused, scene taps, the signal button, pause/resume and restoring the paused destination after a background process kill. Device settings are restored after the checks.
