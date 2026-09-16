package com.example.engine

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import com.example.model.ColorGradingParams
import com.example.model.FilterPresets

object ColorGradingEngine {

    /**
     * Builds a hardware-accelerated ColorMatrix from advanced grading parameters.
     */
    fun createColorFilter(params: ColorGradingParams, ignorePreset: Boolean = false): ColorFilter {
        val matrix = createColorMatrix(params, ignorePreset)
        return ColorFilter.colorMatrix(matrix)
    }

    fun createColorMatrix(params: ColorGradingParams, ignorePreset: Boolean = false): ColorMatrix {
        val cm = ColorMatrix()

        // Base parameters
        var exp = params.exposure
        var con = params.contrast
        var sat = params.saturation
        var bri = params.brightness
        var temp = params.temperature
        var tint = params.tint
        var high = params.highlights
        var shad = params.shadows

        // Blend with active preset if present
        if (!ignorePreset && params.presetId != null) {
            val preset = FilterPresets.getById(params.presetId)
            if (preset != null) {
                val weight = params.presetIntensity
                exp += preset.exposure * weight
                con *= (1f + (preset.contrast - 1f) * weight)
                sat *= (1f + (preset.saturation - 1f) * weight)
                bri += preset.brightness * weight
                temp += preset.temperature * weight
                tint += preset.tint * weight
                high += preset.highlights * weight
                shad += preset.shadows * weight
            }
        }

        // Clamp parameters safely
        con = con.coerceIn(0.2f, 3.0f)
        sat = sat.coerceIn(0.0f, 3.0f)
        bri = bri.coerceIn(-1.0f, 1.0f)
        exp = exp.coerceIn(-1.5f, 1.5f)

        // 1. Exposure & Brightness & Contrast multiplier
        // R' = (R - 0.5) * contrast + 0.5 + brightness + exposure * 0.5
        val scale = con * (1f + exp * 0.5f)
        val offset = (0.5f * (1f - con) + bri + exp * 0.25f + high * 0.1f + shad * 0.1f) * 255f

        // Temperature (Red / Blue balance)
        // temp > 0: warmer (more Red, less Blue)
        // temp < 0: cooler (less Red, more Blue)
        val rTemp = 1.0f + (temp * 0.25f)
        val bTemp = 1.0f - (temp * 0.25f)

        // Tint (Green / Magenta balance)
        // tint > 0: magenta (more R & B, less G)
        // tint < 0: green (more G, less R & B)
        val gTint = 1.0f - (tint * 0.25f)
        val rTint = 1.0f + (tint * 0.12f)
        val bTint = 1.0f + (tint * 0.12f)

        val rScale = (scale * rTemp * rTint).coerceAtLeast(0f)
        val gScale = (scale * gTint).coerceAtLeast(0f)
        val bScale = (scale * bTemp * bTint).coerceAtLeast(0f)

        // Saturation coefficients (ITU-R BT.709)
        val lr = 0.2126f
        val lg = 0.7152f
        val lb = 0.0722f

        val invSat = 1.0f - sat
        val rR = (invSat * lr + sat) * rScale
        val rG = (invSat * lg) * gScale
        val rB = (invSat * lb) * bScale

        val gR = (invSat * lr) * rScale
        val gG = (invSat * lg + sat) * gScale
        val gB = (invSat * lb) * bScale

        val bR = (invSat * lr) * rScale
        val bG = (invSat * lg) * gScale
        val bB = (invSat * lb + sat) * bScale

        val values = floatArrayOf(
            rR, rG, rB, 0f, offset,
            gR, gG, gB, 0f, offset,
            bR, bG, bB, 0f, offset,
            0f, 0f, 0f, 1f, 0f
        )

        val tempMatrix = ColorMatrix(values)
        cm.set(tempMatrix)
        return cm
    }
}
