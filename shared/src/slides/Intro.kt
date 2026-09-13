package de.myposter.academy.shaders.slides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.myposter.academy.shaders.shader.CpuAdjustedImage
import de.myposter.academy.shaders.shader.ImageAdjust
import de.myposter.academy.shaders.shader.MeasureFrameTime
import de.myposter.academy.shaders.shader.ShaderAdjustedImage
import de.myposter.academy.shaders.shader.loadDemoImage
import de.myposter.academy.shaders.theme.Body
import de.myposter.academy.shaders.theme.CenteredBlock
import de.myposter.academy.shaders.theme.Muted
import de.myposter.academy.shaders.theme.Palette
import de.myposter.academy.shaders.theme.Reveal
import de.myposter.academy.shaders.theme.Statement
import de.myposter.academy.shaders.theme.TitledSlide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kodein.cup.PreparedSlide
import net.kodein.cup.Slide
import net.kodein.cup.speaker.SpeakerNotes
import net.kodein.cup.widgets.material3.BulletPoints

// Slide 1 — 0:00–0:20
val titel by Slide(
    context = SpeakerNotes(
        """
        Kurz halten. Nicht vorstellen, was kommt — direkt in die Demo.
        """
    )
) {
    CenteredBlock {
        Text(
            "Warum dein Bildfilter auf der falschen Hardware läuft",
            color = Palette.OnBackground,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 44.sp,
        )
        Spacer(Modifier.height(16.dp))
        Body("GPU-Shader, anhand eines echten Umbaus", size = 19)
        Spacer(Modifier.height(28.dp))
        Muted("Jannis · MYPOSTER Academy")
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
        - Median über N Läufe, nach Warmup
        - feste Bildgröße nennen
        - CPU-Zahl = eine volle Filterrunde über das Pixel-Array
        - GPU-Zahl = gemessene Zeit pro Frame, Filter inklusive

        Steps:
        0. CPU-Implementierung — ziehen, es ruckelt
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

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    demo == null -> Muted("Testbild lädt …")
                    step == 0 -> CpuAdjustedImage(
                        demo = demo,
                        adjust = adjust,
                        modifier = Modifier.fillMaxSize(),
                        onTiming = TalkState.timings::addCpu,
                    )
                    else -> {
                        val bitmap = remember(demo) { demo.toImageBitmap() }
                        ShaderAdjustedImage(bitmap, adjust, Modifier.fillMaxSize())
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (step == 0) "CPU" else "GPU",
                    color = if (step == 0) Palette.Cpu else Palette.Gpu,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(44.dp),
                )
                Slider(
                    value = brightness.toFloat(),
                    onValueChange = { brightness = it.toInt() },
                    valueRange = -100f..100f,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "$brightness",
                    color = Palette.Muted,
                    fontSize = 15.sp,
                    modifier = Modifier.width(44.dp),
                )
            }

            Reveal(step >= 2) { TimingBadge(Modifier.padding(top = 10.dp)) }
        }
    }
}

// Slide 3 — 3:00–3:40
val hookZahlen by Slide(
    stepCount = 2,
    context = SpeakerNotes(
        """
        Hier kommt die Wendung, die den Vortrag interessant macht.

        Der Shader hat die CPU-Implementierung **nicht ersetzt**.
        Alle Pfade, die das Ergebnis als Bitmap zurücklesen statt es zu zeichnen —
        PhotoImageBitmap, der Photobook-Renderer, die nativen Renderer —
        nutzen weiter den Byte-Loop.

        Wir haben jetzt zwei Implementierungen, die pixelgenau übereinstimmen müssen.
        Warum das so ist, kommt am Ende. Spannungsbogen aufmachen und stehenlassen.
        """
    )
) { step ->
    TitledSlide("Die Messung") {
        val cpu = TalkState.timings.cpuMedianMs
        val gpu = TalkState.timings.frameMedianMs
        val max = listOfNotNull(cpu, gpu).maxOrNull() ?: 1.0

        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            TimingBar("CPU", cpu, max, Palette.Cpu)
            TimingBar("GPU", gpu, max, Palette.Gpu)
        }

        Spacer(Modifier.height(10.dp))
        Muted(
            TalkState.demo?.let {
                "${it.width}×${it.height} (${"%.0f".format(it.megaPixels)} MP) · " +
                    "Median über ${TalkState.timings.cpuRuns} Läufe, nach Warmup"
            } ?: "Bildgröße: noch nicht gemessen"
        )

        Spacer(Modifier.weight(1f))

        Reveal(step >= 1) {
            Statement("Und der Byte-Loop lebt weiter.", accent = Palette.Warn)
        }
    }
}

// Slide 4 — 3:40–4:00
val agenda by Slide(
    context = SpeakerNotes("Zehn Sekunden. Nicht vorlesen.")
) {
    TitledSlide("Agenda") {
        BulletPoints {
            item { Body("Warum die GPU das kann") }
            item { Body("Was ein Shader eigentlich ist") }
            item { Body("Der echte Shader, Zeile für Zeile") }
            item { Body("Wie man selbst hinkommt") }
            item { Body("Dasselbe Prinzip, ganz anderes Feld") }
            item { Body("Wann es sich lohnt — und wann nicht") }
        }
    }
}
