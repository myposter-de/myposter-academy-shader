package de.myposter.academy.shaders.shader

/**
 * The name of the input shader of [IMAGE_ADJUST_SHADER].
 *
 * `ImageFilter.makeRuntimeShader` binds the filtered image to this uniform.
 */
const val IMAGE_ADJUST_SHADER_INPUT: String = "image"

/**
 * The image adjustment filter, in SkSL.
 *
 * The same string runs as AGSL on Android 13+ and as SkSL under Compose Desktop.
 *
 * Three rules drive this code, and slide 11 walks through all three:
 *  1. No `if` in the pixel path. A GPU runs both branches of a divergent `if`.
 *  2. Everything that is constant per draw is a uniform, not an instruction.
 *  3. `float3x3` is column-major. The constructor arguments are the transpose
 *     of the textbook matrix.
 */
// language=GLSL
val IMAGE_ADJUST_SHADER: String = """
uniform shader $IMAGE_ADJUST_SHADER_INPUT;

// One draw sets these once. All twelve million pixels read the same values.
uniform float brightness;         // identity: 0.0    (-100 .. 100)
uniform float contrastSlope;      // identity: 1.0
uniform float contrastIntercept;  // identity: 0.0
uniform float saturation;         // identity: 1.0
uniform float vibrance;           // identity: 0.0    (-1 .. 1)
uniform float sepia;              // identity: 0.0    (0 .. 1)
uniform float temperature;        // identity: 0.0    (-1 .. 1)
uniform float3 gamma;             // identity: (1, 1, 1)

// The CPU path rounds and clamps to 8 bit after every step.
// The GPU path must do the same, or the two paths produce different images.
float3 step8(float3 c) {
    return clamp(floor(c + 0.5), 0.0, 255.0);
}

half4 main(float2 coord) {
    half4 src = ${IMAGE_ADJUST_SHADER_INPUT}.eval(coord);

    // Uniform over a whole image in practice, so this branch does not diverge.
    if (src.a <= 0.0) return half4(0.0);

    float alpha = float(src.a);

    // Skia hands over premultiplied alpha. The formulas below need
    // unpremultiplied 0..255. Skip this and half-transparent pixels go dark.
    float3 c = float3(src.rgb) / alpha * 255.0;

    // 1. Brightness is an exposure curve, not an offset.
    //    A bright pixel has little headroom left, so it moves less.
    float headroom = 1.0 - (c.r + c.g + c.b) / 765.0;
    c = step8(c + brightness * headroom);

    // 2. Contrast is (c - 127.5) * slope + 127.5.
    //    Multiplied out on the CPU, it costs one multiply and one add here.
    c = step8(c * contrastSlope + contrastIntercept);

    // 3. Saturation mixes against the Rec.709 luma.
    //    Green weighs seven times as much as blue.
    float luma = dot(c, float3(0.2126, 0.7152, 0.0722));
    c = step8(mix(float3(luma), c, saturation));

    // 4. Vibrance pushes every channel away from the pixel average,
    //    weighted by how colorful the pixel already is.
    //    An already saturated pixel is nearly untouched.
    float avg = (c.r + c.g + c.b) / 3.0;
    float colorfulness = (max(max(c.r, c.g), c.b) - min(min(c.r, c.g), c.b)) / 255.0;
    c = step8(c + (c - float3(avg)) * (vibrance * (1.0 - colorfulness)));

    // 5. Sepia. float3x3 is COLUMN-MAJOR in SkSL and AGSL.
    //    These arguments are the transpose of the textbook matrix.
    //    Tidy them into row order and red and blue swap places.
    float3x3 sepiaMatrix = float3x3(
        0.393, 0.349, 0.272,   // column 0
        0.769, 0.686, 0.534,   // column 1
        0.189, 0.168, 0.131    // column 2
    );
    c = step8(mix(c, sepiaMatrix * c, sepia));

    // 6. Temperature moves red up and blue down, or the other way round.
    c = step8(c + float3(temperature * 30.0, 0.0, temperature * -30.0));

    // 7. Gamma is per channel, so the uniform is a float3, not three floats.
    c = step8(pow(c / 255.0, gamma) * 255.0);

    // Premultiply again, because that is what Compose expects back.
    float3 outColor = c / 255.0;
    return half4(half3(outColor * alpha), src.a);
}
""".trimIndent()

/**
 * The starting point of the live playground on slide 9.
 *
 * Deliberately trivial: coordinates in, color out.
 */
// language=GLSL
val STARTER_SHADER: String = """
half4 main(float2 coord) {
    return half4(coord.x / 400.0, coord.y / 400.0, 0.5, 1.0);
}
""".trimIndent()
