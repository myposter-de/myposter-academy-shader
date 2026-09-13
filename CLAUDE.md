# myposter-academy-shader

A conference talk about GPU shaders, built as a Compose Desktop app with
**CuP (Compose ur Pres)** — https://github.com/KodeinKoders/CuP

Target: JVM desktop only. No Android, no web.

## Build system

This project uses the **Kotlin Toolchain (Amper)**, not Gradle.

- `./kotlin build` — compile everything
- `./kotlin test` — run the tests
- `./kotlin run -m desktopApp` — start the presentation
- `./kotlin show settings -m shared` — print the effective, resolved settings

Configuration lives in `project.yaml`, `libs.versions.toml` and the two
`module.yaml` files. There is no `build.gradle.kts`.

## Modules

- `shared` — `kmp/lib`, platform `jvm`. All slides, the shader and the theme.
- `desktopApp` — `jvm/app`. Only `main.kt`, which calls `cupApplication`.

## Pinned JDK 21

Both modules pin `jvm.release: 21` and `jvm.jdk.version: 21`.

CuP colors source code with highlight.js on GraalJS. GraalJS calls
`sun.misc.Unsafe.ensureClassInitialized`, which JDK 25 removed. On JDK 25 the
`SourceCode` widget throws at runtime. Do not raise the JDK version.

## Source layout

```
shared/src/
  Presentation.kt        Slides list + the Presentation composable
  theme/                 Palette, typography, layout widgets
  shader/                The shader, the CPU twin, the demo image, timings
  slides/                One file per section of the talk
shared/test/             Shader compile tests and CPU/GPU parity tests
```

Slide order and content follow `shader-vortrag-folien.md`.
Background facts for the Q&A are in `shader-hintergrund-briefing.md`.
Both files are the source of truth. Change them first, then the code.

## CuP rules that bite

- A slide is a property delegate: `val myName by Slide { }`. The property name
  becomes the slide name and the web URL. Do not rename a slide casually.
- Steps run from `0` to `stepCount - 1`. Always compare with `>=`, never `==`.
- `SlideSpecs` does **not** merge with the defaults. A `defaultSlideSpecs` must
  set `size`, `startTransitions` and `endTransitions`, or CuP throws
  `IllegalStateException: Unspecified` on the first frame.
- Anything that loads or parses belongs in the prepare block of a
  `PreparedSlide`. Draw nothing there — return the content from `slideContent`.
- `rememberSourceCode` must run in a prepare block. Its marked sections must
  either stay inside one line or cover whole lines, indentation included.
- Speaker notes are `SpeakerNotes("markdown")` in the slide `context`.
  The docs name `SpeakerNotesMD`. That class does not exist in Beta-17.
- Backup slides carry `Export.none()`, so they stay out of the PDF export.

## The shader

`shader/ImageAdjustShader.kt` holds `IMAGE_ADJUST_SHADER`, one SkSL string.
`shader/CpuImageAdjust.kt` holds the same math as a byte loop.

The two must agree pixel for pixel. That is the point of the talk, and
`shared/test/ShaderTest.kt` guards it. If you change one, change the other.

Three rules the shader follows on purpose:

1. No `if` in the pixel path. A GPU runs both branches of a divergent `if`.
2. Everything constant per draw is a uniform, computed in
   `toImageAdjustUniforms()`.
3. `float3x3` is column-major. The constructor arguments are the transpose of
   the textbook matrix. Do not "tidy" the sepia matrix.

Every field of `ImageAdjustUniforms` is the identity when the matching
`ImageAdjust` field is `0`. That is why the shader needs no branch.

## Demo image

`loadDemoImage()` generates a 4000 × 3000 test image, so the repository holds no
binary assets. To present with a real photo, start with:

```
-Dshadertalk.photo=/path/to/photo.jpg
```

## Running the app

The app opens a window and keeps state in a `.cup` directory: the last slide and
the window position. Delete `.cup` to start from slide 1.

Keys: arrows or space to step, `S` for the speaker window, `F` for full screen,
`esc` for the overview.
