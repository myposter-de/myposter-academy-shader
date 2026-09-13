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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import de.myposter.academy.shaders.shader.STARTER_SHADER
import de.myposter.academy.shaders.shader.ShaderCanvas
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.BigStat
import de.myposter.academy.shaders.theme.Caption
import de.myposter.academy.shaders.theme.Card
import de.myposter.academy.shaders.theme.CenterText
import de.myposter.academy.shaders.theme.CodePanel
import de.myposter.academy.shaders.theme.Fill
import de.myposter.academy.shaders.theme.Mono
import de.myposter.academy.shaders.theme.SectionLabel
import de.myposter.academy.shaders.theme.SlideFrame
import de.myposter.academy.shaders.theme.Step
import de.myposter.academy.shaders.theme.ThreadRow
import de.myposter.academy.shaders.theme.deck
import de.myposter.academy.shaders.theme.monoFamily
import net.kodein.cup.Slide
import net.kodein.cup.speaker.SpeakerNotes
import org.jetbrains.skia.RuntimeEffect

// Slide 5 — 4:00–5:30
val warumSchnell by Slide(
    stepCount = 5,
    context = SpeakerNotes(
        """
        Die Rechnung laut vormachen. „Pro Slider-Bewegung“ ist der Satz, der die
        Zahl vom Benchmark zur Alltagserfahrung macht.
        """
    )
) { step ->
    SlideFrame(
        id = "05 · warumSchnell",
        timing = "4:00–5:30 · 4 Steps",
        title = "Die Größenordnung",
    ) {
        Fill(spacedBy = 20.dp) {
            Step(step >= 1) {
                BigStat("12.000.000", "Pixel in einem 12-MP-Foto")
            }
            Step(step >= 2) {
                BigStat(
                    value = "300 Mio.",
                    text = "Rechenoperationen — × 8 Filterschritte × 3 Kanäle. " +
                        "Pro Slider-Bewegung. Und ein Slider erzeugt 60 davon pro Sekunde.",
                    valueColor = MaterialTheme.deck.cpu,
                )
            }
            Step(step >= 3) {
                HardwareRow(
                    "CPU",
                    "Einige Kerne, seriell, mit SIMD vielleicht 8 Werte gleichzeitig",
                    MaterialTheme.colorScheme.onBackground,
                )
            }
            Step(step >= 4) {
                HardwareRow(
                    "GPU",
                    "Hunderte bis tausende Recheneinheiten, die nichts anderes tun " +
                        "als genau diese Art Arithmetik",
                    MaterialTheme.deck.gpu,
                )
            }
        }
    }
}

@Composable
private fun HardwareRow(label: String, text: String, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            modifier = Modifier.width(310.dp),
            style = MaterialTheme.typography.headlineMedium,
            color = color,
        )
        Spacer(Modifier.width(20.dp))
        Body(text)
    }
}

