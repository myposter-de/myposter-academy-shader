package de.myposter.academy.shaders.shader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

/**
 * A rolling median over the last [capacity] samples.
 *
 * The median, not the mean: one slow sample from a garbage collection must not
 * decide the number on the slide.
 */
class RollingMedian(private val capacity: Int = 31) {
    private val samples = ArrayDeque<Double>()

    val count: Int get() = samples.size

    fun add(value: Double) {
        samples.addLast(value)
        while (samples.size > capacity) samples.removeFirst()
    }

    fun median(): Double? {
        if (samples.isEmpty()) return null
        val sorted = samples.sorted()
        return sorted[sorted.size / 2]
    }

    fun clear() = samples.clear()
}

/** Holds the measured numbers of the hook demo. */
class DemoTimings {
    private val cpu = RollingMedian()
    private val frames = RollingMedian()

    var cpuMedianMs: Double? by mutableStateOf(null)
        private set
    var frameMedianMs: Double? by mutableStateOf(null)
        private set
    var cpuRuns: Int by mutableStateOf(0)
        private set

    /** Drops the first samples, so a cold shader compile does not count. */
    private var warmup = 3

    fun addCpu(ms: Double) {
        cpu.add(ms)
        cpuRuns = cpu.count
        cpuMedianMs = cpu.median()
    }

    fun addFrame(ms: Double) {
        if (warmup > 0) {
            warmup--
            return
        }
        frames.add(ms)
        frameMedianMs = frames.median()
    }

    fun resetFrames() {
        frames.clear()
        warmup = 3
        frameMedianMs = null
    }
}

/**
 * Measures the interval between drawn frames while [active] is true.
 *
 * This is the honest GPU number: how long one full frame takes, filter
 * included, on this machine, at this image size.
 */
@Composable
fun MeasureFrameTime(active: Boolean, onFrame: (Double) -> Unit) {
    LaunchedEffect(active) {
        if (!active) return@LaunchedEffect
        var last = 0L
        while (true) {
            val now = withFrameNanos { it }
            if (last != 0L) onFrame((now - last) / 1_000_000.0)
            last = now
        }
    }
}

/** Formats a duration for the slide. */
fun formatMs(ms: Double?): String = when {
    ms == null -> "—"
    ms >= 100 -> "${ms.toInt()} ms"
    ms >= 10 -> "${(ms * 10).toInt() / 10.0} ms"
    else -> "${(ms * 100).toInt() / 100.0} ms"
}
