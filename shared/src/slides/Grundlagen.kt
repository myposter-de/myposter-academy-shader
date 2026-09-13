package de.myposter.academy.shaders.slides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.myposter.academy.shaders.shader.STARTER_SHADER
import de.myposter.academy.shaders.shader.ShaderCanvas
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.Muted
import de.myposter.academy.shaders.theme.Palette
import de.myposter.academy.shaders.theme.Promise
import de.myposter.academy.shaders.theme.Reveal
import de.myposter.academy.shaders.theme.Statement
import de.myposter.academy.shaders.theme.TitledSlide
import net.kodein.cup.Slide
import net.kodein.cup.speaker.SpeakerNotes
import net.kodein.cup.widgets.material3.BulletPoints
import org.jetbrains.skia.RuntimeEffect

// Slide 5 — 4:00–5:30
val warumSchnell by Slide(
    stepCount = 4,
    context = SpeakerNotes(
        """
        Die Rechnung laut vormachen.

        „Pro Slider-Bewegung" ist der Satz, der die Zahl vom Benchmark
        zur Alltagserfahrung macht.
        """
    )
) { step ->
    TitledSlide("Warum die GPU das kann") {
        BulletPoints(spacedBy = 14.dp) {
            item(visible = step >= 1) {
                Body("12 MP = 12.000.000 Pixel", size = 19)
            }
            item(visible = step >= 2) {
                Body("× 8 Filterschritte × 3 Kanäle ≈ 300 Mio. Rechenoperationen", size = 19)
            }
            item(visible = step >= 3) {
                Body("CPU: einige Kerne, seriell, mit SIMD vielleicht 8 Werte gleichzeitig", size = 19)
            }
            item(visible = step >= 4) {
                Body("GPU: hunderte bis tausende Recheneinheiten für genau diese Arithmetik", size = 19)
            }
        }
        Spacer(Modifier.weight(1f))
        Reveal(step >= 2) {
            Muted("… pro Slider-Bewegung. Und ein Slider erzeugt 60 davon pro Sekunde.", size = 16)
        }
    }
}

// Slide 6 — 5:30–7:00
val simt by Slide(
    stepCount = 3,
    context = SpeakerNotes(
        """
        Der Divergenz-Punkt ist das, was die meisten nicht wissen und was am
        ehesten hängenbleibt.

        Optional als Q&A-Munition: das eine `if` im Shader (`alpha <= 0`) ist
        unkritisch, weil es für ein ganzes Bild fast immer einheitlich ausfällt.
        Divergenz kostet nur, wenn sie *innerhalb* einer Gruppe auftritt.
        """
    )
) { step ->
    TitledSlide("SIMT — eine Instruktion, viele Daten") {
        BulletPoints(spacedBy = 14.dp) {
            item(visible = step >= 1) {
                Body("Die GPU führt Pixel in Gruppen aus — 32 oder 64, je nach Hersteller.")
            }
            item(visible = step >= 1) {
                Body("Alle Threads einer Gruppe führen dieselbe Instruktion aus, auf verschiedenen Daten.")
            }
            item(visible = step >= 2) {
                Body("Ein `if`, bei dem die Gruppe sich aufteilt: die GPU läuft beide Zweige, jeweils mit der Hälfte deaktiviert.")
            }
            item(visible = step >= 2) {
                Body("Kosten: beide Zweige. Nutzen: einer.")
            }
        }
        Spacer(Modifier.weight(1f))
        Reveal(step >= 3) {
            Promise(1, "Deshalb hat mein Shader kein einziges `if` im Pixelpfad.")
        }
    }
}

// Slide 7 — 7:00–7:45
val kostenmodell by Slide(
    stepCount = 2,
    context = SpeakerNotes(
        """
        Versprechen 2 und 3 aufmachen. Beide werden auf Slide 11 eingelöst.
        """
    )
) { step ->
    TitledSlide("Das Kostenmodell") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Reveal(step >= 0) {
                Column {
                    Body("Was man einmal pro Draw rechnen kann, gehört nicht in den Pixelcode.")
                    Spacer(Modifier.height(10.dp))
                    Promise(2, "Zwei Zeilen Kotlin ersetzen 12 Millionen Divisionen.")
                }
            }
            Reveal(step >= 1) {
                Column {
                    Body("Der Shader ist eine eigene Sprache mit eigenen Konventionen.")
                    Spacer(Modifier.height(10.dp))
                    Promise(3, "Eine falsch gelesene Matrix, und das Foto kommt falsch aus dem Drucker.")
                }
            }
        }
    }
}

