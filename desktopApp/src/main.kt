package de.myposter.academy.shaders

import java.io.File
import net.kodein.cup.cupApplication

fun main() {
    // -Dshadertalk.screenshots=<dir> renders every slide and exits.
    val screenshots = System.getProperty("shadertalk.screenshots")
    if (screenshots != null) {
        renderScreenshots(File(screenshots))
        return
    }
    cupApplication(title = "GPU-Shader — MYPOSTER Academy") {
        ShaderTalk()
    }
}
