package de.myposter.academy.shaders.slides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.CenteredBlock
import de.myposter.academy.shaders.theme.Muted
import de.myposter.academy.shaders.theme.Palette
import de.myposter.academy.shaders.theme.Reveal
import de.myposter.academy.shaders.theme.Statement
import de.myposter.academy.shaders.theme.TitledSlide
import net.kodein.cup.Slide
import net.kodein.cup.speaker.SpeakerNotes
import net.kodein.cup.widgets.material3.BulletPoints

// Slide 15 — 17:45–18:45
val preis by Slide(
    stepCount = 4,
    context = SpeakerNotes(
        """
        Ehrliche Kostenseite. Macht den Vortrag glaubwürdiger als eine reine
        Erfolgsstory.

        Step 1 löst den Spannungsbogen von Slide 3 auf: Der Readback ist der
        Grund, warum unser Byte-Loop weiterlebt.
        """
    )
) { step ->
    TitledSlide("Der Preis") {
        BulletPoints(spacedBy = 12.dp) {
            item(visible = step >= 1) {
                Column {
                    Label("Transfer")
                    Body(
                        "Lohnt sich nur, wenn das Bild ohnehin auf der GPU liegt und dort bleibt. " +
                            "Muss das Ergebnis zurück in den RAM, frisst der Readback den Gewinn.",
                        size = 14,
                    )
                }
            }
            item(visible = step >= 2) {
                Column {
                    Label("Zwei Implementierungen, eine Wahrheit")
                    Body(
                        "Eine billigere Umformulierung eines Schritts ändert, was der Kunde gedruckt bekommt. " +
                            "Absicherung: Pixelvergleichs-Tests zwischen beiden Pfaden.",
                        size = 14,
                    )
                }
            }
            item(visible = step >= 3) {
                Column {
                    Label("Debugging")
                    Body("Kein Breakpoint, kein Log. Man debuggt, indem man Zwischenergebnisse als Farbe ausgibt.", size = 14)
                }
            }
            item(visible = step >= 4) {
                Column {
                    Label("Fragmentierung")
                    Body(
                        "AGSL braucht Android 13+. Präzision (half vs. float) verhält sich zwischen Geräten " +
                            "nicht identisch. Compile-Zeit beim ersten Frame.",
                        size = 14,
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Reveal(step >= 1) {
            Statement("Das ist der Grund, warum unser Byte-Loop weiterlebt.", accent = Palette.Warn, size = 17)
        }
    }
}

@androidx.compose.runtime.Composable
private fun Label(text: String) {
    Text(text, color = Palette.Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
}

// Slide 16 — 18:45–19:15
val wannLohntEs by Slide(
    stepCount = 3,
    context = SpeakerNotes("Die Checkliste zum Mitnehmen. Alle drei müssen zutreffen.")
) { step ->
    TitledSlide("Wann es sich lohnt") {
        Muted("Alle drei müssen zutreffen.")
        Spacer(Modifier.height(20.dp))
        BulletPoints(spacedBy = 18.dp) {
            item(visible = step >= 1) {
                Body("Dieselbe simple Rechnung auf sehr vielen unabhängigen Daten", size = 19)
            }
            item(visible = step >= 2) {
                Body("Das Ergebnis soll sowieso auf den Bildschirm", size = 19)
            }
            item(visible = step >= 3) {
                Body("Es passiert interaktiv oder oft, nicht einmal im Batch", size = 19)
            }
        }
    }
}

// Slide 17 — 19:15–19:35
val takeaway by Slide(
    stepCount = 2,
    context = SpeakerNotes("Langsam vorlesen. Das ist der Satz, der bleiben soll.")
) { step ->
    CenteredBlock {
        Text(
            text = "Wenn dieselbe simple Rechnung millionenfach auf unabhängigen Daten läuft " +
                "und das Ergebnis sowieso auf den Bildschirm soll, ist die GPU nicht der exotische, " +
                "sondern der naheliegende Ort.",
            color = Palette.OnBackground,
            fontSize = 22.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(18.dp))
        Muted("Und der Einstieg kostet heute eine Funktion, nicht eine Grafik-Pipeline.", size = 16)
        Spacer(Modifier.height(28.dp))
        Reveal(step >= 1) {
            Statement(
                "Und wenn das Ergebnis gar nicht auf den Bildschirm soll, heißt das Werkzeug " +
                    "Compute statt Fragment — das Denkmuster bleibt dasselbe.",
                accent = Palette.Gpu,
                size = 17,
            )
        }
    }
}

// Slide 18
val fragen by Slide(
    context = SpeakerNotes(
        """
        Backup-Slides liegen hinter dieser Folie:
        Messmethodik, Divergenz, Präzision, Compute, TBDR, Tensor Cores, NPU.
        """
    )
) {
    CenteredBlock {
        Text("Fragen?", color = Palette.OnBackground, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(30.dp))
        Muted("Shader, Slides und Playground: (Repo-Link)", size = 15)
        Spacer(Modifier.height(8.dp))
        Muted("Diese Präsentation ist eine Compose-App — CuP, github.com/KodeinKoders/CuP", size = 15)
    }
}
