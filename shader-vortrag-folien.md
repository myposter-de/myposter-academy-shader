# GPU-Shader — MYPOSTER Academy

**Foliengerüst zur Überführung nach CuP (Compose ur Pres)**
Zieldauer: 19–20 Minuten + Q&A · 18 Slides

---

## Vorbemerkung zur technischen Umsetzung

**Von Desktop-JVM präsentieren, nicht vom Web-Export.**
Compose Desktop rendert über Skia. Damit läuft dein `IMAGE_ADJUST_SHADER` in seiner
SkSL-Lesart direkt in der Präsentation — derselbe String, den du in der App an
`RuntimeShader` gibst. Der CuP-Web-Export (Wasm/CanvasKit) ist für `RuntimeEffect`
deutlich wackeliger; nimm ihn allenfalls als Fallback ohne Live-Demos.

Grundmuster für alle Shader-Demos (API-Namen gegen deine Skiko-Version prüfen):

```kotlin
// commonMain oder jvmMain, je nach Target-Setup
private val effect = RuntimeEffect.makeForShader(IMAGE_ADJUST_SHADER)

@Composable
fun AdjustedImage(adjust: ImageAdjust, modifier: Modifier = Modifier) {
    val u = remember(adjust) { adjust.toImageAdjustUniforms() }
    val renderEffect = remember(u) {
        RuntimeShaderBuilder(effect).apply {
            uniform("brightness", u.brightness)
            uniform("contrastSlope", u.contrastSlope)
            uniform("contrastIntercept", u.contrastIntercept)
            uniform("saturation", u.saturation)
            uniform("vibrance", u.vibrance)
            uniform("sepia", u.sepia)
            uniform("temperature", u.temperature)
            uniform("gamma", u.gammaR, u.gammaG, u.gammaB)
        }.let {
            ImageFilter.makeRuntimeShader(it, IMAGE_ADJUST_SHADER_INPUT, null)
                .asComposeRenderEffect()
        }
    }
    Image(
        painter = painterResource(Res.drawable.demo_photo),
        contentDescription = null,
        modifier = modifier.graphicsLayer { this.renderEffect = renderEffect }
    )
}
```

**Demo-Hygiene:** ein festes Testbild (12 MP, im Repo eingecheckt), `PreparedSlide`
für alles, was laden muss, und jede Demo einmal ohne Publikum auf dem
Präsentationsrechner durchklicken. Ein kalter Shader-Compile beim ersten Frame
kostet ein paar hundert Millisekunden — das sieht aus wie ein Bug, ist aber nur
der Treiber.

---

## Slide 1 — `titel`

**0:00–0:20 · stepCount = 1**

> # Warum dein Bildfilter auf der falschen Hardware läuft
> GPU-Shader, anhand eines echten Umbaus
> Jannis · MYPOSTER Academy

*Notes:* Kurz halten. Nicht vorstellen, was kommt — direkt in die Demo.

---

## Slide 2 — `hookDemo` ⚡ LIVE

**0:20–3:00 · stepCount = 3**

Die wichtigste Folie des Vortrags. Kein Text, nur Bild und Slider.

- **Step 0:** Testbild + ein Helligkeits-Slider, verdrahtet gegen die
  **CPU-Implementierung** (der Byte-Loop, in einen Coroutine-Dispatcher gepackt,
  damit die Präsentation nicht einfriert). Ziehen. Es ruckelt, es hinkt hinterher.
- **Step 1:** Derselbe Slider, verdrahtet gegen den **Shader**. Ziehen. Es klebt
  am Finger.
- **Step 2:** Zahl einblenden — gemessene Zeit pro Frame, CPU vs. GPU, mit
  Bildgröße dazu.

```kotlin
val hookDemo by PreparedSlide(stepCount = 3) {
    val bitmap = remember { loadDemoImage() }   // im prepare-Block, nichts zeichnen!
    slideContent { step ->
        var brightness by remember { mutableStateOf(0) }
        if (step == 0) CpuAdjustedImage(bitmap, brightness)
        else ShaderAdjustedImage(bitmap, brightness)
        Slider(
            value = brightness.toFloat(),
            onValueChange = { brightness = it.toInt() },
            valueRange = -100f..100f
        )
        AnimatedVisibility(visible = step >= 2) { TimingBadge(...) }
    }
}
```

*Notes:* Nichts erklären, während du ziehst. Die Stille wirkt. Erst beim
Zahlen-Step reden. Ehrlich bleiben: Median über N Läufe, nach Warmup, feste
Bildgröße nennen.

---

## Slide 3 — `hookZahlen`

