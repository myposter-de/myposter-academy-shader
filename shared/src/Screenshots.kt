package de.myposter.academy.shaders

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import de.myposter.academy.shaders.theme.ShaderTalkTheme
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

/**
 * Renders every slide, at every step, into [outDir] as a PNG.
 *
 * Start the app with `-Dshadertalk.screenshots=<dir>` to use it. Nothing in the
 * deck depends on this — it exists so a layout change can be reviewed without
 * clicking through twenty slides.
 */
fun renderScreenshots(outDir: File, scale: Float = 2f) {
    outDir.deleteRecursively()
    outDir.mkdirs()

    talkSlides.slideList.forEachIndexed { index, slide ->
        for (step in 0..slide.lastStep) {
            ImageComposeScene(
                width = (960 * scale).toInt(),
                height = (540 * scale).toInt(),
                density = Density(scale),
            ) {
                ShaderTalkTheme {
                    Column(Modifier.size(960.dp, 540.dp)) {
                        // The slide body, without CuP's own frame and transitions.
                        slide.contentBuilder()(this, step)
                    }
                }
            }.let { scene ->
                try {
                    // Compose resources — the fonts — resolve on a later frame.
                    repeat(6) { scene.render(120_000_000L) }
                    val image = scene.render(120_000_000L)
                    val name = "%02d-%s-step%d.png".format(index + 1, slide.name, step)
                    File(outDir, name).writeBytes(
                        image.encodeToData(EncodedImageFormat.PNG)!!.bytes
                    )
                } finally {
                    scene.close()
                }
            }
        }
    }
    println("Wrote screenshots to ${outDir.absolutePath}")
}
