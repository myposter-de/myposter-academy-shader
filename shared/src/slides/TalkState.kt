package de.myposter.academy.shaders.slides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.myposter.academy.shaders.shader.DemoImage
import de.myposter.academy.shaders.shader.DemoTimings
import de.myposter.academy.shaders.shader.formatMs
import de.myposter.academy.shaders.theme.Palette

/**
 * The state the live demo produces and later slides read.
 *
 * Slide 2 measures. Slide 3 shows the measurement as bars.
 */
object TalkState {
    val timings: DemoTimings = DemoTimings()

    /** The test image. Loaded once, when slide 2 prepares. */
    var demo: DemoImage? by mutableStateOf(null)
}

/** The measured numbers, shown on the last step of the hook demo. */
@Composable
fun TimingBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Palette.Surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Metric("CPU", formatMs(TalkState.timings.cpuMedianMs), Palette.Cpu)
        Spacer(Modifier.width(28.dp))
        Metric("GPU", formatMs(TalkState.timings.frameMedianMs), Palette.Gpu)
        Spacer(Modifier.width(28.dp))
        Metric("Bild", TalkState.demo?.let { "${it.width}×${it.height}" } ?: "—", Palette.Muted)
    }
}

@Composable
private fun Metric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Palette.Muted, fontSize = 11.sp)
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/** One horizontal bar of the comparison on slide 3. */
@Composable
fun TimingBar(
    label: String,
    ms: Double?,
    maxMs: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val fraction = if (ms == null || maxMs <= 0.0) 0f else (ms / maxMs).coerceIn(0.02, 1.0).toFloat()
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Palette.Muted, fontSize = 13.sp, modifier = Modifier.width(48.dp))
            Text(formatMs(ms), color = color, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Palette.Surface),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(16.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}
