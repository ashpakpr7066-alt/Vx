package com.example.engine

import kotlin.math.abs
import kotlin.math.sin

object AudioWaveformEngine {

    fun generatePeaks(seed: Long, count: Int = 80): List<Float> {
        val peaks = ArrayList<Float>(count)
        val s = (seed % 1000).toFloat()
        for (i in 0 until count) {
            val t = i * 0.35f + s
            val base = (sin(t) * 0.4f + sin(t * 2.3f) * 0.3f + sin(t * 5.1f) * 0.2f)
            val beat = if (i % 8 == 0) 0.85f else if (i % 4 == 0) 0.65f else 0.3f
            val amplitude = (abs(base) * 0.7f + beat * 0.3f).coerceIn(0.12f, 0.95f)
            peaks.add(amplitude)
        }
        return peaks
    }
}
