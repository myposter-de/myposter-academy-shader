package de.myposter.academy.shaders.shader

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.layout.ContentScale
import kotlin.system.measureNanoTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

/**
 * The compiled filter. Compiled once, on first use.
 *
 * The first frame that uses it still costs a few hundred milliseconds: that is the driver compiling
 * for the actual GPU, not a bug.
 */
val imageAdjustEffect: RuntimeEffect by lazy { RuntimeEffect.makeForShader(IMAGE_ADJUST_SHADER) }

/** Builds the Compose render effect for one set of uniforms. */
fun imageAdjustRenderEffect(u: ImageAdjustUniforms): RenderEffect {
    val builder =
        RuntimeShaderBuilder(imageAdjustEffect).apply {
            uniform("brightness", u.brightness)
            uniform("contrastSlope", u.contrastSlope)
            uniform("contrastIntercept", u.contrastIntercept)
            uniform("saturation", u.saturation)
            uniform("vibrance", u.vibrance)
            uniform("sepia", u.sepia)
            uniform("temperature", u.temperature)
            uniform("gamma", u.gammaR, u.gammaG, u.gammaB)
        }
    return ImageFilter.makeRuntimeShader(builder, IMAGE_ADJUST_SHADER_INPUT, null as ImageFilter?)
        .asComposeRenderEffect()
}

/** The GPU path. The filter runs while the image is drawn. */
@Composable
fun ShaderAdjustedImage(
    image: ImageBitmap,
    adjust: ImageAdjust,
    modifier: Modifier = Modifier,
) {
    val uniforms = remember(adjust) { adjust.toImageAdjustUniforms() }
    val effect = remember(uniforms) { imageAdjustRenderEffect(uniforms) }
    Image(
        bitmap = image,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.graphicsLayer { renderEffect = effect },
    )
}

/**
 * The CPU path. The filter runs over the pixel array, then the result becomes a new bitmap.
 *
 * One worker runs one pass at a time and never drops a finished pass. While a pass runs, the slider
 * keeps moving and the newer values pile up. The worker then skips to the newest one. So the image
 * crawls behind the slider instead of freezing until the drag ends — that is the real lag, and the
 * audience must see it.
 *
 * @param onTiming Reports the duration of one full pass, in milliseconds.
 */
@Composable
fun CpuAdjustedImage(
    demo: DemoImage,
    adjust: ImageAdjust,
    modifier: Modifier = Modifier,
    onTiming: (Double) -> Unit = {},
) {
    var result by remember(demo) { mutableStateOf(demo.toImageBitmap()) }
    val requested = remember(demo) { mutableStateOf(adjust) }
    requested.value = adjust
    val report by rememberUpdatedState(onTiming)

    LaunchedEffect(demo) {
        val dst = ByteArray(demo.rgba.size)
        // snapshotFlow conflates: a value that arrives during a pass waits, and
        // every value before it is dropped. No cancellation, no discarded work.
        snapshotFlow { requested.value }
            .collect { target ->
                result =
                    withContext(Dispatchers.Default) {
                        val nanos = measureNanoTime { cpuImageAdjust(demo.rgba, dst, target) }
                        report(nanos / 1_000_000.0)
                        rgbaToImageBitmap(demo.width, demo.height, dst.copyOf())
                    }
            }
    }

    Image(
        bitmap = result,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

/** Draws a bare fragment shader over the whole canvas. Used by the playground. */
@Composable
fun ShaderCanvas(effect: RuntimeEffect, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply { shader = RuntimeShaderBuilder(effect).makeShader(null) }
            canvas.skiaCanvas.drawRect(Rect.makeWH(size.width, size.height), paint)
        }
    }
}