// Slide 8 — 7:45–9:00
val wasIstEinShader by Slide(
    stepCount = 3,
    context = SpeakerNotes(
        """
        „Du schreibst den Schleifenkörper und gibst die Kontrolle über das Wann ab"
        ist der Satz, den die Leute mitnehmen sollen. Wiederhole ihn.
        """
    )
) { step ->
    TitledSlide("Was ein Shader eigentlich ist") {
        Reveal(step >= 0) {
            Statement("Ein Fragment-Shader ist eine reine Funktion: Koordinate → Farbe")
        }
        Spacer(Modifier.height(18.dp))
        BulletPoints(spacedBy = 12.dp) {
            item(visible = step >= 1) {
                Body("Du schreibst nicht die Schleife. Du schreibst den Schleifenkörper.")
            }
            item(visible = step >= 1) {
                Body("Wann und wie oft er läuft, entscheidet das System.")
            }
            item(visible = step >= 2) {
                Body("Kein Zustand zwischen Pixeln. Keine Reihenfolge-Annahme.")
            }
            item(visible = step >= 2) {
                Body("Kein Zugriff auf das Nachbarergebnis. Kein `printf`.")
            }
        }
    }
}

// Slide 9 — 9:00–10:00 · LIVE
val shaderPlayground by Slide(
    context = SpeakerNotes(
        """
        Diese Folie ist der Grund, warum der Vortrag in CuP und nicht in
        PowerPoint steckt — das ruhig kurz sagen.

        Vorher einen Fehler provozieren (Semikolon löschen), damit klar wird:
        es ist ein echter Compiler, kein Video.

        Dann eine Zeile ändern, z. B. sin(coord.x / 20.0) einbauen.
        """
    )
) {
    var source by remember { mutableStateOf(STARTER_SHADER) }
    val compiled = remember(source) { runCatching { RuntimeEffect.makeForShader(source) } }

    Row(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(Modifier.weight(1.15f).fillMaxHeight()) {
            Muted("Shader", size = 12)
            Spacer(Modifier.height(6.dp))
            BasicTextField(
                value = source,
                onValueChange = { source = it },
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Palette.OnBackground,
                ),
                cursorBrush = SolidColor(Palette.Accent),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Palette.CodeBackground)
                    .padding(12.dp),
            )
            Spacer(Modifier.height(8.dp))
            compiled.exceptionOrNull()?.let {
                Text(
                    text = it.message.orEmpty().lines().take(4).joinToString("\n"),
                    color = Palette.Warn,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                )
            }
        }

        Column(Modifier.weight(1f).fillMaxHeight()) {
            Muted("Ergebnis", size = 12)
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Palette.CodeBackground),
                contentAlignment = Alignment.Center,
            ) {
                compiled.fold(
                    onSuccess = { ShaderCanvas(it, Modifier.fillMaxSize()) },
                    onFailure = { Muted("Compile-Fehler") },
                )
            }
        }
    }
}

// Slide 10 — 10:00–10:45
val pipeline by Slide(
    stepCount = 2,
    context = SpeakerNotes(
        """
        Einordnung, damit Vertex-Shader nicht als Lücke im Raum stehen bleiben.
        """
    )
) { step ->
    TitledSlide("Wo der Fragment-Shader sitzt") {
        Reveal(step >= 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PipelineStage("Vertex-Shader", "Wo liegt die Geometrie?", Palette.Muted, Modifier.weight(1f))
                Arrow()
                PipelineStage("Rasterizer", "Welche Pixel sind bedeckt?", Palette.Muted, Modifier.weight(1f))
                Arrow()
                PipelineStage("Fragment-Shader", "Welche Farbe hat dieses Pixel?", Palette.Accent, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Reveal(step >= 1) {
            Body(
                "Für Bildfilter ist die Geometrie immer dasselbe Rechteck. " +
                    "Deshalb schreibst du bei AGSL und SkSL keinen Vertex-Shader — der ist schon da."
            )
        }
    }
}

@Composable
private fun PipelineStage(title: String, question: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Palette.Surface)
            .padding(14.dp),
    ) {
        Text(title, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Muted(question, size = 12)
    }
}

@Composable
private fun Arrow() {
    Text("→", color = Palette.Muted, fontSize = 20.sp)
}
