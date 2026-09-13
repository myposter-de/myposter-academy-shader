package de.myposter.academy.shaders.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import net.kodein.cup.widgets.material3.cupScaleDown

/** The colors of the deck. One accent per role, no decoration. */
object Palette {
    val Background = Color(0xFF12141A)
    val Surface = Color(0xFF1C2029)
    val OnBackground = Color(0xFFECEEF2)
    val Muted = Color(0xFF8C93A3)

    /** The accent of the deck. Use it for the key statement of a slide. */
    val Accent = Color(0xFFFF5A36)

    /** The CPU path. */
    val Cpu = Color(0xFFF2B233)

    /** The GPU path. */
    val Gpu = Color(0xFF3ED598)

    /** A warning or a cost. */
    val Warn = Color(0xFFFF6B81)

    val CodeBackground = Color(0xFF171A21)
}

val Mono: FontFamily = FontFamily.Monospace

@Composable
fun ShaderTalkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Palette.Accent,
            background = Palette.Background,
            surface = Palette.Surface,
            onBackground = Palette.OnBackground,
            onSurface = Palette.OnBackground,
        ),
        typography = MaterialTheme.typography.cupScaleDown(),
        content = content,
    )
}

/** The default text style of a slide body. */
val BodyStyle: TextStyle = TextStyle(color = Palette.OnBackground)
