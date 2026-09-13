package de.myposter.academy.shaders.shader

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo

/**
 * The fixed test image of the talk. Always the same pixels, so the numbers on slide 3 are
 * reproducible.
 *
 * @property rgba Unpremultiplied RGBA bytes, row major.
 */
class DemoImage(
    val width: Int,
    val height: Int,
    val rgba: ByteArray,
) {
    val pixelCount: Int
        get() = width * height

    /** The megapixel count, for the caption under the demo. */
    val megaPixels: Double
        get() = pixelCount / 1_000_000.0

    fun toImageBitmap(): ImageBitmap = rgbaToImageBitmap(width, height, rgba)
}

/** The default demo size: 12 megapixels, the size the talk quotes. */
const val DEMO_WIDTH: Int = 4000
const val DEMO_HEIGHT: Int = 3000

/**
 * Loads the demo image.
 *
 * Set the system property `shadertalk.photo` to a JPEG or PNG path to use a real photo. Without it,
 * the image is generated, so the repository stays free of binary assets and the demo still runs
 * anywhere.
 */
fun loadDemoImage(width: Int = DEMO_WIDTH, height: Int = DEMO_HEIGHT): DemoImage {
    val path = System.getProperty("shadertalk.photo")
    if (path != null) {
        val file = File(path)
        if (file.isFile) return readPhoto(file)
        println("WARNING: shadertalk.photo=$path does not exist. Using the generated image.")
    }
    return generateDemoImage(width, height)
}

private fun readPhoto(file: File): DemoImage {
    val img: BufferedImage = ImageIO.read(file)
    val w = img.width
    val h = img.height
    val argb = img.getRGB(0, 0, w, h, null, 0, w)
    val rgba = ByteArray(w * h * 4)
    for (p in argb.indices) {
        val v = argb[p]
        val o = p * 4
        rgba[o] = ((v shr 16) and 0xFF).toByte()
        rgba[o + 1] = ((v shr 8) and 0xFF).toByte()
        rgba[o + 2] = (v and 0xFF).toByte()
        rgba[o + 3] = ((v ushr 24) and 0xFF).toByte()
    }
    return DemoImage(w, h, rgba)
}

/**
 * Generates a photo-like test image: a sky gradient, a sun, a landscape band and fine detail. It
 * covers highlights, shadows and saturated color, so the filter steps all have something to act on.
 */
private fun generateDemoImage(width: Int, height: Int): DemoImage {
    val rgba = ByteArray(width * height * 4)
    val sunX = width * 0.72
    val sunY = height * 0.24
    val sunR = height * 0.11
    val horizon = height * 0.58

    for (y in 0 until height) {
        val fy = y.toDouble() / height
        for (x in 0 until width) {
            val fx = x.toDouble() / width
            val o = (y * width + x) * 4

            var r: Double
            var g: Double
            var b: Double

            if (y < horizon) {
                // Sky: deep blue at the top, warm near the horizon.
                val t = y / horizon
                r = 40 + 195 * t * t
                g = 90 + 120 * t
                b = 190 - 60 * t * t
                val d = hypot(x - sunX, y - sunY)
                if (d < sunR * 3.2) {
                    val glow = (1.0 - d / (sunR * 3.2)).coerceIn(0.0, 1.0)
                    val core = if (d < sunR) 1.0 else 0.0
                    r += 255 * (glow * glow * 0.8 + core * 0.6)
                    g += 220 * (glow * glow * 0.6 + core * 0.6)
                    b += 120 * (glow * glow * 0.3 + core * 0.5)
                }
            } else {
                // Ground: green fading to shadow, with ridges.
                val t = (y - horizon) / (height - horizon)
                val ridge = sin(fx * PI * 9 + t * 4) * 0.5 + 0.5
                r = 30 + 70 * ridge * (1 - t)
                g = 70 + 130 * ridge * (1 - t * 0.7)
                b = 30 + 60 * (1 - ridge) * (1 - t)
            }

            // Fine detail, so the image is not a smooth gradient.
            val detail = sin(fx * 800) * sin(fy * 600) * 6
            r += detail
            g += detail
            b += detail

            rgba[o] = clamp8(r)
            rgba[o + 1] = clamp8(g)
            rgba[o + 2] = clamp8(b)
            rgba[o + 3] = 255.toByte()
        }
    }
    return DemoImage(width, height, rgba)
}

private fun clamp8(v: Double): Byte = v.toInt().coerceIn(0, 255).toByte()

/** Wraps unpremultiplied RGBA bytes into an [ImageBitmap]. */
fun rgbaToImageBitmap(width: Int, height: Int, rgba: ByteArray): ImageBitmap {
    val info = ImageInfo(width, height, ColorType.RGBA_8888, ColorAlphaType.UNPREMUL)
    return Image.makeRaster(info, rgba, width * 4).toComposeImageBitmap()
}
