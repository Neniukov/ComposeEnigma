# Liquid Portal

An interactive Compose study: a liquid window into sunlit dunes, suspended over a cold mountain landscape.

- Hold anywhere in the scene to open the portal; drag to move it. Touch takes control from autoplay.
- Release to close the portal and send a refracting wave through the mountains.
- **Watch it move** starts autoplay; **Pause motion** closes it. The back arrow returns to the collection.
- Android 13+ uses an AGSL RuntimeShader with image refraction, chromatic separation and a spectral rim. Android 9–12L uses a Canvas silhouette, gradient rim and drawn ripple.
- Animation state belongs to the local UI. Fast-changing values are read during drawing. Animation pauses when the destination is inactive or the app is not resumed, and the frame clock stops after the final ripple.
- Image decoding and shader setup run off the UI thread. Previews use the same renderer with immediate resource loading.

## Validation

Run `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug :app:lintRelease`.

The motion tests cover lifecycle restart after completed and interrupted ripples, spring velocity continuity when retargeting, rapid reopening, small normalized movements and frame-clock inactivity after settling. Shader frames and layout previews cover portrait, compact, landscape, large text and the Canvas renderer. GPU performance and real-device pointer input require an Android device or emulator.

## Source

- UI and motion: `app/src/main/java/com/neniukov/composeenigma/ui/portal/`
- Shader: `app/src/main/res/raw/liquid_portal.agsl`
- Palette: `app/src/main/java/com/neniukov/composeenigma/ui/theme/PortalColors.kt`

## Image provenance

The mountain photograph reuses the existing `app/src/main/res/drawable-nodpi/dolomites.png` asset, graded at render time.

The desert texture was generated with the built-in image generation tool and copied into `app/src/main/res/drawable-nodpi/portal_desert.png`.

Final generation prompt:

> Use case: photorealistic-natural. Asset type: full-bleed portrait Android app landscape texture, 2:3 portrait. Primary request: a breathtaking untouched orange sand desert with sculptural dunes at golden hour, seen from a low elevated viewpoint, a sweeping sharp dune crest diagonally crossing the middle and deep terracotta shadow valleys below, distant layered burnt orange dunes fading into pale apricot haze. Composition: horizon at upper third, warm light peach sky in top quarter, rich finely textured dunes fill middle and bottom, striking contrast between glowing saffron sunlight and deep rust red shadows, cinematic editorial landscape photograph, realistic sand ripples. Entire canvas is the photograph. No people, plants, buildings, animals, text, graphic elements, frame, border, portal, circles, UI or watermark. Output a single high-quality photographic background image.

## References

- [AGSL brushes in Compose](https://developer.android.com/develop/ui/compose/graphics/draw/brush#agsl)
- [RuntimeShader](https://developer.android.com/reference/android/graphics/RuntimeShader)
- [Pointer input and gestures](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures)
