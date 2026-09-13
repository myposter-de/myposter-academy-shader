package de.myposter.academy.shaders.shader

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The byte loop. The same math as [IMAGE_ADJUST_SHADER], one pixel at a time.
 *
 * This is not dead code that the shader replaced. Every path that reads the result back as a bitmap
 * instead of drawing it still uses this loop. Both paths must agree pixel for pixel.
 *
 * @param src RGBA bytes, unpremultiplied, row major.
 * @param dst The destination buffer. Must have the same size as [src].
 */
fun cpuImageAdjust(src: ByteArray, dst: ByteArray, adjust: ImageAdjust) {
    val u = adjust.toImageAdjustUniforms()

    var i = 0
    while (i < src.size) {
        val a = src[i + 3].toInt() and 0xFF
        if (a == 0) {
            dst[i] = 0
            dst[i + 1] = 0
            dst[i + 2] = 0
            dst[i + 3] = 0
            i += 4
            continue
        }

        var r = (src[i].toInt() and 0xFF).toFloat()
        var g = (src[i + 1].toInt() and 0xFF).toFloat()
        var b = (src[i + 2].toInt() and 0xFF).toFloat()

        // 1. Brightness as an exposure curve.
        val headroom = 1f - (r + g + b) / 765f
        r = step8(r + u.brightness * headroom)
        g = step8(g + u.brightness * headroom)
        b = step8(b + u.brightness * headroom)

        // 2. Contrast.
        r = step8(r * u.contrastSlope + u.contrastIntercept)
        g = step8(g * u.contrastSlope + u.contrastIntercept)
        b = step8(b * u.contrastSlope + u.contrastIntercept)

        // 3. Saturation against the Rec.709 luma.
        val luma = 0.2126f * r + 0.7152f * g + 0.0722f * b
        r = step8(luma + (r - luma) * u.saturation)
        g = step8(luma + (g - luma) * u.saturation)
        b = step8(luma + (b - luma) * u.saturation)

        // 4. Vibrance.
        val avg = (r + g + b) / 3f
        val colorfulness = (max(max(r, g), b) - min(min(r, g), b)) / 255f
        val w = u.vibrance * (1f - colorfulness)
        r = step8(r + (r - avg) * w)
        g = step8(g + (g - avg) * w)
        b = step8(b + (b - avg) * w)

        // 5. Sepia, written the way the textbook writes it.
        val sr = 0.393f * r + 0.769f * g + 0.189f * b
        val sg = 0.349f * r + 0.686f * g + 0.168f * b
        val sb = 0.272f * r + 0.534f * g + 0.131f * b
        r = step8(r + (sr - r) * u.sepia)
        g = step8(g + (sg - g) * u.sepia)
        b = step8(b + (sb - b) * u.sepia)

        // 6. Temperature.
        r = step8(r + u.temperature * 30f)
        b = step8(b - u.temperature * 30f)

        // 7. Gamma per channel.
        r = step8((r / 255f).pow(u.gammaR) * 255f)
        g = step8((g / 255f).pow(u.gammaG) * 255f)
        b = step8((b / 255f).pow(u.gammaB) * 255f)

        dst[i] = r.toInt().toByte()
        dst[i + 1] = g.toInt().toByte()
        dst[i + 2] = b.toInt().toByte()
        dst[i + 3] = a.toByte()

        i += 4
    }
}

/** Rounds and clamps to 8 bit. The shader does exactly the same. */
private fun step8(v: Float): Float = v.roundToInt().coerceIn(0, 255).toFloat()
