package de.myposter.academy.shaders.slides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.Caption
import de.myposter.academy.shaders.theme.CodePanel
import de.myposter.academy.shaders.theme.Fill
import de.myposter.academy.shaders.theme.SectionLabel
import de.myposter.academy.shaders.theme.SlideFrame
import de.myposter.academy.shaders.theme.SoloStep
import de.myposter.academy.shaders.theme.Step
import de.myposter.academy.shaders.theme.deck
import de.myposter.academy.shaders.theme.monoFamily
import net.kodein.cup.PreparedSlide
import net.kodein.cup.Slide
import net.kodein.cup.sa.SourceCode
import net.kodein.cup.sa.SourceCodeThemes
import net.kodein.cup.sa.rememberSourceCode
import net.kodein.cup.speaker.SpeakerNotes

/** The eight explanations of the walkthrough, one per step. */
private data class CodeStep(val label: String, val text: String, val promise: Int? = null)

private val codeSteps = listOf(
    CodeStep(
        "Step 1 · Uniforms",
        "Pro Draw konstant, für alle zwölf Millionen Pixel gleich. " +
            "Der Kanal von CPU nach GPU.",
    ),
    CodeStep(
        "Step 2 · Alpha",
        "Compose und Skia liefern premultiplied Alpha, die Filterformeln brauchen " +
            "unpremultiplied 0..255. Falsch herum, und Halbtransparentes wird dunkler.",
    ),
    CodeStep(
        "Step 3 · step8()",
        "Die CPU-Variante rundet und clamped nach jedem Schritt. Wer im Float bleibt, " +
            "bekommt ein anderes Bild — nicht nur ein genaueres.",
    ),
    CodeStep(
        "Step 4 · Helligkeit",
        "Kein Offset, sondern eine Belichtungskurve: der Faktor hängt davon ab, wie hell " +
            "das Pixel schon ist. Helle Bereiche brennen nicht aus.",
    ),
    CodeStep(
        "Step 5 · Versprechen 2 eingelöst",
        "Kontrast ist (c − 127.5) · s + 127.5. Ausmultipliziert bleiben eine Multiplikation " +
            "und eine Addition — der Rest sind zwei Zeilen Kotlin, einmal pro Draw.",
        promise = 2,
    ),
    CodeStep(
        "Step 6 · Sättigung und Vibrance",
        "Sättigung mischt gegen das Luma — Rec.709, Grün zählt siebenmal so viel wie Blau. " +
            "Vibrance gewichtet mit dem Abstand zum Durchschnitt: schon bunte Pixel werden " +
            "kaum angefasst.",
    ),
    CodeStep(
        "Step 7 · Versprechen 3 eingelöst",
        "float3x3 ist in AGSL und SkSL column-major. Die Konstruktor-Argumente stehen " +
            "spaltenweise — im Code steht die transponierte Lehrbuchmatrix. Wer sie aufräumt, " +
            "vertauscht Rot und Blau.",
        promise = 3,
    ),
    CodeStep(
        "Step 8 · Gamma und Rückgabe",
        "Gamma pro Kanal, deshalb ein float3-Uniform statt drei Floats. Am Ende wieder " +
            "premultiplizieren, damit Compose bekommt, was es erwartet.",
    ),
)

