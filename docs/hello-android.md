# Hello, Android

A green Android robot built entirely from procedural geometry and rendered in Jetpack Compose Canvas.

- Drag to orbit the robot horizontally and tilt it vertically. Spring smoothing preserves momentum when the target changes.
- **Say hello** raises an articulated arm and waves. **Do a flip** performs a complete airborne somersault. Trick buttons become available again after landing.
- The header control pauses or resumes idle motion and tricks. The model remains draggable while paused.
- The robot breathes, bobs, blinks and gently turns its head. Separate joints control the head, arms and legs.
- Geometry and vertex buffers are cached. Each frame projects the mesh with perspective, culls backfaces, sorts visible faces by depth and applies smooth vertex lighting with a specular highlight. The character uses one mesh draw call. Contact shadows and orbital rings anchor the floating character.
- Frame-rate state is read in the Canvas draw phase. Only the trick buttons observe a derived ready state. The clock stops when the destination is inactive or the app leaves the resumed lifecycle; a paused, settled scene requests no frames.
- The screen supports portrait, compact, landscape and large text layouts, and exposes accessible rotation actions alongside the visible buttons.

Source: `app/src/main/java/com/neniukov/composeenigma/ui/droid/`.

Validation: `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.

The motion tests cover pausing mid-flip, turning while paused, spring settling, landing upright, rapid repeated commands and tilt limits. Compose previews cover four layout configurations. The screen, orbit gesture, wave, flip and pause/resume were also checked on a connected Pixel 9 using an R8-optimized release build.

The Android robot is reproduced from the work created and shared by Google and used according to terms described in the [Creative Commons 3.0 Attribution License](https://creativecommons.org/licenses/by/3.0/). See [Android brand guidelines](https://developer.android.com/distribute/marketing-tools/brand-guidelines).
