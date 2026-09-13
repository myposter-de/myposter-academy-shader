package de.myposter.academy.shaders.shader

import kotlin.math.pow

/**
 * The user facing filter settings. Every field is neutral at 0.
 *
 * The talk uses only [brightness], but the other fields keep the shader and the CPU path honest:
 * both must handle the full set.
 */
data class ImageAdjust(
    val brightness: Int = 0, // -100 .. 100
    val contrast: Int = 0, // -100 .. 100
    val saturation: Int = 0, // -100 .. 100
    val vibrance: Int = 0, // -100 .. 100
    val sepia: Int = 0, // 0 .. 100
    val temperature: Int = 0, // -100 .. 100
    val gamma: Int = 0, // -100 .. 100
)

/**
 * The values the shader actually reads.
 *
 * Every field is the identity when the matching [ImageAdjust] field is 0. That is why the shader
 * needs no branch: a step that must do nothing still runs, with neutral uniforms.
 */
data class ImageAdjustUniforms(
    val brightness: Float,
    val contrastSlope: Float,
    val contrastIntercept: Float,
    val saturation: Float,
    val vibrance: Float,
    val sepia: Float,
    val temperature: Float,
    val gammaR: Float,
    val gammaG: Float,
    val gammaB: Float,
)

/**
 * Turns the settings into uniforms. This runs once per draw.
 *
 * The contrast lines are promise 2 of the talk: `(c - 127.5) * s + 127.5` multiplied out here costs
 * two lines of Kotlin, instead of twelve million subtractions and additions in the pixel path.
 */
fun ImageAdjust.toImageAdjustUniforms(): ImageAdjustUniforms {
    val slope = contrastSlope(contrast)

    // Negate the Int, not the Float. -0.0f breaks equality, and equality
    // is what keeps `remember` from rebuilding the uniforms every frame.
    val vibranceValue = if (vibrance == 0) 0f else vibrance / 100f

    val gammaValue = 2f.pow(-gamma / 100f)

    return ImageAdjustUniforms(
        brightness = brightness.toFloat(),
        contrastSlope = slope,
        contrastIntercept = 127.5f * (1f - slope),
        saturation = 1f + saturation / 100f,
        vibrance = vibranceValue,
        sepia = sepia / 100f,
        temperature = temperature / 100f,
        gammaR = gammaValue,
        gammaG = gammaValue,
        gammaB = gammaValue,
    )
}

/** Maps -100..100 to a contrast slope of 0..2, with 1.0 at 0. */
private fun contrastSlope(contrast: Int): Float = 1f + contrast / 100f