// Slide 11 — 10:45–14:30
val codeWalkthrough by PreparedSlide(
    stepCount = 8,
    context = SpeakerNotes(
        """
        Beim Sepia-Step darf man kurz menschlich werden — das ist der Fehler,
        den man garantiert einmal macht, und im Print-Kontext heißt „falsche
        Farbe“ nicht „sieht komisch aus“, sondern „Reklamation“.

        Zeitpuffer: Step 6 zusammenziehen, aber Step 3, 5 und 7 nicht opfern —
        das sind die drei Versprechen.
        """
    )
) {
    val sourceCode = rememberSourceCode(language = "glsl") {
        val uniforms by marker(onlyShown(0))
        val unpremul by marker(onlyShown(1))
        val step8 by marker(onlyShown(2))
        val brightness by marker(onlyShown(3))
        val contrast by marker(onlyShown(4))
        val color by marker(onlyShown(5))
        val sepia by marker(onlyShown(6))
        val gammaOut by marker(onlyShown(7))

        // language=GLSL
        """
            uniform shader image;
            ${uniforms}uniform float brightness;
            uniform float contrastSlope;
            uniform float contrastIntercept;
            uniform float saturation;
            uniform float vibrance;
            uniform float sepia;
            uniform float temperature;
            uniform float3 gamma;
            ${X}${step8}float3 step8(float3 c) {
                return clamp(floor(c + 0.5), 0.0, 255.0);
            }
            ${X}
            half4 main(float2 coord) {
            ${unpremul}    half4 src = image.eval(coord);
                if (src.a <= 0.0) return half4(0.0);
                float alpha = float(src.a);
                float3 c = float3(src.rgb) / alpha * 255.0;
            ${X}${brightness}    float headroom = 1.0 - (c.r + c.g + c.b) / 765.0;
                c = step8(c + brightness * headroom);
            ${X}${contrast}    c = step8(c * contrastSlope + contrastIntercept);
            ${X}${color}    float luma = dot(c, float3(0.2126, 0.7152, 0.0722));
                c = step8(mix(float3(luma), c, saturation));

                float avg = (c.r + c.g + c.b) / 3.0;
                float bunt = (max(max(c.r, c.g), c.b)
                            - min(min(c.r, c.g), c.b)) / 255.0;
                c = step8(c + (c - float3(avg))
                    * (vibrance * (1.0 - bunt)));
            ${X}${sepia}    float3x3 sepiaMatrix = float3x3(
                    0.393, 0.349, 0.272,
                    0.769, 0.686, 0.534,
                    0.189, 0.168, 0.131);
                c = step8(mix(c, sepiaMatrix * c, sepia));
            ${X}${gammaOut}    c = step8(c + float3(temperature * 30.0, 0.0,
                                     temperature * -30.0));
                c = step8(pow(c / 255.0, gamma) * 255.0);
                return half4(half3(c / 255.0 * alpha), src.a);
            ${X}}
        """.trimIndent()
    }

    slideContent { step ->
        SlideFrame(
            id = "11 · codeWalkthrough",
            timing = "10:45–14:30 · 8 Steps",
            title = "Der Shader, Abschnitt für Abschnitt",
        ) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                CodePanel(Modifier.weight(1.15f).fillMaxHeight()) {
                    SourceCode(
                        sourceCode = sourceCode,
                        step = step,
                        // 11 sp keeps the shader's longest line inside the panel.
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = monoFamily,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                        ),
                        theme = SourceCodeThemes.darcula,
                        modifier = Modifier.weight(1f),
                    )
                    Caption(
                        "Auszug · vollständiger Shader im Repo",
                        color = MaterialTheme.deck.onCodeSurfaceMuted,
                    )
                }

                // Eight stacked explanations do not fit the frame at slide type
                // sizes. The right column shows exactly one.
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    codeSteps.forEachIndexed { index, explanation ->
                        SoloStep(step == index) {
                            Column {
                                SectionLabel(
                                    text = explanation.label,
                                    color = when (explanation.promise) {
                                        2 -> MaterialTheme.deck.gpu
                                        3 -> MaterialTheme.deck.cpu
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                )
                                Spacer(Modifier.height(12.dp))
                                Body(
                                    explanation.text,
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Slide 12 — 14:30–15:00
val identitaet by Slide(
    stepCount = 3,
    context = SpeakerNotes(
        """
        Schließt den Bogen zu Slide 6.

        Kleines Detail, das gut ankommt: das `-0.0`-Problem bei Vibrance —
        negiert wird der Int-Input, nicht der Float, weil `-0.0f` sonst die
        Equality-Prüfung und damit das Caching kaputtmacht.
        """
    )
) { step ->
    SlideFrame(
        id = "12 · identitaet",
        timing = "14:30–15:00 · 2 Steps",
        title = "Identität statt Verzweigung",
    ) {
        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(36.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Step(step >= 1, Modifier.weight(1.1f)) {
                Column {
                    Body("Jedes Feld ist bei ImageAdjust-Wert 0 die Identität.")
                    Spacer(Modifier.height(16.dp))
                    listOf(
                        "brightness" to "0.0",
                        "contrastSlope" to "1.0",
                        "saturation" to "1.0",
                        "sepia" to "0.0",
                        "gamma" to "(1.0, 1.0, 1.0)",
                    ).forEach { (field, identity) -> IdentityRow(field, identity) }
                }
            }
            Step(step >= 2, Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "Ein Filter, der nichts tun soll, rechnet trotzdem mit.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(16.dp))
                    Body(
                        "Der ungenutzte Schritt kostet ein paar Instruktionen. " +
                            "Die Verzweigung hätte mehr gekostet."
                    )
                }
            }
        }
    }
}

@Composable
private fun IdentityRow(field: String, identity: String) {
    Row(Modifier.fillMaxWidth().height(28.dp), verticalAlignment = Alignment.CenterVertically) {
        MonoCell(field, MaterialTheme.colorScheme.onBackground, Modifier.weight(1f))
        Spacer(Modifier.width(20.dp))
        MonoCell(identity, MaterialTheme.deck.gpu)
    }
}

@Composable
private fun MonoCell(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        fontFamily = monoFamily,
        color = color,
    )
}
