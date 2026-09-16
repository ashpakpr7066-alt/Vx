package com.example.model

data class CurvePoint(val x: Float, val y: Float)

data class RgbCurves(
    val master: List<CurvePoint> = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f)),
    val red: List<CurvePoint> = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f)),
    val green: List<CurvePoint> = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f)),
    val blue: List<CurvePoint> = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f))
)

data class ColorGradingParams(
    val exposure: Float = 0f,          // -1.0 to 1.0
    val contrast: Float = 1.0f,        // 0.5 to 1.8
    val saturation: Float = 1.0f,      // 0.0 to 2.0
    val brightness: Float = 0f,        // -0.5 to 0.5
    val temperature: Float = 0f,       // -1.0 (Cool/Blue) to 1.0 (Warm/Orange)
    val tint: Float = 0f,              // -1.0 (Green) to 1.0 (Magenta)
    val highlights: Float = 0f,        // -1.0 to 1.0
    val shadows: Float = 0f,           // -1.0 to 1.0
    val vignette: Float = 0f,          // 0.0 to 1.0
    val sharpness: Float = 0f,         // 0.0 to 1.0
    val hueShift: Float = 0f,          // -180 to 180
    val presetId: String? = null,
    val presetIntensity: Float = 1.0f, // 0.0 to 1.0
    val curves: RgbCurves = RgbCurves()
) {
    val isDefault: Boolean
        get() = exposure == 0f &&
                contrast == 1.0f &&
                saturation == 1.0f &&
                brightness == 0f &&
                temperature == 0f &&
                tint == 0f &&
                highlights == 0f &&
                shadows == 0f &&
                vignette == 0f &&
                sharpness == 0f &&
                hueShift == 0f &&
                presetId == null
}
