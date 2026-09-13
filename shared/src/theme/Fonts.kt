package de.myposter.academy.shaders.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import de.myposter.academy.shaders.resources.Res
import de.myposter.academy.shaders.resources.avenir_next_bold_font
import de.myposter.academy.shaders.resources.avenir_next_demi_font
import de.myposter.academy.shaders.resources.avenir_next_heavy_italic_font
import de.myposter.academy.shaders.resources.avenir_next_regular_font
import org.jetbrains.compose.resources.FontResource
import org.jetbrains.compose.resources.getFontResourceBytes
import org.jetbrains.compose.resources.getSystemResourceEnvironment

/**
 * Avenir Next, read once, before the first slide draws.
 *
 * The composable `Font(Res.font.…)` of Compose Resources loads in the
 * background. Until the bytes arrive it hands back the platform fallback, and
 * that fallback is wider than Avenir Next. A slide that fits in the design font
 * then wraps one line more and runs off its frame. A talk must not depend on
 * that race, so the deck reads the four faces itself and builds the family from
 * the bytes. The result is synchronous: it is either the right font or nothing.
 */
suspend fun loadAvenirNext(): FontFamily {
    val env = getSystemResourceEnvironment()

    suspend fun face(
        name: String,
        resource: FontResource,
        weight: FontWeight,
        style: FontStyle = FontStyle.Normal,
    ) = Font(name, getFontResourceBytes(env, resource), weight, style)

    return FontFamily(
        face("AvenirNext-HeavyItalic", Res.font.avenir_next_heavy_italic_font, FontWeight.Black, FontStyle.Italic),
        face("AvenirNext-Bold", Res.font.avenir_next_bold_font, FontWeight.Bold),
        face("AvenirNext-Demi", Res.font.avenir_next_demi_font, FontWeight.SemiBold),
        face("AvenirNext-Regular", Res.font.avenir_next_regular_font, FontWeight.Normal),
    )
}

/**
 * The family, once loaded. Held outside the composition on purpose.
 *
 * CuP composes a slide in a composition of its own, so every slide frame builds
 * the theme again. Without this cache each of them would read the four files
 * again and flash an empty frame while it waits.
 */
private var loaded: FontFamily? = null

/** Returns the family once it is loaded, null before that. */
@Composable
fun rememberAvenirNext(): FontFamily? {
    var family by remember { mutableStateOf(loaded) }
    LaunchedEffect(Unit) {
        if (family == null) {
            val it = loadAvenirNext()
            loaded = it
            family = it
        }
    }
    return family
}
