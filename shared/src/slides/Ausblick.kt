package de.myposter.academy.shaders.slides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.Caption
import de.myposter.academy.shaders.theme.DarkSection
import de.myposter.academy.shaders.theme.Fill
import de.myposter.academy.shaders.theme.SectionLabel
import de.myposter.academy.shaders.theme.SlideFrame
import de.myposter.academy.shaders.theme.Step
import de.myposter.academy.shaders.theme.deck
import de.myposter.academy.shaders.theme.monoFamily
import net.kodein.cup.Slide
import net.kodein.cup.speaker.SpeakerNotes

// Slide 13 — 15:00–16:30
val leiter by
    Slide(
        stepCount = 6,
        context =
            SpeakerNotes(
                """
        Stufe 2 ist die eigentliche Nachricht dieser Folie: Der Einstieg kostet
        heute eine Funktion, nicht eine Grafik-Pipeline. Das war vor fünf Jahren
        anders.
        """
            ),
    ) { step ->
        SlideFrame(
            id = "13 · leiter",
            timing = "15:00–16:30 · 5 Steps",
            title = "Die Leiter der Abstraktionen",
        ) {
            Fill(spacedBy = 10.dp) {
                Step(step >= 1) {
                    Rung(
                        "01",
                        "Deklarative Filter",
                        "CSS filter, SVG-Primitives, ColorMatrixColorFilter, " +
                            "alle nutzen intern die GPU",
                    )
                }
                Step(step >= 2) {
                    Rung(
                        number = "02",
                        title = "Shader ohne Pipeline",
                        text =
                            "AGSL, SkSL, Paint Worklets. Du schreibst nur main(). " +
                                "Hier läuft mein Code. Und hier läuft diese Präsentation.",
                        background = MaterialTheme.deck.panelSuccess,
                        numberColor = MaterialTheme.deck.gpu,
                        textColor = MaterialTheme.colorScheme.onBackground,
                    )
                }
                Step(step >= 3) {
                    Rung("03", "Abstraktion über der Pipeline", "PixiJS, regl, Three.js, glfx.js")
                }
                Step(step >= 4) {
                    Rung("04", "Volle Kontrolle", "WebGL, WebGPU, OpenGL, Metal, Vulkan")
                }
                Step(step >= 5) {
                    Rung(
                        "05",
                        "Querschnitt",
                        "Compute-Shader, wenn das Ergebnis gar nicht auf den Bildschirm soll",
                    )
                }
            }
        }
    }

@Composable
private fun Rung(
    number: String,
    title: String,
    text: String,
    background: Color = MaterialTheme.deck.panel,
    numberColor: Color = MaterialTheme.colorScheme.outline,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(MaterialTheme.deck.cornerM))
                .background(background)
                .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Text(
            text = number,
            modifier = Modifier.width(40.dp).alignByBaseline(),
            style = MaterialTheme.typography.titleLarge,
            fontFamily = monoFamily,
            color = numberColor,
        )
        Text(
            text = title,
            modifier = Modifier.width(260.dp).alignByBaseline(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.width(12.dp))
        Caption(text, Modifier.weight(1f).alignByBaseline(), color = textColor)
    }
}

// Slide 14 — 16:30–17:45
val computeUndKi by
    Slide(
        stepCount = 6,
        context =
            SpeakerNotes(
                """
        Kernsatz: „Die GPU ist keine Grafikkarte, die man für KI zweckentfremdet
        hat. Sie ist eine Durchsatzmaschine, und Grafik war ihre erste
        Anwendung.“

        Nicht in Tensor-Core-Details abbiegen — eigener Vortrag, siehe Backup.
        Über der Zeit: Step 2 ist der entbehrlichste.
        """
            ),
    ) { step ->
        DarkSection {
            SlideFrame(
                id = "14 · computeUndKi",
                timing = "16:30–17:45 · 5 Steps",
                title = "Compute-Shader und KI",
            ) {
                // Two columns. The left one walks the argument, the right one
                // carries the two back-references to earlier slides.
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement =
                            Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    ) {
                        Step(step >= 1) {
                            ComputePoint(
                                "Die Pipeline weglassen",
                                MaterialTheme.deck.teal,
                                "Ein Compute-Shader ist ein Shader ohne Rasterizer und ohne " +
                                    "Framebuffer. Übrig bleibt der Kern, eine Funktion für " +
                                    "hunderttausende unabhängige Datenpunkte.",
                            )
                        }
                        Step(step >= 2) {
                            ComputePoint(
                                "Die Geschichte",
                                MaterialTheme.deck.teal,
                                "Vor CUDA haben Forscher ihre Daten als Textur verpackt, durch " +
                                    "einen Fragment-Shader geschickt und das Bild wieder " +
                                    "ausgelesen. GPGPU.",
                            )
                        }
                        Step(step >= 3) {
                            ComputePoint(
                                "Warum neuronale Netze",
                                MaterialTheme.deck.teal,
                                "Ein Layer ist im Kern eine Matrixmultiplikation: identische " +
                                    "Operation, millionenfach unabhängig, keine Verzweigung auf " +
                                    "Datenbasis. Dieselbe Form wie mein Bildfilter.",
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement =
                            Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    ) {
                        Step(step >= 4) {
                            ComputePoint(
                                "Rückbezug 1 · Bandbreite",
                                MaterialTheme.deck.amber,
                                "Bei LLM-Inferenz begrenzt die Speicherbandbreite, nicht die " +
                                    "Rechenleistung. Meinen Filter begrenzt ebenso das Lesen und " +
                                    "Schreiben des Bildes, nicht die Arithmetik.",
                            )
                        }
                        Step(step >= 5) {
                            ComputePoint(
                                "Rückbezug 2 · Präzision",
                                MaterialTheme.deck.amber,
                                "Die Frage aus step8(), wie viele Bits es wirklich braucht, heißt " +
                                    "in der KI-Welt Quantisierung: fp32, fp16, int8, int4.",
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
private fun ComputePoint(label: String, labelColor: Color, text: String) {
    Column(Modifier.fillMaxWidth()) {
        SectionLabel(label, labelColor)
        Spacer(Modifier.height(9.dp))
        Body(text, color = MaterialTheme.colorScheme.onBackground)
    }
}