**3:00–3:40 · stepCount = 2**

- **Step 0:** Die Messung als Balken — CPU-ms vs. GPU-ms, Bildgröße im Untertitel.
- **Step 1:** Einblendung: *„Und der Byte-Loop lebt weiter."*

*Notes:* Hier kommt die Wendung, die den Vortrag interessant macht. Der Shader hat
die CPU-Implementierung **nicht ersetzt**. Alle Pfade, die das Ergebnis als Bitmap
zurücklesen statt es zu zeichnen — PhotoImageBitmap, der Photobook-Renderer, die
nativen Renderer — nutzen weiter den Byte-Loop. Wir haben jetzt zwei
Implementierungen, die pixelgenau übereinstimmen müssen. Warum das so ist, kommt
am Ende. Spannungsbogen aufmachen und stehenlassen.

---

## Slide 4 — `agenda`

**3:40–4:00 · stepCount = 1**

> 1. Warum die GPU das kann
> 2. Was ein Shader eigentlich ist
> 3. Der echte Shader, Zeile für Zeile
> 4. Wie man selbst hinkommt
> 5. Dasselbe Prinzip, ganz anderes Feld
> 6. Wann es sich lohnt — und wann nicht

*Notes:* Zehn Sekunden. Nicht vorlesen.

---

## Slide 5 — `warumSchnell`

**4:00–5:30 · stepCount = 4**

Die Größenordnung greifbar machen.

- **Step 1:** 12 MP = 12.000.000 Pixel
- **Step 2:** × 8 Filterschritte × 3 Kanäle ≈ **300 Mio. Rechenoperationen** —
  pro Slider-Bewegung, und ein Slider erzeugt 60 davon pro Sekunde
- **Step 3:** CPU: einige Kerne, seriell, mit SIMD vielleicht 8 Werte gleichzeitig
- **Step 4:** GPU: hunderte bis tausende Recheneinheiten, die nichts anderes tun
  als genau diese Art Arithmetik

*Notes:* Die Rechnung laut vormachen. „Pro Slider-Bewegung" ist der Satz, der die
Zahl vom Benchmark zur Alltagserfahrung macht.

---

## Slide 6 — `simt`

**5:30–7:00 · stepCount = 3**

Das zentrale Modell — und das erste von drei Versprechen, die später eingelöst werden.

- **Step 1:** Die GPU führt Pixel in Gruppen aus (32er oder 64er, je nach
  Hersteller). Alle Threads einer Gruppe führen **dieselbe Instruktion** aus, auf
  verschiedenen Daten. SIMT.
- **Step 2:** Grafik: ein `if`, bei dem die Hälfte der Gruppe in den einen und die
  Hälfte in den anderen Zweig will → die GPU läuft **beide** Zweige, jeweils mit
  der Hälfte deaktiviert. Kosten: beide Zweige, Nutzen: einer.
- **Step 3:** **Versprechen 1:** *Deshalb hat mein Shader kein einziges `if` im
  Pixelpfad.* (Einlösung auf Slide 11)

*Notes:* Der Divergenz-Punkt ist das, was die meisten nicht wissen und was am
ehesten hängenbleibt. Optional als Q&A-Munition: das eine `if` im Shader
(`alpha <= 0`) ist unkritisch, weil es für ein ganzes Bild fast immer einheitlich
ausfällt — Divergenz kostet nur, wenn sie *innerhalb* einer Gruppe auftritt.

---

## Slide 7 — `kostenmodell`

**7:00–7:45 · stepCount = 2**

Versprechen 2 und 3 aufmachen.

- **Step 1:** Was man einmal pro Draw rechnen kann, gehört nicht in den Pixelcode.
  **Versprechen 2:** *Zwei Zeilen Kotlin ersetzen 12 Millionen Divisionen.*
- **Step 2:** Der Shader ist eine eigene Sprache mit eigenen Konventionen.
  **Versprechen 3:** *Eine falsch gelesene Matrix, und das Foto kommt falsch aus
  dem Drucker.*

---

## Slide 8 — `wasIstEinShader`

**7:45–9:00 · stepCount = 3**

Das Kernbild des Vortrags.

- **Step 1:** Ein Fragment-Shader ist eine **reine Funktion**: `Koordinate → Farbe`
- **Step 2:** Du schreibst **nicht** die Schleife. Du schreibst den Schleifenkörper.
  Wann und wie oft er läuft, entscheidet das System.
- **Step 3:** Was daraus folgt: kein Zustand zwischen Pixeln, keine
  Reihenfolge-Annahme, kein Zugriff auf das Nachbarergebnis, kein `printf`.

