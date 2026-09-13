package de.myposter.academy.shaders.slides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.myposter.academy.shaders.theme.AccentRule
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.Card
import de.myposter.academy.shaders.theme.Fill
import de.myposter.academy.shaders.theme.HeroFrame
import de.myposter.academy.shaders.theme.HeroTitle
import de.myposter.academy.shaders.theme.SectionLabel
import de.myposter.academy.shaders.theme.SlideFrame
import de.myposter.academy.shaders.theme.Step
import de.myposter.academy.shaders.theme.deck
import de.myposter.academy.shaders.theme.monoFamily
import net.kodein.cup.Slide
import net.kodein.cup.speaker.SpeakerNotes

// Slide 15 — 17:45–18:45
val preis by
    Slide(
        stepCount = 5,
        context =
            SpeakerNotes(
                """
        Ehrliche Kostenseite — macht den Vortrag glaubwürdiger als eine reine
        Erfolgsstory. Step 1 löst den Spannungsbogen von Slide 3 auf.
        """
            ),
    ) { step ->
        SlideFrame(id = "15 · preis", timing = "17:45–18:45 · 4 Steps", title = "Der Preis") {
            Fill(spacedBy = 22.dp) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    Step(step >= 1, Modifier.weight(1f)) {
                        CostCard(
                            label = "Transfer",
                            labelColor = MaterialTheme.deck.cpu,
                            text =
                                "Lohnt sich nur, wenn das Bild ohnehin auf der GPU liegt und " +
                                    "dort bleibt. Muss das Ergebnis zurück in den RAM, frisst der " +
                                    "Readback den Gewinn.",
                            conclusion = "Deshalb lebt unser Byte-Loop weiter.",
                        )
                    }
                    Step(step >= 2, Modifier.weight(1f)) {
                        CostCard(
                            label = "Doppelte Wartung",
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            text =
                                "Eine billigere Umformulierung eines Schritts ändert, was der " +
                                    "Kunde gedruckt bekommt. Absicherung: Pixelvergleichs-Tests " +
                                    "zwischen beiden Pfaden.",
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    Step(step >= 3, Modifier.weight(1f)) {
                        CostCard(
                            label = "Debugging",
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            text =
                                "Kein Breakpoint, kein Log. Man debuggt, indem man " +
                                    "Zwischenergebnisse als Farbe ausgibt.",
                        )
                    }
                    Step(step >= 4, Modifier.weight(1f)) {
                        CostCard(
                            label = "Fragmentierung",
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            text =
                                "AGSL braucht Android 13+, Präzision verhält sich zwischen " +
                                    "Geräten nicht identisch, und der erste Frame kostet Compile-Zeit.",
                        )
                    }
                }
            }
        }
    }

@Composable
private fun CostCard(label: String, labelColor: Color, text: String, conclusion: String? = null) {
    Card(Modifier.fillMaxWidth()) {
        SectionLabel(label, labelColor)
        Spacer(Modifier.height(10.dp))
        Body(text, color = MaterialTheme.colorScheme.onBackground)
        if (conclusion != null) {
            Spacer(Modifier.height(9.dp))
            Text(
                text = conclusion,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

// Slide 16 — 18:45–19:15
val wannLohntEs by
    Slide(
        stepCount = 4,
        context = SpeakerNotes("Die Checkliste zum Mitnehmen — alle drei müssen zutreffen."),
    ) { step ->
        SlideFrame(
            id = "16 · wannLohntEs",
            timing = "18:45–19:15 · 3 Steps",
            title = "Wann sich die GPU lohnt",
        ) {
            SectionLabel("Alle drei müssen zutreffen", MaterialTheme.colorScheme.outline)
            Fill(spacedBy = 28.dp) {
                Step(step >= 1) {
                    Criterion(
                        "01",
                        "Dieselbe simple Rechnung auf sehr vielen unabhängigen Daten",
                    )
                }
                Step(step >= 2) {
                    Criterion("02", "Das Ergebnis soll sowieso auf den Bildschirm")
                }
                Step(step >= 3) {
                    Criterion("03", "Es passiert interaktiv oder oft, nicht einmal im Batch")
                }
            }
        }
    }

@Composable
private fun Criterion(number: String, text: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            text = number,
            modifier = Modifier.width(80.dp).alignByBaseline(),
            style = MaterialTheme.typography.displayMedium.copy(fontFamily = monoFamily),
            color = MaterialTheme.deck.gpu,
        )
        Spacer(Modifier.width(24.dp))
        Text(
            text = text,
            modifier = Modifier.alignByBaseline(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

// Slide 17 — 19:15–19:35
val takeaway by
    Slide(
        stepCount = 2,
        context =
            SpeakerNotes("Langsam sprechen. Der zweite Step ist die Brücke zurück zu Compute."),
    ) { step ->
        HeroFrame(id = "17 · takeaway", timing = "19:15–19:35 · 2 Steps") {
            Text(
                text =
                    "Wenn dieselbe simple Rechnung millionenfach auf unabhängigen Daten " +
                        "läuft und das Ergebnis sowieso auf den Bildschirm soll, ist die GPU " +
                        "der naheliegende Ort.",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(26.dp))
            Text(
                text = "Und der Einstieg kostet heute eine Funktion, nicht eine Grafik-Pipeline.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(26.dp))
            Step(step >= 1) {
                Text(
                    text =
                        "Und wenn das Ergebnis gar nicht auf den Bildschirm soll, heißt das " +
                            "Werkzeug Compute statt Fragment. Das Denkmuster bleibt dasselbe.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.deck.teal,
                )
            }
        }
    }

// Slide 18
val fragen by
    Slide(context = SpeakerNotes("Repo-Link einblenden lassen und stehen lassen.")) {
        HeroFrame(id = "18 · fragen", timing = "1 Step") {
            HeroTitle("Fragen?")
            Spacer(Modifier.height(22.dp))
            AccentRule()
            Spacer(Modifier.height(22.dp))
            LinkLine("Shader, Slides und Playground: ", "github.com/…")
            Spacer(Modifier.height(10.dp))
            LinkLine(
                "Diese Präsentation ist eine Compose-App — CuP, ",
                "github.com/KodeinKoders/CuP",
            )
        }
    }

@Composable
private fun LinkLine(text: String, link: String) {
    Row {
        Body(text, Modifier.alignByBaseline(), color = MaterialTheme.colorScheme.onBackground)
        Text(
            text = link,
            modifier = Modifier.alignByBaseline(),
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = monoFamily,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}
