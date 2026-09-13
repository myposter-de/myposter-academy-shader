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

/**
 * Holds the measured numbers of the hook demo.
 *
 * We measure the CPU and we do not measure the GPU. Every clock on the drawing
 * path is capped by the refresh rate: a filter that costs 2 ms and one that
 * costs 12 ms both report 16.7 ms. That number is the monitor, not the filter.
 *
 * So the GPU side gets the frame budget instead, and the comparison becomes
 * "one pass" against "one frame". Both numbers are then true.
 */
class DemoTimings {
    private val cpu = RollingMedian()
    private val frames = RollingMedian()

    /** Duration of one full byte-loop pass over the whole image. */
    var cpuMedianMs: Double? by mutableStateOf(null)
        private set

    /** The measured frame interval, so the budget of this machine's display. */
    var frameBudgetMs: Double? by mutableStateOf(null)
        private set

    var cpuRuns: Int by mutableStateOf(0)
        private set

    /** How many frames one CPU pass costs. */
    val cpuFrames: Double?
        get() {
            val pass = cpuMedianMs ?: return null
            val budget = frameBudgetMs ?: return null
            return pass / budget
        }

    fun addCpu(ms: Double) {
        cpu.add(ms)
        cpuRuns = cpu.count
        cpuMedianMs = cpu.median()
    }

    fun addFrame(ms: Double) {
        frames.add(ms)
        frameBudgetMs = frames.median()
    }
}

/**
 * Measures the interval between drawn frames while [active] is true.
 *
 * This is the frame budget, not the cost of the filter. vsync clamps it, which
 * is exactly why we report it as a budget.
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

/** Formats a frame count for the slide. */
fun formatFrames(frames: Double?): String = when {
    frames == null -> "—"
    frames < 1.5 -> "1 Frame"
    else -> "${frames.toInt()} Frames"
}

/** Formats a duration for the slide. */
fun formatMs(ms: Double?): String = when {
    ms == null -> "—"
    ms >= 100 -> "${ms.toInt()} ms"
    ms >= 10 -> "${(ms * 10).toInt() / 10.0} ms"
    else -> "${(ms * 100).toInt() / 100.0} ms"
}
