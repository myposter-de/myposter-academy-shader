# GPU & Shader — Hintergrund-Briefing

Nicht für die Folien. Für dich, damit du in der Diskussion Boden unter den Füßen hast.

---

## 1. Warum die GPU schnell ist — das ehrliche Bild

### Nicht „mehr Kerne", sondern ein anderer Deal

Eine CPU ist darauf optimiert, **einen** Befehlsstrom so schnell wie möglich
abzuarbeiten. Der größte Teil ihrer Chipfläche geht nicht in Rechenwerke, sondern
in die Maschinerie drumherum: Sprungvorhersage, Out-of-Order-Execution,
Register-Renaming, große Caches. Das alles existiert, um **Latenz zu verstecken** —
damit der eine Strom nie stillsteht.

Eine GPU macht den umgekehrten Deal. Sie verzichtet auf fast die gesamte
Latenz-Maschinerie und steckt die Fläche in Rechenwerke. Latenz versteckt sie
nicht durch Cleverness, sondern durch **Überzeichnung**: es sind so viele Threads
gleichzeitig in Flight, dass immer genug rechenbereite da sind, während andere auf
Speicher warten. Wartet eine Thread-Gruppe auf eine Textur, wird in einem Takt auf
eine andere Gruppe umgeschaltet — kein Context-Switch-Overhead, weil die Register
aller Gruppen gleichzeitig vorgehalten werden.

Konsequenz, die man oft falsch erzählt bekommt: Eine GPU ist **nicht** pro
Operation schneller als eine CPU. Sie ist in der Latenz einzelner Operationen
eher schlechter. Sie gewinnt ausschließlich über Durchsatz und nur, wenn du ihr
genug unabhängige Arbeit gibst.

### SIMT und Divergenz

Threads laufen in festen Gruppen: **Warp** (NVIDIA, 32 Threads), **Wavefront**
(AMD, 32 oder 64), **Subgroup/Quad** (mobil, oft 4er-Quads innerhalb größerer
Gruppen). Alle Threads einer Gruppe teilen sich **einen** Program Counter. Sie
führen dieselbe Instruktion aus, auf unterschiedlichen Daten — Single Instruction,
Multiple Threads.

Daraus folgt das Divergenz-Problem: Wenn innerhalb einer Gruppe ein `if`
unterschiedlich ausfällt, kann die Hardware nicht zwei Programme gleichzeitig
laufen lassen. Sie läuft **beide Zweige nacheinander** und maskiert jeweils die
Threads, die nicht dazugehören. Kosten: Summe beider Zweige. Nutzen: einer.

Wichtige Differenzierung, die du in der Diskussion brauchst:

