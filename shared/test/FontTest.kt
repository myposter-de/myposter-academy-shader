package de.myposter.academy.shaders

import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Typeface
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The deck stands or falls with Avenir Next.
 *
 * The platform fallback is wider. Every slide that fits in Avenir Next then
 * wraps one line more and runs off its frame. So the four faces must load, and
 * a broken file must fail here, not in front of an audience.
 */
class FontTest {

    private val faces = listOf(
        "avenir_next_regular_font.otf",
        "avenir_next_demi_font.otf",
        "avenir_next_bold_font.otf",
        "avenir_next_heavy_italic_font.otf",
    )

    @Test
    fun skiaReadsEveryFace() {
        for (name in faces) {
            val file = File("composeResources/font/$name")
            assertTrue(file.isFile, "missing: ${file.absolutePath}")
            val typeface: Typeface? =
                FontMgr.default.makeFromData(Data.makeFromBytes(file.readBytes()))
            assertNotNull(typeface, "Skia rejected $name")
            println("$name -> glyphs=${typeface.glyphsCount} bold=${typeface.isBold} italic=${typeface.isItalic}")
        }
    }
}
