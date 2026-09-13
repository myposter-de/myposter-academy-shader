package de.myposter.academy.shaders.slides

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.myposter.academy.shaders.shader.DemoImage
import de.myposter.academy.shaders.shader.DemoTimings
import de.myposter.academy.shaders.shader.formatMs
import de.myposter.academy.shaders.theme.deck
import de.myposter.academy.shaders.theme.monoFamily

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
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        val timings = TalkState.timings
        Metric("CPU ${formatMs(timings.cpuMedianMs)}/Durchlauf", MaterialTheme.deck.cpu)
        Metric("GPU < ${formatMs(timings.frameBudgetMs)}/Frame", MaterialTheme.deck.gpu)
        Text(
            text = TalkState.demo
                ?.let { "${it.width} × ${it.height} · CPU-Median nach Warmup" }
                ?: "CPU-Median nach Warmup",
            modifier = Modifier.alignByBaseline(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun RowScope.Metric(text: String, color: Color) {
    Text(
        text = text,
        modifier = Modifier.alignByBaseline(),
        style = MaterialTheme.typography.titleLarge,
        fontFamily = monoFamily,
        color = color,
    )
}

/** One horizontal bar of the comparison on slide 3. */
@Composable
fun TimingBar(
    label: String,
    ms: Double?,
    maxMs: Double,
    color: Color,
    valueText: String,
    modifier: Modifier = Modifier,
) {
    val fraction = if (ms == null || maxMs <= 0.0) 0f else (ms / maxMs).coerceIn(0.015, 1.0).toFloat()
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            modifier = Modifier.width(170.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .weight(fraction.coerceAtLeast(0.015f))
                .height(44.dp)
                .background(color),
            contentAlignment = Alignment.CenterEnd,
        ) {
            // A short bar has no room for its own label. It goes next to the bar instead.
            if (fraction > 0.25f) {
                Text(
                    text = valueText,
                    modifier = Modifier.padding(end = 14.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = monoFamily,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        if (fraction <= 0.25f) {
            Spacer(Modifier.width(14.dp))
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = monoFamily,
                color = color,
            )
        }
        Spacer(Modifier.weight((1f - fraction).coerceAtLeast(0.001f)))
    }
}
