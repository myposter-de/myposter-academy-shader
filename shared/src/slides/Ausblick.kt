package de.myposter.academy.shaders.slides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.Muted
import de.myposter.academy.shaders.theme.Palette
import de.myposter.academy.shaders.theme.Reveal
import de.myposter.academy.shaders.theme.Statement
import de.myposter.academy.shaders.theme.TitledSlide
import net.kodein.cup.Slide
import net.kodein.cup.speaker.SpeakerNotes

// Slide 13 — 15:00–16:30
val leiter by Slide(
    stepCount = 5,
    context = SpeakerNotes(
        """
        Stufe 2 ist die eigentliche Nachricht dieser Folie:
        Der Einstieg kostet heute eine Funktion, nicht eine Grafik-Pipeline.
        Das war vor fünf Jahren anders.
        """
    )
) { step ->
    TitledSlide("Die Leiter — von „kein Shader-Code“ bis „volle Kontrolle“") {
        Column(Modifier.fillMaxWidth()) {
            Rung(
                visible = step >= 1,
                level = "1",
                title = "Deklarative Filter",
                text = "CSS filter, SVG-Primitives, Canvas filter, ColorMatrixColorFilter. Nutzen intern die GPU.",
            )
            Rung(
                visible = step >= 2,
                level = "2",
                title = "Shader ohne Pipeline",
                text = "AGSL (RuntimeShader, Android 13+), SkSL (Skia, Compose Desktop), CSS Houdini. Du schreibst nur main().",
                highlight = true,
            )
            Rung(
                visible = step >= 3,
                level = "3",
                title = "Abstraktion über der Pipeline",
                text = "PixiJS, regl, Three.js, glfx.js.",
            )
            Rung(
                visible = step >= 4,
                level = "4",
                title = "Volle Kontrolle",
                text = "WebGL / WebGPU, OpenGL / Metal / Vulkan.",
            )
            Rung(
                visible = step >= 5,
                level = "×",
                title = "Querschnitt: Compute-Shader",
                text = "Wenn das Ergebnis gar nicht auf den Bildschirm soll.",
            )
        }
        Spacer(Modifier.weight(1f))
        Reveal(step >= 2) {
            Muted("Hier läuft mein Code. Und hier läuft diese Präsentation.", size = 15)
        }
    }
}

@Composable
private fun Rung(
    visible: Boolean,
    level: String,
    title: String,
    text: String,
    highlight: Boolean = false,
) {
    Reveal(visible) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Palette.Surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = level,
                color = if (highlight) Palette.Accent else Palette.Muted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(20.dp),
            )
            Column {
                Text(
                    text = title,
                    color = if (highlight) Palette.Accent else Palette.OnBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Muted(text, size = 12)
            }
        }
    }
}

// Slide 14 — 16:30–17:45
val computeUndKi by Slide(
    stepCount = 5,
    context = SpeakerNotes(
        """
        Kernsatz für diese Folie:
        **„Die GPU ist keine Grafikkarte, die man für KI zweckentfremdet hat.
        Sie ist eine Durchsatzmaschine, und Grafik war ihre erste Anwendung."**

        Nicht in Details zu Tensor Cores abbiegen — das ist ein eigener Vortrag.
        Falls jemand nachhakt: Backup-Slide `backupTensorCores`.

        Wenn du über der Zeit bist: Step 2 (Geschichte) ist der entbehrlichste.
        Die beiden Rückbezüge sind der Grund, warum die Folie existiert.
        """
    )
) { step ->
    TitledSlide("Dasselbe Prinzip, ganz anderes Feld") {
        Column(Modifier.fillMaxWidth()) {
            Point(
                visible = step >= 1,
                title = "Die Pipeline weglassen",
                text = "Ein Compute-Shader ist ein Shader ohne Rasterizer und ohne Framebuffer. " +
                    "Übrig bleibt der Kern: eine Funktion für hunderttausende unabhängige Datenpunkte. " +
                    "Das Ergebnis geht in einen Buffer statt in ein Bild.",
            )
            Point(
                visible = step >= 2,
                title = "Die Geschichte",
                text = "Vor CUDA haben Forscher ihre Daten als Textur verpackt, durch einen Fragment-Shader " +
                    "geschickt und das Bild wieder ausgelesen. „GPGPU“. Die Hardware war für Pixel gebaut, " +
                    "die Mathematik war es nie.",
            )
            Point(
                visible = step >= 3,
                title = "Warum ausgerechnet neuronale Netze",
                text = "Ein Layer ist im Kern eine Matrixmultiplikation. Millionen unabhängige Multiplikationen " +
                    "und Additionen, identische Operation, keine Verzweigung auf Datenbasis. " +
                    "Dieselbe Form wie mein Bildfilter.",
                accent = Palette.Accent,
            )
            Point(
                visible = step >= 4,
                title = "Rückbezug 1 — Bandbreite",
                text = "Bei LLM-Inferenz ist der Engpass nicht die Rechenleistung, sondern die Speicherbandbreite. " +
                    "Mein Filter ist auch nicht rechenlimitiert, sondern limitiert durch Lesen und Schreiben des Bildes.",
                accent = Palette.Gpu,
            )
            Point(
                visible = step >= 5,
                title = "Rückbezug 2 — Quantisierung",
                text = "Wie viele Bits brauche ich wirklich? In der KI-Welt heißt das fp32 → fp16 → int8 → int4. " +
                    "Dieselbe Abwägung wie bei step8(): weniger Präzision, mehr Durchsatz, irgendwo eine Grenze.",
                accent = Palette.Gpu,
            )
        }
    }
}

@Composable
private fun Point(
    visible: Boolean,
    title: String,
    text: String,
    accent: Color = Palette.Muted,
) {
    Reveal(visible) {
        Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text(title, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Body(text, size = 13)
        }
    }
}
