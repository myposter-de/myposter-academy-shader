package de.myposter.academy.shaders.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * The theme of the deck. MYPOSTER colors, Avenir Next, storyboard type sizes.
 *
 * The deck is light. [DarkSection] switches one slide to the dark ground —
 * title, "Compute und KI", "Zum Mitnehmen", "Fragen" and the backup divider.
 */
@Composable
fun ShaderTalkTheme(content: @Composable () -> Unit) {
    Themed(dark = false, content = content)
}

/** One slide on the dark ground. Put it inside the slide, around the whole frame. */
@Composable
fun DarkSection(content: @Composable () -> Unit) {
    Themed(dark = true, content = content)
}

@Composable
private fun Themed(dark: Boolean, content: @Composable () -> Unit) {
    // Draw nothing until Avenir Next is in. A frame of empty slide beats a frame
    // in the platform fallback, which wraps wider and breaks every layout.
    val family = rememberAvenirNext() ?: return
    val scheme = if (dark) darkScheme else lightScheme
    MaterialTheme(
        colorScheme = scheme,
        typography = deckTypography(family),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(2.dp),
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(6.dp),
            large = RoundedCornerShape(8.dp),
            extraLarge = RoundedCornerShape(12.dp),
        ),
    ) {
        CompositionLocalProvider(
            LocalDeckTokens provides if (dark) darkDeckTokens else lightDeckTokens,
            LocalContentColor provides scheme.onBackground,
            content = content,
        )
    }
}
