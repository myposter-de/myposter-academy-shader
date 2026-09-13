package de.myposter.academy.shaders.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import de.myposter.academy.shaders.resources.Res
import de.myposter.academy.shaders.resources.avenir_next_bold_font
import de.myposter.academy.shaders.resources.avenir_next_demi_font
import de.myposter.academy.shaders.resources.avenir_next_heavy_italic_font
import de.myposter.academy.shaders.resources.avenir_next_regular_font
import org.jetbrains.compose.resources.Font

/**
 * The sizes below are the storyboard values divided by two.
 *
 * A CuP slide is 640 × 360 dp and the storyboard is 1920 × 1080 px, so the
 * deck runs at a 960 × 540 dp slide size. See `Presentation.kt`.
 */
@Composable
fun avenirNext(): FontFamily = FontFamily(
    Font(Res.font.avenir_next_heavy_italic_font, FontWeight.Black, FontStyle.Italic),
    Font(Res.font.avenir_next_bold_font, FontWeight.Bold),
    Font(Res.font.avenir_next_demi_font, FontWeight.SemiBold),
    Font(Res.font.avenir_next_regular_font, FontWeight.Normal),
)

/** No Roboto Mono is licensed for the desktop app, so the platform monospace stands in. */
val monoFamily: FontFamily = FontFamily.Monospace

@Composable
fun deckTypography(): Typography {
    val base = TextStyle.Default.copy(
        fontFamily = avenirNext(),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        ),
    )
    val bold = base.copy(fontWeight = FontWeight.Bold)
    val demi = base.copy(fontWeight = FontWeight.SemiBold)

    return Typography(
        // --t-hero, in Avenir Next Heavy Italic. Title, "Fragen?" and "Backup".
        displayLarge = base.copy(
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            fontSize = 59.sp,
            lineHeight = 70.sp,
            letterSpacing = 0.25.sp,
        ),
        // --t-huge. The two numbers on "Die Größenordnung" and the checklist.
        displayMedium = bold.copy(fontSize = 52.sp, lineHeight = 52.sp, letterSpacing = (-1).sp),
        // The takeaway sentence.
        displaySmall = bold.copy(fontSize = 29.sp, lineHeight = 36.sp),
        // --t-title. The heading of every content slide.
        headlineLarge = bold.copy(fontSize = 32.sp, lineHeight = 35.sp, letterSpacing = (-0.25).sp),
        // --t-sub. A lead sentence or a card headline.
        headlineMedium = bold.copy(fontSize = 22.sp, lineHeight = 28.sp),
        headlineSmall = demi.copy(fontSize = 22.sp, lineHeight = 29.sp),
        // --t-body.
        titleLarge = bold.copy(fontSize = 17.sp, lineHeight = 24.sp),
        titleMedium = demi.copy(fontSize = 17.sp, lineHeight = 24.sp),
        bodyLarge = base.copy(fontSize = 17.sp, lineHeight = 24.sp),
        // --t-small.
        bodyMedium = base.copy(fontSize = 14.sp, lineHeight = 20.sp),
        // --t-meta. The slide id and the timing.
        bodySmall = base.copy(fontSize = 12.sp, lineHeight = 16.sp),
        // The uppercase section label above a card.
        labelLarge = bold.copy(fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.25.sp),
        labelMedium = bold.copy(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.25.sp),
        labelSmall = demi.copy(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.25.sp),
    )
}
