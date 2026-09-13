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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.Muted
import de.myposter.academy.shaders.theme.Palette
import de.myposter.academy.shaders.theme.Promise
import de.myposter.academy.shaders.theme.Reveal
import de.myposter.academy.shaders.theme.TitledSlide
import net.kodein.cup.PreparedSlide
import net.kodein.cup.Slide
import net.kodein.cup.sa.SourceCode
import net.kodein.cup.sa.SourceCodeThemes
import net.kodein.cup.sa.rememberSourceCode
import net.kodein.cup.speaker.SpeakerNotes
import net.kodein.cup.widgets.material3.BulletPoints

/** The eight explanations of the walkthrough, one per step. */
private data class CodeStep(val title: String, val lines: List<String>, val promise: Int? = null)

private val codeSteps = listOf(
    CodeStep(
        "Uniforms",
        listOf(
            "Uniforms sind pro Draw konstant — für alle zwölf Millionen Pixel gleich.",
            "Das ist der Kanal von der CPU zur GPU.",
        ),
    ),
    CodeStep(
        "Premultiplied Alpha",
        listOf(
            "Compose und Skia liefern premultiplied Alpha.",
            "Die Filterformeln brauchen unpremultiplied 0..255.",
            "Falsch herum: Halbtransparentes wird dunkler.",
        ),
    ),
    CodeStep(
        "step8()",
        listOf(
            "Die CPU-Variante rundet und clamped nach jedem Schritt.",
            "Wer im Float bleibt, bekommt ein anderes Bild — nicht nur ein genaueres.",
        ),
    ),
    CodeStep(
        "Brightness",
        listOf(
            "Kein Offset, sondern eine Belichtungskurve.",
            "Der Faktor hängt davon ab, wie hell das Pixel schon ist: 1 - summe/765.",
            "Helle Bereiche brennen nicht aus.",
        ),
    ),
    CodeStep(
        "Kontrast",
        listOf(
            "Kontrast ist (c - 127.5) * s + 127.5.",
            "Ausmultipliziert: eine Multiplikation und eine Addition.",
            "Der Rest sind zwei Zeilen Kotlin in toImageAdjustUniforms(), einmal pro Draw:",
            "    contrastSlope = 1f + contrast / 100f",
            "    contrastIntercept = 127.5f * (1f - slope)",
        ),
        promise = 2,
    ),
    CodeStep(
        "Saturation und Vibrance",
        listOf(
            "Saturation mischt gegen das Luma — Rec.709: Grün zählt siebenmal so viel wie Blau.",
            "Vibrance zieht jeden Kanal vom Durchschnitt weg, gewichtet mit der schon",
            "vorhandenen Buntheit. Schon bunte Pixel werden kaum angefasst.",
        ),
    ),
    CodeStep(
        "Sepia-Matrix",
        listOf(
            "float3x3 ist in AGSL und SkSL column-major.",
            "Die Konstruktor-Argumente stehen spaltenweise — die Matrix im Code ist",
            "die transponierte Lehrbuchmatrix.",
            "Wer sie „aufräumt“, vertauscht Rot und Blau.",
        ),
        promise = 3,
    ),
    CodeStep(
        "Gamma und Rückgabe",
        listOf(
            "Gamma pro Kanal, deshalb ein float3-Uniform statt drei Floats.",
            "Am Ende wieder premultiplizieren, damit Compose bekommt, was es erwartet.",
        ),
    ),
)

// Slide 11 — 10:45–14:30
val codeWalkthrough by PreparedSlide(
    stepCount = 8,
    context = SpeakerNotes(
        """
        Beim Sepia-Step darf man kurz menschlich werden — das ist der Fehler,
        den man garantiert einmal macht. Im Print-Kontext heißt „falsche Farbe"
        nicht „sieht komisch aus", sondern „Reklamation".

        Zeitpuffer: falls du hinten dran bist, Step 6 zusammenziehen.
        Step 3, 5 und 7 nicht opfern — das sind die drei Versprechen.
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
                float bunt = (max(max(c.r, c.g), c.b) - min(min(c.r, c.g), c.b)) / 255.0;
                c = step8(c + (c - float3(avg)) * (vibrance * (1.0 - bunt)));
            ${X}${sepia}    float3x3 sepiaMatrix = float3x3(
                    0.393, 0.349, 0.272,
                    0.769, 0.686, 0.534,
                    0.189, 0.168, 0.131);
                c = step8(mix(c, sepiaMatrix * c, sepia));
            ${X}${gammaOut}    c = step8(c + float3(temperature * 30.0, 0.0, temperature * -30.0));
                c = step8(pow(c / 255.0, gamma) * 255.0);
                return half4(half3(c / 255.0 * alpha), src.a);
            ${X}}
        """.trimIndent()
    }

    slideContent { step ->
        val explanation = codeSteps[step.coerceIn(codeSteps.indices)]

        Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 22.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = "Der Shader, Zeile für Zeile",
                    color = Palette.OnBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.weight(1f))
                Muted("${step + 1} / 8", size = 13)
            }
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.25f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Palette.CodeBackground)
                        .padding(14.dp),
                ) {
                    SourceCode(
                        sourceCode = sourceCode,
                        step = step,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                        ),
                        theme = SourceCodeThemes.darcula,
                    )
                }

                Column(Modifier.weight(1f).fillMaxHeight()) {
                    Text(
                        text = explanation.title,
                        color = Palette.Accent,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(10.dp))
                    explanation.lines.forEach {
                        if (it.startsWith("    ")) {
                            Text(
                                text = it.trim(),
                                color = Palette.Gpu,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 17.sp,
                            )
                        } else {
                            Body(it, size = 14)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    explanation.promise?.let {
                        Promise(it, "eingelöst", paid = true)
                    }
                }
            }
        }
    }
}

// Slide 12 — 14:30–15:00
val identitaet by Slide(
    stepCount = 2,
    context = SpeakerNotes(
        """
        Schließt den Bogen zu Slide 6.

        Kleines Detail, das gut ankommt: das `-0.0`-Problem bei Vibrance.
        Negiert wird der Int-Input, nicht der Float, weil `-0.0f` sonst die
        Equality-Prüfung und damit das Caching kaputtmacht.
        """
    )
) { step ->
    TitledSlide("Identität statt Verzweigung") {
        BulletPoints(spacedBy = 14.dp) {
            item(visible = step >= 0) {
                Body("Jedes Feld von ImageAdjustUniforms ist bei ImageAdjust-Wert 0 die Identität.")
            }
            item(visible = step >= 1) {
                Body("Ein Filter, der nichts tun soll, setzt seine Uniforms neutral und rechnet trotzdem mit.")
            }
            item(visible = step >= 1) {
                Body("Der ungenutzte Schritt kostet ein paar Instruktionen. Die Verzweigung hätte mehr gekostet.")
            }
        }
        Spacer(Modifier.weight(1f))
        Reveal(step >= 1) {
            Muted("saturation = 1.0 · slope = 1.0 · sepia = 0.0 · gamma = (1, 1, 1)", size = 14)
        }
    }
}