*Notes:* „Du schreibst den Schleifenkörper und gibst die Kontrolle über das Wann
ab" ist der Satz, den die Leute mitnehmen sollen. Wiederhole ihn.

---

## Slide 9 — `shaderPlayground` ⚡ LIVE

**9:00–10:00 · stepCount = 1**

Ein `TextField` mit editierbarem Shader-Code links, das gerenderte Ergebnis rechts.
Bei jeder Änderung neu kompilieren, Compile-Fehler als roter Text darunter.

Startinhalt bewusst trivial:

```glsl
half4 main(float2 coord) {
    return half4(coord.x / 400.0, coord.y / 400.0, 0.5, 1.0);
}
```

Live eine Zeile ändern — z. B. `sin(coord.x / 20.0)` einbauen — und zeigen, dass
sofort ein Verlauf/Streifenmuster entsteht.

```kotlin
val shaderPlayground by Slide {
    var src by remember { mutableStateOf(STARTER_SHADER) }
    val result = remember(src) { runCatching { RuntimeEffect.makeForShader(src) } }
    Row {
        TextField(src, { src = it }, textStyle = TextStyle(fontFamily = FontFamily.Monospace))
        result.fold(
            onSuccess = { ShaderCanvas(it) },
            onFailure = { Text(it.message ?: "", color = Color.Red) }
        )
    }
}
```

*Notes:* Diese Folie ist der Grund, warum der Vortrag in CuP und nicht in
PowerPoint steckt — das ruhig kurz sagen. Vorher einen Fehler provozieren
(Semikolon löschen), damit klar wird: es ist ein echter Compiler, kein Video.

---

## Slide 10 — `pipeline`

**10:00–10:45 · stepCount = 2**

Einordnung, damit Vertex-Shader nicht als Lücke im Raum stehen bleiben.

- **Step 1:** Vertex-Shader (wo liegt die Geometrie?) → Rasterizer (welche Pixel
  sind bedeckt?) → **Fragment-Shader** (welche Farbe hat dieses Pixel?)
- **Step 2:** Für Bildfilter ist die Geometrie immer dasselbe Rechteck. Deshalb
  brauchst du bei AGSL/SkSL keinen Vertex-Shader zu schreiben — der ist schon da.

---

## Slide 11 — `codeWalkthrough` 🔑

**10:45–14:30 · stepCount = 8**

Der Kern. CuPs `SourceCode`-Komponente mit Section-Highlighting, ein Step pro
markiertem Abschnitt. Code links, Erklärung rechts oder darunter.

| Step | Markierter Abschnitt | Aussage |
|---|---|---|
| 1 | `uniform`-Block | Uniforms sind pro Draw konstant, für alle Millionen Pixel gleich. Der Kanal von CPU nach GPU. |
| 2 | `float3 c = float3(src.rgb) / alpha * 255.0;` | Compose/Skia liefern **premultiplied** Alpha, die Filterformeln brauchen unpremultiplied 0..255. Falsch herum → Halbtransparentes wird dunkler. |
| 3 | `step8()` | Die CPU-Variante rundet und clamped nach **jedem** Schritt. Wer im Float bleibt, bekommt ein *anderes* Bild — nicht nur ein genaueres. |
| 4 | Brightness-Zeile | Kein Offset, sondern eine Belichtungskurve: der Faktor hängt davon ab, wie hell das Pixel schon ist (`1 - summe/765`). Helle Bereiche brennen nicht aus. |
| 5 | `contrastSlope` / `contrastIntercept` + Sprung nach `toImageAdjustUniforms()` | **Versprechen 2 eingelöst.** Kontrast ist mathematisch `(c - 127.5) * s + 127.5`. Ausmultipliziert wird daraus eine Multiplikation und eine Addition — der Rest ist zwei Zeilen Kotlin, einmal pro Draw. |
| 6 | Saturation + Vibrance | Saturation mischt gegen das Luma (Rec.709-Gewichte: Grün zählt siebenmal so viel wie Blau). Vibrance zieht jeden Kanal zum eigenen Maximum, gewichtet mit dem Abstand zum Durchschnitt — d. h. schon bunte Pixel werden kaum angefasst. |
| 7 | Sepia-Matrix | **Versprechen 3 eingelöst.** `float3x3` ist in AGSL/SkSL **column-major**. Die Konstruktor-Argumente stehen deshalb spaltenweise — die Matrix im Code ist die transponierte Lehrbuchmatrix. Wer sie „aufräumt", vertauscht R und B. |
| 8 | `pow(c/255, gamma)` + Rückgabe | Gamma pro Kanal, deshalb ein `float3`-Uniform statt drei Floats. Am Ende wieder premultiplizieren, damit Compose bekommt, was es erwartet. |

