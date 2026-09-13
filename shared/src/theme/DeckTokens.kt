package de.myposter.academy.shaders.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The tokens the deck needs and Material 3 has no slot for.
 *
 * Read them as `MaterialTheme.deck.…`, never as a literal in a slide.
 */
@Immutable
data class DeckTokens(
    /** The CPU path. Always the same color, on every slide. */
    val cpu: Color,
    /** The GPU path. */
    val gpu: Color,
    /** The Compute and KI section. */
    val teal: Color,
    /** A back-reference on the Compute and KI slide. */
    val amber: Color,
    /** A neutral card. */
    val panel: Color,
    /** A card that carries a promise. */
    val panelInfo: Color,
    /** A card that marks the level the talk lives on. */
    val panelSuccess: Color,
    /** A thread that the GPU switched off. */
    val idle: Color,
    /** The background of a code block. */
    val codeSurface: Color,
    val onCodeSurface: Color,
    val onCodeSurfaceMuted: Color,
) {
    /** The side padding of every slide. 100 px of the 1920 px storyboard. */
    val padH: Dp = 50.dp

    /** The top padding of every slide. 72 px of the 1080 px storyboard. */
    val padTop: Dp = 36.dp

    /** The bottom padding of every slide. 80 px of the 1080 px storyboard. */
    val padBottom: Dp = 40.dp

    /** The gap between the title and the body. */
    val gapTitle: Dp = 26.dp

    /** The opacity of a step that the speaker has not reached yet. */
    val dim: Float = 0.14f

    /** The width of the accent rule under a hero title. */
    val ruleWidth: Dp = 42.dp

    /** The height of the accent rule under a hero title. */
    val ruleHeight: Dp = 4.dp

    /** The corner of a code block or a pipeline stage. 12 px of the storyboard. */
    val cornerM: Dp = 6.dp

    /** The corner of a large card. 16 px of the storyboard. */
    val cornerL: Dp = 8.dp
}

internal val LocalDeckTokens = staticCompositionLocalOf { lightDeckTokens }

/** The deck tokens of the current theme. */
val MaterialTheme.deck: DeckTokens
    @Composable @ReadOnlyComposable get() = LocalDeckTokens.current