// Slide 6 — 5:30–7:00
val simt by Slide(
    stepCount = 4,
    context = SpeakerNotes(
        """
        Der Divergenz-Punkt ist das, was die meisten nicht wissen und am ehesten
        hängenbleibt.

        Q&A-Munition: das eine `if` im Shader (`alpha <= 0`) ist unkritisch,
        weil es für ein ganzes Bild fast immer einheitlich ausfällt — Divergenz
        kostet nur innerhalb einer Gruppe.
        """
    )
) { step ->
    SlideFrame(
        id = "06 · simt",
        timing = "5:30–7:00 · 3 Steps",
        title = "SIMT: eine Instruktion, viele Pixel",
    ) {
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(36.dp),
        ) {
            Step(step >= 1, Modifier.weight(1f)) {
                Column(Modifier.fillMaxHeight()) {
                    Body(
                        "Die GPU führt Pixel in Gruppen aus — 32 oder 64, je nach Hersteller. " +
                            "Alle Threads einer Gruppe führen dieselbe Instruktion aus, " +
                            "auf verschiedenen Daten.",
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(4) { ThreadRow(List(8) { true }) }
                    }
                    Spacer(Modifier.height(14.dp))
                    Caption("Eine Gruppe · 32 Threads · ein Programmzähler")
                }
            }

            Step(step >= 2, Modifier.weight(1f)) {
                Column(Modifier.fillMaxHeight()) {
                    Body(
                        "Ein if, bei dem die Gruppe sich aufteilt: die GPU läuft beide " +
                            "Zweige, jeweils mit der Hälfte deaktiviert.",
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(20.dp))
                    val branchA = listOf(true, false, true, false, false, true, false, true)
                    BranchRow("Zweig A", branchA)
                    Spacer(Modifier.height(6.dp))
                    BranchRow("Zweig B", branchA.map { !it })
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Kosten: beide Zweige. Nutzen: einer.",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.deck.cpu,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Step(step >= 3) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Versprechen 1")
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Deshalb hat mein Shader kein einziges if im Pixelpfad.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
private fun BranchRow(label: String, pattern: List<Boolean>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            modifier = Modifier.width(70.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = monoFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ThreadRow(pattern)
    }
}

// Slide 7 — 7:00–7:45
val kostenmodell by Slide(
    stepCount = 3,
    context = SpeakerNotes(
        """
        Zwei Versprechen aufmachen und stehenlassen — eingelöst werden sie im
        Code-Walkthrough, Step 5 und Step 7.
        """
    )
) { step ->
    SlideFrame(
        id = "07 · kostenmodell",
        timing = "7:00–7:45 · 2 Steps",
        title = "Das Kostenmodell",
    ) {
        Fill {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Step(step >= 1, Modifier.weight(1f)) {
                    PromiseCard(
                        label = "Versprechen 2",
                        labelColor = MaterialTheme.colorScheme.primary,
                        background = MaterialTheme.deck.panelInfo,
                        headline = "Zwei Zeilen Kotlin ersetzen 12 Millionen Divisionen.",
                        body = "Was einmal pro Draw gerechnet werden kann, " +
                            "gehört nicht in den Pixelcode.",
                    )
                }
                Step(step >= 2, Modifier.weight(1f)) {
                    PromiseCard(
                        label = "Versprechen 3",
                        labelColor = MaterialTheme.deck.cpu,
                        background = MaterialTheme.deck.panel,
                        headline = "Eine falsch gelesene Matrix, und das Foto kommt " +
                            "falsch aus dem Drucker.",
                        body = "Der Shader ist eine eigene Sprache mit eigenen Konventionen.",
                    )
                }
            }
        }
    }
}

@Composable
private fun PromiseCard(
    label: String,
    labelColor: Color,
    background: Color,
    headline: String,
    body: String,
) {
    Card(Modifier.fillMaxWidth(), background = background) {
        SectionLabel(label, labelColor)
        Spacer(Modifier.height(14.dp))
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))
        Caption(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Slide 8 — 7:45–9:00
val wasIstEinShader by Slide(
    stepCount = 4,
    context = SpeakerNotes(
        """
        „Du schreibst den Schleifenkörper und gibst die Kontrolle über das Wann
        ab“ ist der Satz, den die Leute mitnehmen sollen. Wiederhole ihn.
        """
    )
) { step ->
    SlideFrame(
        id = "08 · wasIstEinShader",
        timing = "7:45–9:00 · 3 Steps",
        title = "Was ein Shader ist",
    ) {
        Fill(spacedBy = 26.dp) {
            Step(step >= 1) {
                Column {
                    SectionLabel("Eine reine Funktion", MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "float2 coord → half4 color",
                        style = MaterialTheme.typography.displaySmall.copy(fontFamily = monoFamily),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Step(step >= 2) {
                Text(
                    text = "Du schreibst nicht die Schleife. Du schreibst den " +
                        "Schleifenkörper — wann und wie oft er läuft, entscheidet das System.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Step(step >= 3) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    listOf(
                        "Kein Zustand zwischen Pixeln",
                        "Keine Reihenfolge-Annahme",
                        "Kein Nachbarergebnis",
                        "Kein printf",
                    ).forEach { Body(it) }
                }
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
        """
    )
) {
    var source by remember { mutableStateOf(STARTER_SHADER) }
    val compiled = remember(source) { runCatching { RuntimeEffect.makeForShader(source) } }

    SlideFrame(
        id = "09 · shaderPlayground · LIVE",
        timing = "9:00–10:00 · 1 Step",
        title = "Shader-Playground",
    ) {
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Column(
                modifier = Modifier.weight(1.15f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CodePanel(Modifier.weight(1f).fillMaxWidth()) {
                    BasicTextField(
                        value = source,
                        onValueChange = { source = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = monoFamily,
                            color = MaterialTheme.deck.onCodeSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Mono(
                    text = compiled.exceptionOrNull()
                        ?.message.orEmpty().lines().firstOrNull().orEmpty(),
                    color = MaterialTheme.deck.cpu,
                )
                Caption(
                    "Editierbares Textfeld, Neukompilierung bei jeder Änderung. " +
                        "Live eine Zeile ändern: sin(coord.x / 20.0).",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(MaterialTheme.deck.cornerM))
                        .background(MaterialTheme.deck.panel),
                ) {
                    compiled.fold(
                        onSuccess = { ShaderCanvas(it, Modifier.fillMaxSize()) },
                        onFailure = { CenterText("Compile-Fehler", MaterialTheme.deck.cpu) },
                    )
                }
                Caption(
                    "Das Ergebnis, gerendert in derselben App.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Slide 10 — 10:00–10:45
val pipeline by Slide(
    stepCount = 3,
    context = SpeakerNotes(
        "Einordnung, damit Vertex-Shader nicht als Lücke im Raum stehen bleiben."
    )
) { step ->
    SlideFrame(
        id = "10 · pipeline",
        timing = "10:00–10:45 · 2 Steps",
        title = "Die Grafik-Pipeline",
    ) {
        Step(step >= 1) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                PipelineStage(
                    "Vertex-Shader",
                    "Wo liegt die Geometrie?",
                    MaterialTheme.deck.panel,
                    MaterialTheme.colorScheme.onBackground,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    Modifier.weight(1f),
                )
                Connector()
                PipelineStage(
                    "Rasterizer",
                    "Welche Pixel sind bedeckt?",
                    MaterialTheme.deck.panel,
                    MaterialTheme.colorScheme.onBackground,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    Modifier.weight(1f),
                )
                Connector()
                PipelineStage(
                    "Fragment-Shader",
                    "Welche Farbe hat dieses Pixel?",
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.onPrimary,
                    MaterialTheme.colorScheme.onPrimary,
                    Modifier.weight(1.15f),
                )
            }
        }

        Fill {
            Step(step >= 2) {
                Text(
                    text = "Für Bildfilter ist die Geometrie immer dasselbe Rechteck. " +
                        "Deshalb schreibst du bei AGSL und SkSL keinen Vertex-Shader — " +
                        "der ist schon da.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
private fun PipelineStage(
    title: String,
    question: String,
    background: Color,
    titleColor: Color,
    questionColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(MaterialTheme.deck.cornerM))
            .background(background)
            .padding(20.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = titleColor)
        Spacer(Modifier.height(9.dp))
        Caption(question, color = questionColor)
    }
}

@Composable
private fun Connector() {
    Box(
        Modifier
            .width(28.dp)
            .height(2.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
