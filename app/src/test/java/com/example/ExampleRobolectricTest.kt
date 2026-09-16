package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.ColorGradingEngine
import com.example.engine.KeyframeInterpolator
import com.example.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VN Studio", appName)
    }

    @Test
    fun `keyframe interpolation calculates between two points`() {
        val kf1 = Keyframe(id = "kf1", timeOffsetMs = 0L, scale = 1.0f, rotation = 0f, translationX = 0f, translationY = 0f, opacity = 1.0f)
        val kf2 = Keyframe(id = "kf2", timeOffsetMs = 1000L, scale = 2.0f, rotation = 90f, translationX = 100f, translationY = 200f, opacity = 0.5f, easing = EasingType.LINEAR)

        val clip = ClipModel(
            id = "c1",
            name = "Test Clip",
            trackType = TrackType.MAIN_VIDEO,
            mediaType = MediaType.IMAGE,
            startTimeMs = 0L,
            durationMs = 2000L,
            keyframes = listOf(kf1, kf2)
        )

        // At midpoint 500ms (linear interpolation)
        val state = KeyframeInterpolator.interpolate(clip, timelineTimeMs = 500L)
        assertEquals(1.5f, state.scale, 0.05f)
        assertEquals(45f, state.rotation, 1.0f)
        assertEquals(50f, state.translationX, 2.0f)
        assertEquals(100f, state.translationY, 2.0f)
        assertEquals(0.75f, state.opacity, 0.05f)
    }

    @Test
    fun `color grading engine generates color matrix filter`() {
        val params = ColorGradingParams(
            exposure = 0.2f,
            contrast = 1.2f,
            saturation = 1.3f,
            presetId = "teal_orange",
            presetIntensity = 0.8f
        )
        val filter = ColorGradingEngine.createColorFilter(params)
        assertNotNull(filter)
    }
}
