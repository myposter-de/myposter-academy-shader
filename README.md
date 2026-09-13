# GPU-Shader — MYPOSTER Academy

The talk *„Warum dein Bildfilter auf der falschen Hardware läuft"*, built as a
Compose Desktop application with [CuP (Compose ur Pres)](https://github.com/KodeinKoders/CuP).

The presentation is not a slide export. It is an app, so the two live demos —
the CPU/GPU slider on slide 2 and the shader playground on slide 9 — run the
real `RuntimeShader` code the talk is about.

## Run it

```
./kotlin run -m desktopApp
```

To present with a real photo instead of the generated test image:

```
./kotlin run -m desktopApp -- -Dshadertalk.photo=/path/to/photo.jpg
```

## Keys

| Key | Action |
|---|---|
| → ↓ space enter | one step forward |
| ← ↑ backspace | one step back |
| shift + arrow | one whole slide |
| `S` | speaker window with the notes |
| `F` | full screen |
| `esc` | slide overview |

## Layout

- `shared/src/slides/` — the 18 slides and the Q&A backup slides
- `shared/src/shader/` — the SkSL filter, its CPU twin, the test image, the timings
- `shared/src/theme/` — palette and layout widgets
- `shared/test/` — the shader compiles, and both paths agree

The content follows `shader-vortrag-folien.md`.
Background material for the Q&A is in `shader-hintergrund-briefing.md`.

See `CLAUDE.md` for the build system, the pinned JDK and the CuP pitfalls.
