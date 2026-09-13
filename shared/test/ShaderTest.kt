package de.myposter.academy.shaders

import de.myposter.academy.shaders.shader.IMAGE_ADJUST_SHADER
import de.myposter.academy.shaders.shader.ImageAdjust
import de.myposter.academy.shaders.shader.STARTER_SHADER
import de.myposter.academy.shaders.shader.cpuImageAdjust
import de.myposter.academy.shaders.shader.toImageAdjustUniforms
import org.jetbrains.skia.RuntimeEffect
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ShaderTest {

    @Test
    fun theImageAdjustShaderCompiles() {
        assertNotNull(RuntimeEffect.makeForShader(IMAGE_ADJUST_SHADER))
    }

    @Test
    fun theStarterShaderCompiles() {
        assertNotNull(RuntimeEffect.makeForShader(STARTER_SHADER))
    }

    @Test
    fun neutralSettingsProduceTheIdentityUniforms() {
        val u = ImageAdjust().toImageAdjustUniforms()
        assertEquals(0f, u.brightness)
        assertEquals(1f, u.contrastSlope)
        assertEquals(0f, u.contrastIntercept)
        assertEquals(1f, u.saturation)
        assertEquals(0f, u.vibrance)
        assertEquals(0f, u.sepia)
        assertEquals(0f, u.temperature)
        assertEquals(1f, u.gammaR)
        assertEquals(1f, u.gammaG)
        assertEquals(1f, u.gammaB)
    }

    @Test
    fun theCpuPathLeavesNeutralSettingsAlone() {
        val src = ByteArray(64) { (it * 3 % 256).toByte() }
        // Force a fully opaque alpha, so no pixel takes the transparent path.
        for (i in 3 until src.size step 4) src[i] = 255.toByte()
        val dst = ByteArray(src.size)
        cpuImageAdjust(src, dst, ImageAdjust())
        assertContentEquals(src, dst)
    }
}
