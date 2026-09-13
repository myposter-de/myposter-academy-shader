package de.myposter.academy.shaders.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/*
 * The MYPOSTER palette, taken from the design system file
 * `_ds/.../colors_and_type.css`. These values are the only place in the
 * project that writes a raw color. Every widget reads them through
 * `MaterialTheme.colorScheme` or `MaterialTheme.deck`.
 */

// Brand
private val Blue = Color(0xFF00A4E0) // --mp-primary
private val BlueDark = Color(0xFF007EA6) // --mp-primary-hover
private val Green = Color(0xFF1BB450) // --mp-secondary
private val GreenDark = Color(0xFF0C9D39) // --mp-secondary-hover
private val GreenTint = Color(0x4D9BC31C) // --mp-secondary-transparent
private val Pink = Color(0xFFE5005A) // --mp-tertiary
private val PinkDark = Color(0xFFCE0453) // --mp-tertiary-hover
private val Teal = Color(0xFF06C2BB) // --mp-smashy2
private val Yellow = Color(0xFFFCBF00) // --mp-yellow

// Neutrals
private val White = Color(0xFFFFFFFF) // --mp-white
private val GreyDark = Color(0xFF3A3E44) // --mp-grey-dark
private val CoolGrey = Color(0xFF575A5F) // --mp-cool-gray
private val GreySemiDark = Color(0xFF8BACB7) // --mp-grey-semidark
private val GreyMiddle = Color(0xFFCDCDCD) // --mp-grey-middle
private val GreyLight = Color(0xFFEEEEEE) // --mp-grey-light
private val GreyExtraLight = Color(0xFFF5F5F5) // --mp-grey-extra-light
private val GreyBlue = Color(0xFFF2F8FC) // --mp-grey-blue

/** The light scheme. Most slides use it. */
internal val lightScheme = lightColorScheme(
    primary = Blue,
    onPrimary = White,
    primaryContainer = GreyBlue,
    onPrimaryContainer = GreyDark,
    secondary = Green,
    onSecondary = White,
    secondaryContainer = GreenTint,
    onSecondaryContainer = GreyDark,
    tertiary = Pink,
    onTertiary = White,
    tertiaryContainer = Pink,
    onTertiaryContainer = White,
    error = Pink,
    onError = White,
    background = White,
    onBackground = GreyDark,
    surface = White,
    onSurface = GreyDark,
    surfaceVariant = GreyExtraLight,
    onSurfaceVariant = CoolGrey,
    surfaceContainerLowest = White,
    surfaceContainerLow = GreyExtraLight,
    surfaceContainer = GreyLight,
    outline = GreySemiDark,
    outlineVariant = GreyMiddle,
    inverseSurface = GreyDark,
    inverseOnSurface = White,
)

/** The dark scheme. Title, "Compute und KI", "Zum Mitnehmen", "Fragen" and "Backup" use it. */
internal val darkScheme = darkColorScheme(
    primary = Blue,
    onPrimary = White,
    primaryContainer = BlueDark,
    onPrimaryContainer = White,
    secondary = Green,
    onSecondary = White,
    secondaryContainer = GreenDark,
    onSecondaryContainer = White,
    tertiary = Pink,
    onTertiary = White,
    tertiaryContainer = PinkDark,
    onTertiaryContainer = White,
    error = Pink,
    onError = White,
    background = GreyDark,
    onBackground = White,
    surface = GreyDark,
    onSurface = White,
    surfaceVariant = CoolGrey,
    onSurfaceVariant = White,
    surfaceContainerLowest = GreyDark,
    surfaceContainerLow = CoolGrey,
    surfaceContainer = CoolGrey,
    outline = GreySemiDark,
    outlineVariant = CoolGrey,
    inverseSurface = White,
    inverseOnSurface = GreyDark,
)

internal val lightDeckTokens = DeckTokens(
    cpu = Pink,
    gpu = Green,
    teal = Teal,
    amber = Yellow,
    panel = GreyExtraLight,
    panelInfo = GreyBlue,
    panelSuccess = GreenTint,
    idle = GreyLight,
    codeSurface = GreyDark,
    onCodeSurface = White,
    onCodeSurfaceMuted = GreySemiDark,
)

internal val darkDeckTokens = lightDeckTokens.copy(
    panel = CoolGrey,
    panelInfo = CoolGrey,
    idle = CoolGrey,
)
