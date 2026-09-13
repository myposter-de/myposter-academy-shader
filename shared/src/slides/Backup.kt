package de.myposter.academy.shaders.slides

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.Muted
import de.myposter.academy.shaders.theme.TitledSlide
import net.kodein.cup.Slide
import net.kodein.cup.Slides
import net.kodein.cup.imgexp.Export
import net.kodein.cup.speaker.SpeakerNotes
import net.kodein.cup.utils.slideContextOf
import net.kodein.cup.widgets.material3.BulletPoints

/** A backup slide: a title, a list of facts, no steps. */
private fun backup(name: String, title: String, notes: String, vararg facts: String): Slide =
    Slide(
        name = name,
        // Backup slides stay out of the PDF export.
        context = slideContextOf(Export.none(), SpeakerNotes(notes)),
    ) {
        TitledSlide(title) {
            Muted("Backup — nur für Q&A", size = 12)
            Spacer(Modifier.height(14.dp))
            BulletPoints(spacedBy = 10.dp) {
                facts.forEach { fact -> item { Body(fact, size = 15) } }
            }
        }
    }

val backupMessmethodik: Slide = backup(
    name = "backupMessmethodik",
    title = "Messmethodik",
    notes = "Nenne Gerät, Bildgröße und Anzahl Läufe. Sag auch, was nicht gemessen wurde.",
    "Gerät: der Präsentationsrechner, nicht ein Benchmark-Server.",
    "Bildgröße: fest, 4000 × 3000 (12 MP), im Repo reproduzierbar erzeugt.",
    "Warmup: die ersten Läufe zählen nicht. Ein kalter Shader-Compile kostet Hunderte Millisekunden.",
    "Median über die letzten 31 Läufe, nicht Mittelwert. Ein GC-Ausreißer soll die Zahl nicht bestimmen.",
    "CPU-Zahl: eine volle Filterrunde über das Pixel-Array.",
    "GPU-Zahl: gemessene Zeit pro Frame, Filter inklusive.",
    "Nicht gemessen: Upload des Bildes zur GPU, Readback, Dekodierung der JPEG-Datei.",
)

val backupDivergenz: Slide = backup(
    name = "backupDivergenz",
    title = "Warum das eine `if` unkritisch ist",
    notes = "Divergenz kostet nur innerhalb einer Gruppe.",
    "Der Shader hat genau ein `if`: `src.a <= 0.0`.",
    "Divergenz kostet nur, wenn sie innerhalb einer Thread-Gruppe auftritt.",
    "Für ein ganzes Foto fällt dieser Test fast immer einheitlich aus.",
    "Gruppen an der Kante eines transparenten Bereichs divergieren — das sind wenige.",
)

val backupPraezision: Slide = backup(
    name = "backupPraezision",
    title = "half vs. float",
    notes = "Warum float3 für die 0..255-Domäne.",
    "`half` hat 10 Bit Mantisse. Werte über 1024 lassen sich nicht mehr exakt darstellen.",
    "Die Filterformeln rechnen in 0..255, mit Zwischenergebnissen darüber.",
    "Rundung in `half` weicht dann sichtbar von der CPU-Variante ab.",
    "Deshalb `float3` im Rechenpfad und `half4` nur an der Schnittstelle zu Skia.",
    "Zwischen Geräten verhält sich `half` nicht identisch. `float` ist die sichere Wahl.",
)

val backupCompute: Slide = backup(
    name = "backupCompute",
    title = "Compute-Shader im Detail",
    notes = "Nur wenn jemand nachhakt.",
    "Workgroup: eine Gruppe von Threads, die sich Shared Memory teilt.",
    "Shared Memory ist schneller als der globale Speicher, aber klein.",
    "Blur profitiert davon: jedes Pixel liest die Nachbarn, die Gruppe lädt den Ausschnitt einmal.",
    "Ein Fragment-Shader kann das nicht — er sieht nur sein eigenes Pixel.",
    "APIs: CUDA, Vulkan Compute, Metal Compute, WebGPU.",
)

val backupTbdr: Slide = backup(
    name = "backupTbdr",
    title = "Mobile-GPUs sind anders",
    notes = "Tiling und Bandbreite.",
    "Mobile-GPUs arbeiten Tile-based Deferred Rendering: sie zerlegen das Bild in Kacheln.",
    "Eine Kachel passt in schnellen On-Chip-Speicher. Das spart Bandbreite, und Bandbreite kostet Strom.",
    "Desktop-GPUs haben breite Speicheranbindung und rendern direkt.",
    "Folge: Was auf dem Desktop billig ist, kann auf dem Handy an der Bandbreite hängen.",
)

val backupTensorCores: Slide = backup(
    name = "backupTensorCores",
    title = "Tensor Cores",
    notes = "Kurz halten, das ist ein eigener Vortrag.",
    "Tensor Cores sind Matrix-Einheiten: sie rechnen kleine Matrixmultiplikationen in einem Schritt.",
    "Sie sind kein Nebenprodukt der Grafik-Pipeline, sondern für Inferenz und Training gebaut.",
    "Eine H100 ist deshalb keine „große Grafikkarte“ mehr — die Grafik-Pipeline ist dort Beiwerk.",
    "Der Kern bleibt: dieselbe Operation, sehr viele unabhängige Daten.",
)

val backupNpu: Slide = backup(
    name = "backupNpu",
    title = "NPUs auf Handys",
    notes = "Wann nicht die GPU.",
    "Eine NPU ist auf Integer-Matrixmathematik und niedrige Leistungsaufnahme optimiert.",
    "Für quantisierte Modelle ist sie effizienter als die GPU.",
    "Für Bildfilter, deren Ergebnis direkt gezeichnet wird, ist sie falsch: das Ergebnis müsste zurück.",
    "Faustregel: Ergebnis auf den Bildschirm → GPU. Ergebnis in einen Buffer → NPU prüfen.",
)

/** All backup slides, in the order the Q&A is likely to need them. */
val backupSlides: Slides = Slides(
    backupMessmethodik,
    backupDivergenz,
    backupPraezision,
    backupCompute,
    backupTbdr,
    backupTensorCores,
    backupNpu,
)
