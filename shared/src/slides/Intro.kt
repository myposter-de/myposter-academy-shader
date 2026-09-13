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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.myposter.academy.shaders.shader.CpuAdjustedImage
import de.myposter.academy.shaders.shader.ImageAdjust
import de.myposter.academy.shaders.shader.MeasureFrameTime
import de.myposter.academy.shaders.shader.ShaderAdjustedImage
import de.myposter.academy.shaders.shader.formatFrames
import de.myposter.academy.shaders.shader.formatMs
import de.myposter.academy.shaders.shader.loadDemoImage
import de.myposter.academy.shaders.theme.AccentRule
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.Caption
import de.myposter.academy.shaders.theme.CenterText
import de.myposter.academy.shaders.theme.Fill
import de.myposter.academy.shaders.theme.HeroFrame
import de.myposter.academy.shaders.theme.HeroTitle
import de.myposter.academy.shaders.theme.SectionLabel
import de.myposter.academy.shaders.theme.SlideFrame
import de.myposter.academy.shaders.theme.Step
import de.myposter.academy.shaders.theme.deck
import de.myposter.academy.shaders.theme.monoFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kodein.cup.PreparedSlide
import net.kodein.cup.Slide
import net.kodein.cup.speaker.SpeakerNotes

// Slide 1 — 0:00–0:20
val titel by Slide(
    context = SpeakerNotes("Kurz halten. Nicht ankündigen, was kommt — direkt in die Demo.")
) {
    HeroFrame(id = "01 · titel", timing = "0:00–0:20 · 1 Step") {
        AccentRule()
        Spacer(Modifier.height(22.dp))
        HeroTitle("Warum dein Bildfilter auf der falschen Hardware läuft")
        Spacer(Modifier.height(22.dp))
        Text(
            text = "GPU-Shader, anhand eines echten Umbaus",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Body("Jannis · MYPOSTER Academy", color = MaterialTheme.colorScheme.outline)
    }
}

// Slide 2 — 0:20–3:00 · LIVE
val hookDemo by PreparedSlide(
    stepCount = 3,
    context = SpeakerNotes(
        """
        Nichts erklären, während du ziehst. Die Stille wirkt.
        Erst beim Zahlen-Step reden.

        Ehrlich bleiben:
        - CPU: Median über N Durchläufe, nach Warmup, feste Bildgröße nennen
        - GPU: wir messen sie nicht. vsync deckelt jede Messung auf der
          Zeichenstrecke. Deshalb steht dort das Frame-Budget, nicht die
          Filterzeit. Wenn jemand fragt: ja, der Shader ist noch schneller
          als 16,7 ms — wir können es hier nur nicht zeigen.

        Steps:
        0. CPU-Implementierung — ziehen, das Bild kriecht hinterher
        1. Shader — ziehen, es klebt am Finger
        2. Zahlen einblenden
        """
    )
) {
    // Nothing draws here. This block only prepares.
    LaunchedEffect(Unit) {
        if (TalkState.demo == null) {
            TalkState.demo = withContext(Dispatchers.Default) { loadDemoImage() }
        }
    }

    slideContent { step ->
        val demo = TalkState.demo
        var brightness by remember { mutableStateOf(0) }
        val adjust = remember(brightness) { ImageAdjust(brightness = brightness) }

        MeasureFrameTime(active = step >= 1) { TalkState.timings.addFrame(it) }

        SlideFrame(
            id = "02 · hookDemo · LIVE",
            timing = "0:20–3:00 · 3 Steps",
            title = "Derselbe Slider, zwei Implementierungen",
        ) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                DemoColumn(
                    label = "CPU · Byte-Loop",
                    accent = MaterialTheme.deck.cpu,
                    verdict = "Der Slider läuft. Das Bild hinkt hinterher.",
                    brightness = brightness,
                    onBrightness = { brightness = it },
                    modifier = Modifier.weight(1f),
                ) {
                    when (demo) {
                        null -> CenterText("Testbild lädt …", MaterialTheme.colorScheme.outline)
                        else -> CpuAdjustedImage(
                            demo = demo,
                            adjust = adjust,
                            modifier = Modifier.fillMaxSize(),
                            onTiming = TalkState.timings::addCpu,
                        )
                    }
                }

                Step(step >= 1, Modifier.weight(1f)) {
                    DemoColumn(
                        label = "GPU · Shader",
                        accent = MaterialTheme.deck.gpu,
                        verdict = "Es klebt am Finger.",
                        brightness = brightness,
                        onBrightness = { brightness = it },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        when (demo) {
                            null -> CenterText("Testbild lädt …", MaterialTheme.colorScheme.outline)
                            else -> {
                                val bitmap = remember(demo) { demo.toImageBitmap() }
                                ShaderAdjustedImage(bitmap, adjust, Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Step(step >= 2) { TimingBadge() }
        }
    }
}

/** One half of the hook demo: a label, the image, a slider and a verdict. */
@Composable
private fun DemoColumn(
    label: String,
    accent: Color,
    verdict: String,
    brightness: Int,
    onBrightness: (Int) -> Unit,
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel(label, accent)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.deck.panel),
        ) {
            image()
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = brightness.toFloat(),
                onValueChange = { onBrightness(it.toInt()) },
                valueRange = -100f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Helligkeit",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = monoFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Caption(verdict, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Slide 3 — 3:00–3:40
val hookZahlen by Slide(
    stepCount = 2,
    context = SpeakerNotes(
        """
        Hier kommt die Wendung: Der Shader hat die CPU-Implementierung nicht
        ersetzt. Alle Pfade, die das Ergebnis als Bitmap zurücklesen, nutzen
        weiter den Byte-Loop.

        Zwei Implementierungen, die pixelgenau übereinstimmen müssen.
        Warum, kommt am Ende — Spannungsbogen stehenlassen.
        """
    )
) { step ->
    SlideFrame(id = "03 · hookZahlen", timing = "3:00–3:40 · 2 Steps", title = "Die Messung") {
        val timings = TalkState.timings
        val cpu = timings.cpuMedianMs
        val budget = timings.frameBudgetMs
        val max = listOfNotNull(cpu, budget).maxOrNull() ?: 1.0

        Caption(
            TalkState.demo?.let {
                "${it.width} × ${it.height} px · CPU-Median aus ${timings.cpuRuns} Durchläufen " +
                    "nach Warmup · GPU: das Frame-Budget, denn vsync deckelt jede Messung"
            } ?: "Noch nicht gemessen. Zuerst Slide 2 zeigen"
        )

        Fill(spacedBy = 24.dp) {
            TimingBar(
                label = "CPU · Byte-Loop",
                ms = cpu,
                maxMs = max,
                color = MaterialTheme.deck.cpu,
                valueText = "${formatMs(cpu)} · ${formatFrames(timings.cpuFrames)}",
            )
            TimingBar(
                label = "GPU · Shader",
                ms = budget,
                maxMs = max,
                color = MaterialTheme.deck.gpu,
                valueText = "< ${formatMs(budget)} · 1 Frame",
            )
        }

        Step(step >= 1) {
            Text(
                text = "Und der Byte-Loop lebt weiter.",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

// Slide 4 — 3:40–4:00
val agenda by Slide(
    context = SpeakerNotes("Zehn Sekunden. Nicht vorlesen.")
) {
    SlideFrame(id = "04 · agenda", timing = "3:40–4:00 · 1 Step", title = "Agenda") {
        val items = listOf(
            "01" to "Warum die GPU das kann",
            "02" to "Was ein Shader eigentlich ist",
            "03" to "Der echte Shader, Zeile für Zeile",
            "04" to "Wie man selbst hinkommt",
            "05" to "Dasselbe Prinzip, ganz anderes Feld",
            "06" to "Wann es sich lohnt, und wann nicht",
        )
        Fill(spacedBy = 18.dp) {
            Row(
                Modifier.fillMaxWidth().fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(45.dp),
            ) {
                listOf(items.take(3), items.drop(3)).forEach { column ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
                    ) {
                        column.forEach { (number, label) -> AgendaItem(number, label) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaItem(number: String, label: String) {
    Row {
        Text(
            text = number,
            modifier = Modifier.alignByBaseline(),
            style = MaterialTheme.typography.titleLarge,
            fontFamily = monoFamily,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = label,
            modifier = Modifier.alignByBaseline(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
