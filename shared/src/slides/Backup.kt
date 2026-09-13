package de.myposter.academy.shaders.slides

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.Fill
import de.myposter.academy.shaders.theme.HeroFrame
import de.myposter.academy.shaders.theme.SlideFrame
import net.kodein.cup.Slide
import net.kodein.cup.Slides
import net.kodein.cup.imgexp.Export
import net.kodein.cup.speaker.SpeakerNotes
import net.kodein.cup.utils.slideContextOf

/** A backup slide: a title, a few facts, no steps. */
private fun backup(
    name: String,
    id: String,
    title: String,
    notes: String,
    vararg facts: String,
): Slide =
    Slide(
        name = name,
        // Backup slides stay out of the PDF export.
        context = slideContextOf(Export.none(), SpeakerNotes(notes)),
    ) {
        SlideFrame(id = id, timing = "Backup", title = title) {
            Fill(spacedBy = 13.dp) {
                facts.forEach { Body(it) }
            }
        }
    }

/** The divider the speaker lands on when the Q&A starts. */
val backupIntro: Slide =
    Slide(
        name = "backupIntro",
        context = slideContextOf(Export.none(), SpeakerNotes("Ab hier nur auf Nachfrage.")),
    ) {
        HeroFrame(id = "Backup", timing = "Nur auf Nachfrage") {
            Text(
                text = "Backup",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text =
                    "Messmethodik · Divergenz · Präzision · Compute · " +
                        "Mobile-GPUs · Tensor Cores · NPUs",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }

val backupMessmethodik: Slide =
    backup(
        name = "backupMessmethodik",
        id = "B1 · backupMessmethodik",
        title = "Messmethodik",
        notes = "Zahlen nur nennen, wenn danach gefragt wird.",
        "Gerät und Bildgröße: fest, im Repo dokumentiert",
        "Warmup vor der Messung, 30 Läufe, Median statt Mittelwert",
        "Gemessen wird die Zeit pro Frame im Slider-Drag",
        "Nicht gemessen: Laden und Dekodieren des Bildes, Shader-Compile beim ersten Frame",
    )

val backupDivergenz: Slide =
    backup(
        name = "backupDivergenz",
        id = "B2 · backupDivergenz",
        title = "Das eine if im Shader",
        notes = "Kommt fast immer als Frage, weil auf Slide 6 „kein einziges if“ steht.",
        "if (a <= 0.0) return half4(0.0);",
        "Divergenz kostet nur, wenn sie innerhalb einer Gruppe auftritt. " +
            "Diese Bedingung fällt für ein ganzes Bild fast immer einheitlich aus.",
        "Teuer wären Verzweigungen, die vom Pixelwert abhängen, also genau die, " +
            "die die Filterschritte ersetzt hätten.",
    )

val backupPraezision: Slide =
    backup(
        name = "backupPraezision",
        id = "B3 · backupPraezision",
        title = "half oder float",
        notes = "Nur wenn jemand nach half vs. float fragt.",
        "half ist auf Mobile-GPUs schneller, hat aber in der 0..255-Domäne zu wenige " +
            "Stufen für ein pixelgleiches Ergebnis.",
        "Deshalb float3 in der Rechnung und half4 nur am Ein- und Ausgang.",
        "Präzision ist zwischen Geräten nicht garantiert gleich. Das ist der Grund " +
            "für die Pixelvergleichs-Tests.",
    )

val backupCompute: Slide =
    backup(
        name = "backupCompute",
        id = "B4 · backupCompute",
        title = "Compute-Shader im Detail",
        notes = "Wenn jemand nach Blur oder Shared Memory fragt.",
        "Workgroups statt Pixelraster: du bestimmst selbst, welche Threads zusammengehören.",
        "Shared Memory erlaubt Datenaustausch innerhalb einer Workgroup, " +
            "was ein Fragment-Shader nicht kann.",
        "Deshalb profitiert ein Blur davon: jedes Pixel braucht seine Nachbarn, " +
            "und die lassen sich einmal gemeinsam laden statt neunmal einzeln.",
    )

val backupTbdr: Slide =
    backup(
        name = "backupTbdr",
        id = "B5 · backupTbdr",
        title = "Warum Mobile-GPUs anders sind",
        notes = "Nur bei Fragen zu Android-Performance.",
        "Tile-based Rendering: das Bild wird in Kacheln zerlegt, die im schnellen " +
            "On-Chip-Speicher bearbeitet werden.",
        "Die Bandbreite zum Hauptspeicher begrenzt, nicht die Arithmetik. " +
            "Das Gerät hat ein Energiebudget, keinen Kühlkörper.",
        "Praktische Folge: ein Filter, der das Bild einmal liest und einmal schreibt, " +
            "ist auf Mobile fast gratis. Mehrfache Durchläufe sind es nicht.",
    )

val backupTensorCores: Slide =
    backup(
        name = "backupTensorCores",
        id = "B6 · backupTensorCores",
        title = "Tensor Cores",
        notes = "Nicht von selbst aufschlagen — das ist ein eigener Vortrag.",
        "Eigene Einheiten, die kleine Matrixmultiplikationen in einem Schritt rechnen, " +
            "statt sie aus einzelnen Multiplikationen zusammenzusetzen.",
        "Sie arbeiten in reduzierter Präzision, derselbe Handel wie bei der Quantisierung.",
        "Deshalb ist ein aktueller Rechenzentrums-Chip keine große Grafikkarte mehr: " +
            "die Grafik-Pipeline ist darauf nur noch ein Teil.",
    )

val backupNpu: Slide =
    backup(
        name = "backupNpu",
        id = "B7 · backupNpu",
        title = "NPUs auf dem Handy",
        notes = "Letzte Backup-Folie.",
        "Richtig für feste Modelloperationen bei niedrigem Energieverbrauch: " +
            "Segmentierung, Erkennung, Upscaling.",
        "Falsch für unseren Bildfilter: das Ergebnis soll direkt gezeichnet werden, " +
            "und genau dafür ist die GPU der kürzere Weg.",
        "Die Auswahlregel bleibt dieselbe wie auf Folie 16: Wo liegen die Daten, " +
            "und wo soll das Ergebnis hin?",
    )

/** All backup slides, in the order the Q&A is likely to need them. */
val backupSlides: Slides =
    Slides(
        backupIntro,
        backupMessmethodik,
        backupDivergenz,
        backupPraezision,
        backupCompute,
        backupTbdr,
        backupTensorCores,
        backupNpu,
    )