*Notes:* Beim Sepia-Step darf man kurz menschlich werden — das ist der Fehler, den
man garantiert einmal macht, und im Print-Kontext heißt „falsche Farbe" nicht
„sieht komisch aus", sondern „Reklamation". Zeitpuffer: falls du hinten dran bist,
Steps 6 zusammenziehen, aber Step 3, 5 und 7 nicht opfern — das sind die drei
Versprechen.

---

## Slide 12 — `identitaet`

**14:30–15:00 · stepCount = 2**

Die Design-Entscheidung hinter `ImageAdjustUniforms`.

- **Step 1:** Jedes Feld ist bei `ImageAdjust`-Wert 0 die **Identität**.
- **Step 2:** Deshalb braucht der Shader keine Verzweigung: ein Filter, der nichts
  tun soll, setzt seine Uniforms neutral und rechnet trotzdem mit. Der
  ungenutzte Schritt kostet ein paar Instruktionen — die Verzweigung hätte mehr
  gekostet.

*Notes:* Schließt den Bogen zu Slide 6. Kleines Detail, das gut ankommt: das
`-0.0`-Problem bei Vibrance — negiert wird der Int-Input, nicht der Float, weil
`-0.0f` sonst die Equality-Prüfung und damit das Caching kaputtmacht.

---

## Slide 13 — `leiter`

**15:00–16:30 · stepCount = 5**

Eine Folie, fünf Steps, von „kein Shader-Code" zu „volle Kontrolle".

- **Step 1 — Deklarative Filter:** CSS `filter`, SVG-Primitives, Canvas
  `filter`-Property, auf Android `ColorMatrixColorFilter`. Nutzen intern die GPU.
  Für Standardfälle oft schon genug.
- **Step 2 — Shader ohne Pipeline:** **AGSL** (`RuntimeShader`, Android 13+),
  **SkSL** (Skia, damit Compose Desktop), CSS Houdini Paint Worklets. Du schreibst
  nur `main()`. Kein Kontext, kein Vertex-Shader, keine Buffer.
  → **Hier läuft mein Code. Und hier läuft diese Präsentation.**
- **Step 3 — Abstraktion über der Pipeline:** PixiJS, regl, Three.js, glfx.js.
- **Step 4 — Volle Kontrolle:** WebGL / WebGPU, OpenGL / Metal / Vulkan.
- **Step 5 — Querschnitt:** Compute-Shader, wenn das Ergebnis gar nicht auf den
  Bildschirm soll.

*Notes:* Stufe 2 ist die eigentliche Nachricht dieser Folie: Der Einstieg kostet
heute eine Funktion, nicht eine Grafik-Pipeline. Das war vor fünf Jahren anders.

---

## Slide 14 — `computeUndKi` 🌉

**16:30–17:45 · stepCount = 5**

Der Brückenschlag: Das Prinzip ist nicht an Grafik gebunden — Grafik war nur der
erste Kunde.

- **Step 1 — Die Pipeline weglassen:** Ein **Compute-Shader** ist ein Shader ohne
  Rasterizer und ohne Framebuffer. Übrig bleibt genau der Kern: eine Funktion, die
  für hunderttausende unabhängige Datenpunkte parallel läuft. Nur das Ergebnis
  geht in einen Buffer statt in ein Bild. (CUDA, Vulkan/Metal Compute, WebGPU.)
- **Step 2 — Die Geschichte:** Vor CUDA haben Forscher ihre Daten als **Textur**
  verpackt, durch einen Fragment-Shader geschickt und das Bild wieder ausgelesen,
  nur um die Rechenleistung zu bekommen. „GPGPU". Die Hardware war für Pixel
  gebaut, die Mathematik war es nie.
- **Step 3 — Warum ausgerechnet neuronale Netze:** Ein Layer ist im Kern eine
  **Matrixmultiplikation**. Millionen unabhängige Multiplikationen und
  Additionen, identische Operation, keine Verzweigung auf Datenbasis.
  → *Das ist dieselbe Form wie mein Bildfilter.*
- **Step 4 — Der Rückbezug, Teil 1:** Bei LLM-Inferenz ist der Engpass nicht die
  Rechenleistung, sondern die **Speicherbandbreite** — die Gewichte müssen durch
  den Chip. Exakt das Argument von Slide 5: mein Filter ist auch nicht
  rechenlimitiert, sondern limitiert durch das Lesen und Schreiben des Bildes.