- Divergenz kostet nur **innerhalb** einer Gruppe. Ein `if`, das für das ganze
  Bild gleich ausfällt („ist dieses Uniform gesetzt?"), ist praktisch gratis —
  moderne Treiber erkennen das teilweise sogar und kompilieren Varianten vor.
- Der teure Fall ist ein `if` auf **Pixeldaten** („ist dieses Pixel heller als X?"),
  weil das innerhalb einer 32er-Gruppe beliebig gemischt ausfällt.
- Das `if (alpha <= 0.0)` in deinem Shader ist deshalb unkritisch: bei einem Foto
  ohne Transparenz ist es für jede Gruppe einheitlich false.
- Die verbreitete Faustregel „branchless ist immer besser" stimmt nicht. Ein
  Branch, der einen wirklich teuren Block überspringt (Schleife, viele
  Textur-Samples), lohnt sich auch bei Divergenz. Bei deinen Filterschritten —
  eine Handvoll Arithmetik — lohnt er sich nicht.

### Die eigentliche Grenze: Bandbreite, nicht Rechenleistung

Bei Bildfiltern bist du fast nie rechenlimitiert, sondern **bandbreitenlimitiert**.
Ein 12-MP-RGBA8-Bild sind ~48 MB. Einmal lesen, einmal schreiben. Selbst bei
100 GB/s Bandbreite ist das die Untergrenze deiner Frame-Zeit, egal wie simpel
oder komplex die Arithmetik dazwischen ist.

Das erklärt einen zunächst überraschenden Punkt: **dein achtstufiger Filter kostet
kaum mehr als ein einstufiger.** Die Kosten liegen im Lesen und Schreiben, nicht
in den acht Rechenschritten. Und es erklärt, warum das Zusammenfassen mehrerer
Filter in *einen* Shader-Pass (statt acht Pässe hintereinander) der mit Abstand
größte Gewinn ist — genau das machst du.

Nützliche Begriffe dazu: **arithmetische Intensität** (Rechenoperationen pro
geladenem Byte) und das **Roofline-Modell** (Performance = min(Rechenleistung,
Bandbreite × arithmetische Intensität)). Falls jemand tiefer fragt, ist das die
richtige Vokabel.

### Mobile ist anders: TBDR

Desktop-GPUs rendern „immediate mode" in einen großen Framebuffer im VRAM.
Mobile-GPUs (Mali, Adreno, PowerVR, Apple) rendern **tile-based**: der Bildschirm
wird in kleine Kacheln (z. B. 16×16 oder 32×32) zerlegt, jede Kachel wird komplett
in extrem schnellem On-Chip-Speicher gerendert und erst am Ende einmal in den RAM
geschrieben. Grund: Bandbreite kostet auf einem Handy Akku, und Akku ist das
eigentliche Budget.

Für dich praktisch relevant:

- Mehrere Filter-Pässe sind auf Mobile **überproportional teuer**, weil jeder Pass
  die Kacheln in den RAM zurückschreibt und wieder lädt. Ein-Pass-Shader ist hier
  nicht Feinschliff, sondern der Unterschied.
- Mobile-GPUs teilen sich den Speicher mit der CPU (unified memory). Das macht
  Uploads billiger als auf dem Desktop, aber Readbacks trotzdem teuer, weil
  Synchronisation nötig ist.
- „Es läuft auf meinem Pixel flüssig" sagt wenig über ein drei Jahre altes
  Mittelklassegerät. Adreno und Mali unterscheiden sich in der Präzision von
  `half` und in Compiler-Optimierungen spürbar.

---

## 2. Die Pipeline — was um deinen Shader herum passiert

```
Vertex-Daten
    ↓  Vertex-Shader        ← pro Eckpunkt: wo landet das im Bild?
Primitive (Dreiecke)
    ↓  Rasterizer           ← Festfunktion: welche Pixel deckt das Dreieck?
Fragmente
    ↓  Fragment-Shader      ← pro Pixel: welche Farbe?   ← HIER
Farbwerte
    ↓  Blending / Tiefentest ← Festfunktion
Framebuffer
```

Für Bildfilter ist die Geometrie immer ein bildschirmfüllendes Rechteck aus zwei
Dreiecken. Deshalb können AGSL, SkSL und CSS-Paint-Worklets dich den Vertex-Teil
komplett überspringen lassen: der ist konstant und schon eingebaut.

**Fragment vs. Pixel:** Ein Fragment ist ein Kandidat für ein Pixel. Es kann noch
verworfen werden (Tiefentest, Blending). Bei Bildfiltern fallen die Begriffe
zusammen, aber die Unterscheidung ist der Grund, warum die Sprachen „Fragment-Shader"
heißen.

**Quads (2×2):** Der Rasterizer erzeugt Fragmente immer in 2×2-Blöcken, auch am
Rand eines Dreiecks, wo nur eines davon sichtbar ist. Die unsichtbaren laufen mit
und werden verworfen („Helper Invocations"). Grund: Nur so lassen sich Ableitungen
(`dFdx`/`dFdy`) berechnen, die die Textur-Hardware für die Mipmap-Auswahl braucht.
Nebeneffekt: sehr kleine Dreiecke sind unverhältnismäßig teuer. Für dich irrelevant,
aber gute Antwort, falls jemand nach 3D fragt.

**Die anderen Shader-Typen**, falls gefragt wird: Vertex, Fragment, Geometry
(selten, oft langsam), Tessellation Control/Evaluation, **Compute** (dazu unten),
und in modernen Pipelines Mesh/Task- sowie Ray-Tracing-Shader.

**Compute-Shader** sind der wichtige vierte Typ für dein Thema: Sie laufen
außerhalb der Rasterisierungs-Pipeline, schreiben in beliebige Buffer statt in
einen Framebuffer und haben Zugriff auf geteilten Gruppen-Speicher. Für
Bildverarbeitung, deren Ergebnis **nicht** direkt gezeichnet werden soll, sind sie
der richtige Weg — und für Filter mit Nachbarschaftszugriff (Blur, Convolution),
weil sich Threads über den Shared Memory Ladearbeit teilen können. AGSL und SkSL
können das nicht; dafür bräuchtest du Vulkan, Metal, WebGPU oder RenderScript-Nachfolger.

---

## 3. Die Sprachen

| Sprache | Umfeld | Anmerkung |
|---|---|---|
| **GLSL** | OpenGL, WebGL, Vulkan (via SPIR-V) | Der Klassiker, C-ähnlich |
| **HLSL** | DirectX | Dominiert in der Spieleindustrie |
| **MSL** | Metal (Apple) | C++-basiert |
| **WGSL** | WebGPU | Rust-inspirierte Syntax, neu |
| **SkSL** | Skia | Skias eigene Sprache, GLSL-nah |
| **AGSL** | Android 13+ (`RuntimeShader`) | Praktisch SkSL mit Android-Anstrich |
| **SPIR-V** | Vulkan | Kein Quelltext, Zwischenformat/Bytecode |

**AGSL und SkSL sind fast dasselbe.** Beide sind Skias Shader-Sprache, AGSL ist
die für Android dokumentierte Teilmenge. Unterschiede liegen in den Details
(einige Skia-interne Funktionen sind in AGSL nicht freigegeben, Uniform-Handling
unterscheidet sich leicht). Dass dein Shader-String ohne Änderung in beiden
Dialekten gültig ist, ist deshalb realistisch — aber bewusst so geschrieben, nicht
selbstverständlich. Genau das erlaubt dir, ihn in der CuP-Präsentation
(Compose Desktop → Skia → SkSL) laufen zu lassen.

**Typische Fallstricke für Leute, die aus C/Kotlin kommen:**

- `float3x3` und `mat3` sind **column-major**. `float3x3(a,b,c, d,e,f, g,h,i)`
  füllt Spalten, nicht Zeilen. Genau darum steht deine Sepia-Matrix transponiert
  im Code. Das ist der Klassiker unter den stillen Farbfehlern.
- Vektor-Swizzling: `c.rgb`, `c.xy`, `c.bgra` — geht auch beim Schreiben.
- **Keine** Rekursion, keine Funktionszeiger, keine dynamische Allokation. Loops
  brauchen in älteren Versionen konstante Grenzen.
- Integer-Operationen sind auf manchen mobilen GPUs deutlich langsamer als Floats.
- `pow(x, y)` ist für negative `x` undefiniert. In deinem Shader unkritisch, weil
  `c/255.0` nach `step8()` garantiert in 0..1 liegt — aber genau der Grund, warum
  das `clamp` in `step8()` nicht nur Kosmetik ist.
- Es gibt kein `printf`. Debugging heißt: Zwischenwert als Farbe zurückgeben,
  hingucken, weitermachen.

**Präzision:** `half`/`mediump` sind 16-Bit-Floats mit etwa 3 Dezimalstellen
Genauigkeit. Für Farben, die am Ende auf 8 Bit landen, reicht das meistens — aber
eben nicht, wenn du in einer 0..255-Domäne rechnest und exakt runden musst.
16-Bit-Float kann nicht alle Ganzzahlen bis 255 exakt darstellen, sobald
Zwischenergebnisse größer werden. Dass dein Shader sofort auf `float3` hochzieht
und erst am Ende wieder auf `half` geht, ist deshalb keine Nachlässigkeit,
sondern Voraussetzung dafür, dass die Ergebnisse mit dem Byte-Loop übereinstimmen.
Auf Desktop-GPUs wird `half` oft ohnehin als `float` gerechnet — das ist der
Grund, warum solche Fehler erst auf einem Mittelklasse-Android auffallen.

---

## 4. Farbe — der Teil, an dem die meisten scheitern

### Premultiplied Alpha

Skia, Compose und die meisten Rendering-Systeme speichern Farben
**premultipliziert**: Ein halbtransparentes Rot ist nicht `(1, 0, 0, 0.5)`, sondern
`(0.5, 0, 0, 0.5)`. Grund: Alpha-Blending wird dadurch zu einer einzigen
Multiplikation statt zweier, und lineare Interpolation zwischen Pixeln liefert
korrekte Ergebnisse statt dunkler Ränder.

Filterformeln aus Lehrbüchern und aus CPU-Byte-Loops rechnen dagegen fast immer
**unpremultipliziert**. Wer das nicht umrechnet, bekommt Effekte, die mit der
Transparenz skalieren — der klassische „warum wird mein halbtransparenter Bereich
beim Aufhellen komisch" Bug. Genau deshalb teilt dein Shader eingangs durch Alpha
und multipliziert am Ende wieder.

**Der Sonderfall Alpha = 0:** Dann ist die Farbe nicht wiederherstellbar (0/0).
Das ist der eigentliche Grund für den Early Return in deinem Shader — nicht
Performance, sondern Korrektheit.

### Gamma und Linearität

sRGB-Werte sind **nicht linear** zur Lichtmenge. Der Wert 128 ist nicht „halb so
hell" wie 255, sondern etwa 21 %. Die Kurve ist grob `x^2.2`.

Daraus folgt: Fast alle klassischen Bildfilter — auch deiner — rechnen in
**Gamma-kodiertem Raum**, obwohl physikalisch korrekt der lineare Raum wäre.
Physikalisch korrekt sähe anders aus, und zwar nicht unbedingt besser: Photoshop,
Instagram und jede Kamera-App machen es traditionell auch so, und die Erwartung
der Kunden ist an diesem Look kalibriert.

**Merksatz für die Diskussion:** Die Frage ist nicht „linear oder nicht", sondern
„stimmt es mit dem überein, was der Kunde vorher gesehen hat". In eurem Fall mit
Print-Output ist das keine akademische Frage.

Der `gamma`-Uniform in deinem Shader ist übrigens etwas anderes als die
sRGB-Transferfunktion: Das ist ein pro-Kanal-Exponent als Bedienelement
(Farbstich korrigieren), nicht die Farbraum-Konvertierung.

### Die Luma-Koeffizienten

`dot(c, float3(0.2126, 0.7152, 0.0722))` ist **Rec. 709** — die Gewichte, mit
denen das menschliche Auge die Kanäle zur wahrgenommenen Helligkeit verrechnet.
Grün zählt siebenmal so viel wie Blau. Die ältere Variante ist
Rec. 601 (0.299 / 0.587 / 0.114), die in vielen alten Code-Basen steckt. Dass du
709 verwendest, ist die richtige Wahl für sRGB-Material — aber prüfbar
relevant: wenn CPU- und GPU-Pfad unterschiedliche Koeffizienten hätten, wäre der
Unterschied bei starker Entsättigung sofort sichtbar.

Pedantisch korrekt wäre der Begriff **Luma** (aus gamma-kodierten Werten) im
Gegensatz zu **Luminanz** (aus linearen). Falls jemand im Raum Farbwissenschaft
kann, punktest du damit.

### Quantisierung als Feature

Der interessanteste Punkt an deinem Shader: `step8()` ist **absichtlich** eine
Verschlechterung. Der Byte-Loop rundet nach jedem Schritt auf Ganzzahlen und
clamped auf 0..255. Diese wiederholte Quantisierung ist Teil des Looks — sie
verändert das Ergebnis gegenüber einer durchgehenden Float-Rechnung, nicht nur
dessen Genauigkeit.

Zwei Effekte, die dabei entstehen und die ein reiner Float-Pfad nicht hätte:

1. **Clamping ist nicht umkehrbar.** Ein Pixel, das nach dem Helligkeitsschritt
   auf 255 gedeckelt wurde, kommt durch anschließende Kontrastreduktion nicht
   zurück. Die Reihenfolge der Filter ist deshalb semantisch relevant.
2. **Rundungsfehler akkumulieren** auf eine reproduzierbare Weise, die den
   Endwert um 1–2 Stufen verschieben kann.

Das ist der eigentliche Grund für den Satz in deinem Doc-Kommentar, dass eine
„billigere Reformulierung" ändert, was der Kunde bekommt. Wenn du zwei
aufeinanderfolgende Schritte mathematisch zusammenfasst — etwa Kontrast und
Helligkeit in eine Operation — ist das Ergebnis algebraisch identisch und
praktisch verschieden, weil du eine Rundung wegoptimiert hast.

---

## 5. Dein Shader, Schritt für Schritt — die Mathematik dahinter

**Brightness:** `c * (1 + b * (1 - (r+g+b)/765))`
Der Faktor `765` ist `3 × 255`, also normiert `(r+g+b)/765` auf 0..1 — die
durchschnittliche Helligkeit des Pixels. Der Effekt ist damit bei dunklen Pixeln
maximal und bei hellen nahe null. Das ist eine Belichtungskurve, keine Verschiebung:
Lichter brennen nicht aus, Schwarz bleibt Schwarz (weil multiplikativ). Deutlich
gefälliger als `c + b*255`.

**Kontrast:** `c * slope + intercept`
Die Lehrbuchform ist `(c - 127.5) * s + 127.5`, also Skalierung um den Mittelpunkt.
Ausmultipliziert: `c*s + 127.5*(1-s)`. Dein `contrastIntercept = 255 * (-slope/2 + 0.5)`
ist genau das. Der asymmetrische Faktor `0.25` für negative Werte in
`toImageAdjustUniforms()` ist eine Geschmacksentscheidung: Kontrast wegnehmen wirkt
stärker als Kontrast hinzufügen, also wird die negative Seite gedämpft.

**Saturation:** `mix(luma, c, s)` in ausgeschriebener Form. Bei `s = 0`
Graustufen, bei `s = 1` Original, bei `s > 1` übersättigt. Werte über 1 lassen
Kanäle aus dem Bereich laufen — das `clamp` in `step8()` fängt das ab.

**Vibrance:** `c + (m - c) * (v * |m - avg| * 2/255)`
Jeder Kanal wird zum Pixel-Maximum `m` gezogen. Die Stärke hängt von `|m - avg|`
ab, also davon, wie weit das Maximum vom Durchschnitt entfernt ist. Bei einem
grauen Pixel ist `m ≈ avg`, der Faktor geht gegen null, nichts passiert. Bei einem
schon fast gesättigten Pixel ist der Abstand groß — aber `m - c` klein, weil die
Kanäle schon nah am Maximum sind. Der Effekt landet damit in der Mitte: bei
mäßig bunten Pixeln.

Das ist die übliche Vibrance-Idee („Sättigung nur da, wo noch wenig ist"), hier
über den Abstand zum Maximum statt über eine explizite Sättigungsberechnung gelöst
— billiger und im Ergebnis ähnlich. Der Hautton-Schutz, den Lightroom-Vibrance
zusätzlich hat, fehlt; das wäre eine faire Frage aus dem Publikum.

**Sepia:** Standard-Matrix (Microsoft/W3C-Herkunft, taucht in den SVG-Filtern und
in `ColorMatrix`-Beispielen auf), linear interpoliert zwischen Identität und
vollem Sepia über den `sepia`-Parameter. Deshalb `1.0 - 0.607 * sepia` in der
Diagonale statt einer festen `0.393`.

**Temperature:** `c + (t, 0, -t)` — Rot hoch, Blau runter. Eine sehr grobe
Näherung der Farbtemperatur; physikalisch korrekt wäre eine Verschiebung entlang
der Planckschen Kurve im XYZ- oder LMS-Raum mit Chromatic Adaptation
(Bradford-Matrix). Die einfache Variante ist Standard in Foto-Apps und für einen
Slider völlig ausreichend. Wenn jemand fragt: Ja, es ist eine Näherung. Nein, sie
korrigiert keinen Weißabgleich korrekt. Für ein Bedienelement ist das der
richtige Trade-off.

**Gamma:** `pow(c/255, γ) * 255` pro Kanal. `gammaExponent()` bildet einen
positiven Slider-Wert auf einen Exponenten **unter** 1 ab (aufhellen) und einen
negativen auf dessen Kehrwert **über** 1 (abdunkeln). Die Umkehrung ist nötig,
weil ein kleinerer Exponent die Mitteltöne anhebt.

Der `-0.0f`-Hinweis bei Vibrance ist ein schönes Detail: In IEEE 754 ist
`-0.0 == 0.0` zwar true, aber `Float.equals(-0.0f, 0.0f)` in Kotlin ist **false**,
weil `equals` auf den Bits arbeitet. Eine `data class` mit `-0.0f` im Feld ist
also ungleich derselben mit `0.0f` — und jedes `remember(uniforms)` oder
Uniform-Caching würde unnötig neu bauen. Deshalb wird der Int-Input negiert, nicht
das Ergebnis.

---

## 6. Die Messung — wie du Nachfragen standhältst

**Was du wissen musst, bevor du eine Zahl auf die Folie tust:**

- Gerät und Bildgröße nennen. „3× schneller" ohne Kontext ist wertlos.
- **Warmup:** Der erste Shader-Durchlauf enthält Compile-Zeit (Treiber übersetzt
  AGSL/SkSL in GPU-Instruktionen, kann hunderte ms kosten). Der erste CPU-Durchlauf
  enthält JIT-Aufwärmung. Beides verwerfen.
- **Median statt Bestwert**, und die Streuung kennen. Auf Mobile kommt Thermal
  Throttling dazu: nach zwanzig Läufen sieht die Welt anders aus.
- **GPU-Messung ist asynchron.** Ein `System.nanoTime()` um den Draw-Call herum
  misst, wie lange das *Einreihen* gedauert hat, nicht die Ausführung. Ehrlich
  gemessen wird entweder über die tatsächliche Frame-Rate (FrameMetrics,
  `JankStats`) oder über GPU-Timer-Queries. Wenn du das nicht gemacht hast, sag
  lieber „Frame-Zeit im Slider-Betrieb" statt „Shader-Laufzeit".
- Was **nicht** in der Zahl steckt: Upload des Bildes zur GPU, eventueller
  Readback, Speicherallokation.

**Die faire Version der Aussage** ist nicht „der Shader ist N-mal schneller",
sondern: *„Die CPU-Variante schafft bei dieser Bildgröße kein interaktives
Update. Die GPU-Variante bleibt unter der Frame-Grenze."* Das ist die
Eigenschaft, die dem Nutzer etwas bringt, und sie ist gegen Nachfragen robust.

---

## 7. Wann die GPU verliert

Damit du nicht als Fanboy dastehst — die Fälle, in denen der Umbau falsch wäre:

- **Kleine Datenmengen.** Overhead von Setup und Draw-Call dominiert. Unterhalb
  einiger zehntausend Pixel lohnt es nicht.
- **Ergebnis muss zurück in den RAM.** Readback ist die teuerste Operation im
  ganzen Spiel: er erzwingt Synchronisation zwischen CPU und GPU, die Pipeline
  läuft leer. Bei euch genau der Grund, warum der Byte-Loop für
  `PhotoImageBitmap`, den Photobook-Renderer und die nativen Renderer weiterlebt.
- **Sequentielle Abhängigkeiten.** Alles, wo Pixel N das Ergebnis von Pixel N-1
  braucht — Flood Fill, manche Kompressionsverfahren, Präfixsummen (die gehen,
  aber nur mit ganz anderen Algorithmen).
- **Wenig Arbeit pro geladenem Byte** bei gleichzeitig wahlfreiem Speicherzugriff.
  Die GPU ist auf zusammenhängende Zugriffe („coalesced") ausgelegt.
- **Einmalige Batch-Verarbeitung im Hintergrund.** Wenn niemand wartet, ist
  Wartbarkeit mehr wert als Geschwindigkeit.
- **Korrektheits-kritische Pfade ohne Testabdeckung.** Zwei Implementierungen
  ohne Pixelvergleichstest sind eine Zeitbombe, besonders wenn am Ende ein Druck
  steht.

---

## 8. GPUs jenseits der Grafik — Compute und KI

### Was ein Compute-Shader wirklich ist

Nimm die Grafikpipeline und streiche alles, was mit Dreiecken zu tun hat: kein
Vertex-Shader, kein Rasterizer, kein Framebuffer, kein Blending. Übrig bleibt ein
Programm, das für eine von dir festgelegte Menge an Threads parallel läuft und in
beliebige Buffer schreibt.

Der Programmierer legt dabei selbst fest, wie viele Threads es gibt und wie sie
gruppiert sind — in der Grafikpipeline bestimmt das der Rasterizer. Man startet
ein „Dispatch" über ein 1D-, 2D- oder 3D-Gitter von **Workgroups** (CUDA: Blocks),
jede Workgroup besteht aus Threads, die sich zwei Dinge teilen, die
Fragment-Shader nicht haben:

1. **Shared Memory** — ein kleiner, extrem schneller On-Chip-Speicher, gemeinsam
   für alle Threads einer Workgroup.
2. **Barrieren** — Synchronisationspunkte, an denen alle Threads der Gruppe
   aufeinander warten.

Genau diese beiden Dinge machen den Unterschied bei Filtern mit
**Nachbarschaftszugriff**. Ein 9×9-Blur im Fragment-Shader liest 81 Texel pro
Pixel — und das Nachbarpixel liest 81 fast identische nochmal. Im Compute-Shader
lädt die Workgroup ihre Kachel *einmal* in den Shared Memory, und alle Threads
bedienen sich daraus. Die Bandbreitenersparnis ist der Faktor, nicht die
Rechenleistung.

**Für dein konkretes Beispiel ändert das nichts**, und das ist eine ehrliche
Antwort: Dein Filter ist punktweise — jedes Pixel braucht nur sich selbst. Damit
gibt es nichts zu teilen, und der Fragment-Shader ist die richtige Wahl. Der
Compute-Shader würde erst interessant, wenn Blur, Schärfen oder ein Histogramm
dazukämen.

### Der historische Weg: GPGPU

Vor CUDA (2007) gab es keine Compute-Shader. Wer die Rechenleistung wollte,
musste sie sich über die Grafikpipeline erschleichen: Eingabedaten als Textur
hochladen, ein bildschirmfüllendes Rechteck zeichnen, den Fragment-Shader als
Rechenkern missbrauchen, Ergebnis als Bild wieder auslesen. Für iterative
Verfahren hat man zwischen zwei Texturen hin- und hergerendert („Ping-Pong").

Das war umständlich genug, dass man es nur gemacht hat, wenn es sich wirklich
lohnte — und es lohnte sich. Die Erkenntnis dahinter ist die Kernaussage deiner
Brückenfolie: **Die Hardware war für Pixel gebaut, die Mathematik war es nie.**
NVIDIA hat mit CUDA im Wesentlichen den Umweg entfernt und die Recheneinheiten
direkt adressierbar gemacht.

Der Kipppunkt für die KI-Seite ist gut datierbar: **AlexNet, 2012**, trainiert auf
zwei GeForce GTX 580 — also Consumer-Gaming-Hardware. Die Architektur selbst war
nicht neu; verfügbare Rechenleistung und Daten waren es.

### Warum neuronale Netze so gut passen

Ein Layer eines neuronalen Netzes ist im Kern eine **Matrixmultiplikation**
(GEMM: General Matrix Multiply). Prüfe das gegen die drei Kriterien von Slide 15
und es wird offensichtlich:

- Millionen unabhängige Multiply-Add-Operationen — **massiv parallel**
- Identische Operation für jedes Element — **keine Divergenz**
- Regelmäßige, vorhersagbare Speicherzugriffe — **coalesced**

Das ist strukturell dieselbe Form wie dein Bildfilter, nur mit anderen Zahlen
drin. Die Aktivierungsfunktion (ReLU und Verwandte) ist sogar wörtlich ein
punktweiser Filter — `max(0, x)` auf jedes Element, exakt die Form deiner
Filterschritte.

Der Unterschied zum Bildfilter: Matrixmultiplikation hat eine hohe **arithmetische
Intensität** (viele Rechenoperationen pro geladenem Byte, weil jedes Element
mehrfach verwendet wird). Deshalb ist *Training* rechenlimitiert, während dein
Filter bandbreitenlimitiert ist.

### Der stärkste Rückbezug: Inferenz ist bandbreitenlimitiert

Bei der Textgenerierung eines LLM wird pro erzeugtem Token jedes Gewicht des
Modells **einmal** gelesen und fast nichts damit gerechnet. Ein 70B-Modell in
int8 sind ~70 GB, die pro Token durch den Chip müssen. Die Rechenwerke langweilen
sich; der Speicherbus ist der Engpass.

Das ist exakt dieselbe Diagnose wie bei deinem Filter (Abschnitt 1), nur vier
Größenordnungen weiter oben — und die Konsequenzen sind dieselben:

- **Batching** hilft, weil dieselben Gewichte für mehrere Anfragen gleichzeitig
  genutzt werden — mehr Rechnung pro geladenem Byte. Dasselbe Prinzip wie dein
  Ein-Pass-Shader statt acht Pässen.
- **Warum HBM** (High Bandwidth Memory) auf Rechenzentrums-GPUs sitzt und warum
  Speicherbandbreite in den Datenblättern so prominent steht.
- Warum Quantisierung so viel bringt: halbe Bitbreite heißt halb so viele Bytes
  über den Bus, also fast doppelter Durchsatz — der Rechenvorteil ist zweitrangig.

### Quantisierung — deine `half`-vs-`float`-Frage in groß

In deinem Shader hast du bewusst `float3` statt `half3` gewählt, weil 16-Bit-Floats
die 0..255-Domäne nicht exakt genug abbilden, um mit dem Byte-Loop übereinzustimmen.
Das ist dieselbe Abwägung, die in der KI-Welt unter **Quantisierung** läuft:
fp32 → fp16/bf16 → fp8 → int8 → int4. Jede Stufe halbiert Speicherbedarf und
Bandbreite und verschlechtert die Genauigkeit ein Stück, bis das Ergebnis
irgendwann messbar schlechter wird.

**bf16** ist dabei ein hübsches Detail, falls jemand nachfragt: gleicher
Exponentenbereich wie fp32, aber weniger Mantissenbits. Für neuronale Netze ist
der Wertebereich wichtiger als die Nachkommastellen — umgekehrt als bei deinen
Farbwerten, wo der Bereich klein und bekannt ist und die Genauigkeit zählt. Gutes
Beispiel dafür, dass „weniger Bits" keine eindimensionale Entscheidung ist.

### Was sich an der Hardware geändert hat

Moderne GPUs sind nicht mehr nur schnelle Shader-Maschinen. Die relevanten
Ergänzungen:

- **Tensor Cores** (NVIDIA) bzw. Matrix Cores (AMD): dedizierte Einheiten, die
  kleine Matrixmultiplikationen in einem Schritt rechnen statt als Folge von
  Multiply-Adds. Der Großteil der KI-Rechenleistung moderner Karten kommt von
  hier, nicht von den klassischen Shader-Einheiten.
- **HBM**: gestapelter Speicher direkt neben dem Chip, mehrere TB/s statt einiger
  hundert GB/s.
- **Interconnect** (NVLink u. ä.): weil Modelle nicht mehr auf eine Karte passen,
  wird die Verbindung zwischen GPUs zum eigenen Engpass.

Damit ist eine Rechenzentrums-GPU strenggenommen keine Grafikkarte mehr — manche
haben gar keinen Displayausgang. Was geblieben ist, ist das Ausführungsmodell:
sehr viele Threads, dieselbe Instruktion, Latenz durch Überzeichnung versteckt.

### NPUs — wann nicht die GPU

Auf Handys (und zunehmend Laptops) sitzt neben CPU und GPU eine **NPU** (Apple
Neural Engine, Qualcomm Hexagon, Google Tensor). Noch spezialisierter als eine
GPU: fast nur Matrixoperationen, dafür bei deutlich besserer Energieeffizienz.
Die Staffelung ist damit CPU → GPU → NPU, jeweils weniger Flexibilität gegen mehr
Durchsatz pro Watt.

Für euren Anwendungsfall relevant, falls in der App einmal ein On-Device-Modell
läuft (Objekterkennung, Upscaling, Hintergrundfreistellung): Das gehört auf die
NPU, nicht in einen Shader. Zugang über NNAPI-Nachfolger, Core ML, TFLite
Delegates — nicht über AGSL.

### Die Grenze der Analogie

Damit du nicht überziehst: Ein Fragment-Shader ist **kein** Ersatz für einen
KI-Stack. Was in deinem Fall fehlt, sind Shared Memory, echte Buffer-Ausgabe,
Tensor-Einheiten und die gesamte Infrastruktur (Autodiff, Kernel-Fusion,
Scheduling). Die Gemeinsamkeit liegt auf der Ebene des **Ausführungsmodells** und
der **Kostenlogik** — nicht auf der Werkzeugebene. Genau so solltest du es auf
der Folie auch formulieren.

---

## 9. Vorbereitete Antworten auf wahrscheinliche Fragen

**„Warum nicht `ColorMatrixColorFilter` / CSS `filter`? Das ist doch schon GPU."**
Für lineare Farbtransformationen (Saturation, Sepia, Kontrast) stimmt das. Nicht
abbildbar sind: die Belichtungskurve bei Brightness (der Faktor hängt vom Pixel
ab), Vibrance (hängt vom Maximum des Pixels ab), Gamma (`pow`), und vor allem die
Rundung zwischen den Schritten. Eine Farbmatrix kann nur affin-linear.

**„Warum nicht RenderScript?"**
Ist seit Android 12 deprecated. Nachfolgeempfehlung von Google sind genau Vulkan
Compute und `RuntimeShader`.

**„Läuft das auf allen Geräten?"**
`RuntimeShader` braucht Android 13 (API 33). Darunter brauchst du den Byte-Loop
als Fallback — den ihr ohnehin habt, was die Entscheidung deutlich billiger macht.
Das ist ein guter Punkt fürs Publikum: Der Umbau war nur deshalb risikoarm, weil
die alte Implementierung nicht weg musste.

**„Wie testet ihr, dass beide Pfade dasselbe machen?"**
Die ehrliche Antwort, falls es noch nicht existiert: Das ist die richtige Frage,
und ein Pixelvergleichstest über ein Set Referenzbilder und Parameterkombinationen
ist die Absicherung. Toleranz muss man festlegen — exakte Gleichheit über alle
GPUs ist wegen Präzisionsunterschieden nicht garantierbar, ±1 pro Kanal ist ein
üblicher Rahmen.

**„Hat der Shader nicht ein `if` drin?"**
Ja, eines — die Alpha-Prüfung. Das ist Korrektheit (Division durch null), nicht
Optimierung, und es divergiert bei Fotos praktisch nie innerhalb einer Thread-Gruppe.

**„Warum nicht einfach acht kleine Shader hintereinander?"**
Weil jeder Pass das komplette Bild liest und schreibt. Bei Bandbreitenlimitierung
kostet das achtmal so viel wie ein Pass mit acht Rechenschritten. Auf
Tile-basierten Mobile-GPUs noch mehr.

**„Was ist mit WebGPU?"**
Der Nachfolger von WebGL, näher an moderner nativer API (Vulkan/Metal/DX12), mit
echten Compute-Shadern. Für Browser-Bildverarbeitung mittelfristig die richtige
Wahl. Sprache ist WGSL, nicht GLSL.

**„Könnte man den Byte-Loop nicht einfach löschen und immer über die GPU gehen,
auch für Readback?"**
Technisch ja, praktisch teuer: Readback synchronisiert, und du hättest die
Abhängigkeit von Android 13 dann auf allen Pfaden. Es wäre eine Messung wert — mit
der Prämisse, dass ein langsamerer Pfad mit *einer* Implementierung
möglicherweise mehr wert ist als zwei schnelle, die auseinanderlaufen können.
Das ist eine gute Stelle, um die Frage ans Publikum zurückzugeben.

**„Wie debuggt man das?"**
Zwischenergebnis als Farbe zurückgeben und hinsehen. Ansonsten: RenderDoc
(Desktop/Android), Android GPU Inspector, Xcode Frame Debugger. Für kleine
Shader ist shadertoy.com zum Ausprobieren schneller als jedes Tooling.

---

**„Könnte man Bildfilter dann nicht gleich mit einem Compute-Shader machen?"**
Für punktweise Filter wie meinen bringt es nichts — es gibt keine
Nachbarschaftsdaten zu teilen, und der Fragment-Shader hat den Vorteil, dass das
Ergebnis direkt im Rendering landet, ohne Umweg über einen Buffer. Sobald Blur,
Schärfen oder ein Histogramm dazukommen, kippt die Rechnung.

**„Warum reden alle über Speicherbandbreite statt über TFLOPS?"**
Weil LLM-Inferenz pro Token jedes Gewicht einmal liest und wenig damit rechnet.
Die Rechenwerke sind nicht der Engpass, der Speicherbus ist es. Dasselbe
Roofline-Argument wie bei einem Bildfilter, nur in anderer Größenordnung.

**„Hat das dann noch was mit Shadern zu tun oder ist das eine andere Welt?"**
Das Ausführungsmodell ist dasselbe — sehr viele Threads, dieselbe Instruktion,
Latenz durch Überzeichnung versteckt. Der Werkzeugkasten ist ein anderer:
Tensor Cores, Shared Memory, Autodiff, Kernel-Fusion. Die Analogie trägt auf der
Ebene „warum ist das schnell", nicht auf der Ebene „wie schreibe ich es".

**„Warum nicht die NPU?"**
Für ein neuronales Netz auf dem Handy wäre die NPU tatsächlich die richtige
Einheit — energieeffizienter und dafür gebaut. Für einen frei formulierten
Bildfilter geht das nicht: Die NPU kann fast nur Matrixoperationen, meine
Filterkette ist beliebige Arithmetik. Flexibilität gegen Effizienz, und ich
brauche hier die Flexibilität.

---

## 10. Wenn du noch tiefer willst

- **The Book of Shaders** (thebookofshaders.com) — der beste Einstieg, interaktiv
- **Inigo Quilez' Artikel** (iquilezles.org) — Mathematik für Shader, sehr dicht
- **Android-Doku zu AGSL** — knapp, aber die Referenz für deinen konkreten Fall
- **Skia SkSL-Doku** — was in Compose Desktop tatsächlich geht
- **shadertoy.com** — zum Spielen und für Demos, die man live verändern kann
- **„Making Deep Learning Go Brrrr From First Principles"** von Horace He — die
  klarste Erklärung von Compute- vs. Bandbreiten-Limitierung bei neuronalen Netzen
- **NVIDIAs CUDA Programming Guide**, Kapitel zum Speichermodell — auch lesenswert,
  wenn man nie CUDA schreibt
- **„A trip through the Graphics Pipeline"** von Fabian Giesen — alter, langer
  Blogpost-Serie, immer noch die beste Erklärung, was zwischen Draw-Call und
  Bildschirm passiert