- **Step 5 — Der Rückbezug, Teil 2:** Und die Frage, die mich bei `step8()`
  beschäftigt hat — wie viele Bits brauche ich wirklich? — heißt in der KI-Welt
  **Quantisierung**: fp32 → fp16 → int8 → int4. Dieselbe Abwägung: weniger
  Präzision, mehr Durchsatz, und irgendwo eine Grenze, ab der das Ergebnis nicht
  mehr stimmt.

*Notes:* Kernsatz für diese Folie: **„Die GPU ist keine Grafikkarte, die man für
KI zweckentfremdet hat. Sie ist eine Durchsatzmaschine, und Grafik war ihre erste
Anwendung."** Nicht in Details zu Tensor Cores abbiegen — das ist ein eigener
Vortrag und kostet dich hier die Zeit. Falls jemand nachhakt, siehe Backup-Slide.
Wenn du über der Zeit bist: Step 2 (Geschichte) ist der entbehrlichste, die
beiden Rückbezüge sind der Grund, warum die Folie existiert.

---

## Slide 15 — `preis`

**17:45–18:45 · stepCount = 4**

Ehrliche Kostenseite. Macht den Vortrag glaubwürdiger als eine reine Erfolgsstory.

- **Step 1 — Transfer:** Lohnt sich nur, wenn das Bild ohnehin auf der GPU liegt
  *und* dort bleibt. Muss das Ergebnis zurück in den RAM, frisst der Readback den
  Gewinn. **Das ist der Grund, warum unser Byte-Loop weiterlebt.** (Auflösung des
  Spannungsbogens von Slide 3.)
- **Step 2 — Zwei Implementierungen, eine Wahrheit:** Eine billigere Umformulierung
  eines Schritts ändert, was der Kunde gedruckt bekommt. Absicherung:
  Pixelvergleichs-Tests zwischen beiden Pfaden.
- **Step 3 — Debugging:** Kein Breakpoint, kein Log. Man debuggt, indem man
  Zwischenergebnisse als Farbe ausgibt.
- **Step 4 — Fragmentierung:** AGSL braucht Android 13+, Präzision (`half` vs.
  `float`) verhält sich zwischen Geräten nicht identisch, Compile-Zeit beim
  ersten Frame.

---

## Slide 16 — `wannLohntEs`

**18:45–19:15 · stepCount = 3**

Die Checkliste zum Mitnehmen — alle drei müssen zutreffen.

- **Step 1:** Dieselbe simple Rechnung auf **sehr vielen unabhängigen** Daten
- **Step 2:** Das Ergebnis soll **sowieso auf den Bildschirm**
- **Step 3:** Es passiert **interaktiv oder oft**, nicht einmal im Batch

---

## Slide 17 — `takeaway`

**19:15–19:35 · stepCount = 2**

- **Step 0:**
> **Wenn dieselbe simple Rechnung millionenfach auf unabhängigen Daten läuft und
> das Ergebnis sowieso auf den Bildschirm soll, ist die GPU nicht der exotische,
> sondern der naheliegende Ort.**
>
> Und der Einstieg kostet heute eine Funktion, nicht eine Grafik-Pipeline.

- **Step 1:**
> **Und wenn das Ergebnis gar nicht auf den Bildschirm soll, heißt das Werkzeug
> Compute statt Fragment — das Denkmuster bleibt dasselbe.**

---

## Slide 18 — `fragen`

**stepCount = 1**

> Fragen?
>
> Shader, Slides und Playground: *(Repo-Link)*
> Diese Präsentation ist eine Compose-App — CuP, github.com/KodeinKoders/CuP

---

## Backup-Slides (nach `fragen`, für Q&A)

| Slide | Inhalt |
|---|---|
| `backupMessmethodik` | Gerät, Bildgröße, Warmup, Anzahl Läufe, Median vs. Mittelwert, was *nicht* gemessen wurde |
| `backupDivergenz` | Warum das eine `if` im Shader unkritisch ist |
| `backupPraezision` | `half` vs. `float`, warum `float3` statt `half3` für die 0..255-Domäne |
| `backupCompute` | Compute-Shader im Detail: Shared Memory, Workgroups, warum Blur davon profitiert |
| `backupTbdr` | Warum Mobile-GPUs anders sind als Desktop-GPUs (Tiling, Bandbreite) |
| `backupTensorCores` | Tensor Cores / Matrix-Einheiten, warum eine H100 keine „große Grafikkarte" mehr ist |
| `backupNpu` | NPUs auf Handys — wann die statt der GPU die richtige Einheit sind |
