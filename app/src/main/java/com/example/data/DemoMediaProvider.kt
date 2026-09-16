package com.example.data

import com.example.engine.AudioWaveformEngine
import com.example.model.*

object DemoMediaProvider {

    data class StockMedia(
        val id: String,
        val title: String,
        val category: String,
        val drawableResName: String,
        val durationMs: Long = 5000L,
        val mediaType: MediaType = MediaType.IMAGE
    )

    val STOCK_LIBRARY: List<StockMedia> = listOf(
        StockMedia(
            id = "stock_cyberpunk",
            title = "Cyberpunk Night",
            category = "Cinematic",
            drawableResName = "sample_cyberpunk",
            durationMs = 13020L
        ),
        StockMedia(
            id = "stock_mountains",
            title = "Golden Hour Highway",
            category = "Travel",
            drawableResName = "sample_mountains",
            durationMs = 10000L
        ),
        StockMedia(
            id = "stock_portrait",
            title = "Neon Studio Model",
            category = "Portrait",
            drawableResName = "sample_portrait",
            durationMs = 8000L
        ),
        StockMedia(
            id = "stock_vn_badge",
            title = "VN Studio Logo",
            category = "Graphics",
            drawableResName = "ic_vn_editor",
            durationMs = 4000L
        )
    )

    fun createInitialProjects(): List<Project> {
        // Project 1 matching the user's VN screenshot exactly:
        // Total duration: 1:03.19 (63190L), currentTime: 0:06.92 (6920L)
        // Tracks:
        // Audio Track: Timur2.5.wav (1m 3.04s), Pompeii 01.wav (59.00)
        // Text Track: "Input Title"
        // Sticker/PIP Track: ★ 3.00
        // Main Track: 13.02s filmstrip
        val p1 = Project(
            id = "proj_vn_masterpiece",
            name = "VN Studio Edit",
            aspectRatio = AspectRatio.RATIO_16_9,
            durationMs = 63190L,
            updatedAt = System.currentTimeMillis(),
            fps = 60,
            clips = listOf(
                // Track 1: Audio / Music Lane (Top Track)
                ClipModel(
                    id = "clip_audio_1",
                    name = "Timur2.5.wav",
                    trackType = TrackType.AUDIO,
                    mediaType = MediaType.AUDIO,
                    startTimeMs = 0L,
                    durationMs = 63040L,
                    volume = 0.9f,
                    waveformPeaks = AudioWaveformEngine.generatePeaks(42L, 120)
                ),
                ClipModel(
                    id = "clip_audio_2",
                    name = "Pompeii 01.wav",
                    trackType = TrackType.AUDIO,
                    mediaType = MediaType.AUDIO,
                    startTimeMs = 0L,
                    durationMs = 59000L,
                    volume = 0.75f,
                    waveformPeaks = AudioWaveformEngine.generatePeaks(99L, 100)
                ),

                // Track 2: Text / Title Lane
                ClipModel(
                    id = "clip_txt_1",
                    name = "Input Title",
                    trackType = TrackType.TEXT,
                    mediaType = MediaType.TEXT,
                    startTimeMs = 0L,
                    durationMs = 14000L,
                    textConfig = TextClipConfig(
                        text = "Input Title",
                        fontSizeSp = 40f,
                        textColorHex = 0xFFFFFFFF,
                        hasBackgroundBox = false,
                        isBold = true
                    ),
                    keyframes = listOf(
                        Keyframe("kf_txt_1", timeOffsetMs = 0L, scale = 1.0f, opacity = 1f, easing = EasingType.LINEAR),
                        Keyframe("kf_txt_2", timeOffsetMs = 6920L, scale = 1.0f, opacity = 1f, easing = EasingType.EASE_IN_OUT)
                    )
                ),

                // Track 3: Sticker / PIP Lane (Selected Clip ★ 3.00)
                ClipModel(
                    id = "clip_pip_star",
                    name = "★ 3.00",
                    trackType = TrackType.PIP,
                    mediaType = MediaType.IMAGE,
                    drawableResName = "ic_vn_editor",
                    startTimeMs = 4000L,
                    durationMs = 3000L,
                    keyframes = listOf(
                        Keyframe("kf_pip_1", timeOffsetMs = 0L, scale = 0.75f, opacity = 1f, easing = EasingType.EASE_IN_OUT),
                        Keyframe("kf_pip_2", timeOffsetMs = 3000L, scale = 1.0f, opacity = 1f, easing = EasingType.EASE_IN_OUT)
                    )
                ),
                ClipModel(
                    id = "clip_pip_star_2",
                    name = "★ 3.00",
                    trackType = TrackType.PIP,
                    mediaType = MediaType.IMAGE,
                    drawableResName = "ic_vn_editor",
                    startTimeMs = 2000L,
                    durationMs = 3000L
                ),

                // Track 4: Main Video Track Lane (Filmstrip)
                ClipModel(
                    id = "clip_main_video_1",
                    name = "Main Footage 13.02",
                    trackType = TrackType.MAIN_VIDEO,
                    mediaType = MediaType.IMAGE,
                    drawableResName = "sample_cyberpunk",
                    startTimeMs = 0L,
                    durationMs = 13020L,
                    colorGrading = ColorGradingParams(
                        presetId = "teal_orange",
                        presetIntensity = 0.85f,
                        contrast = 1.2f,
                        exposure = 0.05f,
                        vignette = 0.3f
                    ),
                    keyframes = listOf(
                        Keyframe("kf_mv_1", timeOffsetMs = 0L, scale = 1.0f, translationX = 0f, easing = EasingType.EASE_IN_OUT),
                        Keyframe("kf_mv_2", timeOffsetMs = 13020L, scale = 1.15f, translationX = -20f, easing = EasingType.EASE_IN_OUT)
                    )
                ),
                ClipModel(
                    id = "clip_main_video_2",
                    name = "Mountain Sunset",
                    trackType = TrackType.MAIN_VIDEO,
                    mediaType = MediaType.IMAGE,
                    drawableResName = "sample_mountains",
                    startTimeMs = 13020L,
                    durationMs = 12000L,
                    transitionIn = TransitionType.DISSOLVE,
                    transitionDurationMs = 600L
                )
            )
        )

        val p2 = Project(
            id = "proj_reels_9_16",
            name = "Reels & Shorts (9:16)",
            aspectRatio = AspectRatio.RATIO_9_16,
            durationMs = 15000L,
            updatedAt = System.currentTimeMillis() - 3600000L,
            fps = 30,
            clips = listOf(
                ClipModel(
                    id = "clip_reel_1",
                    name = "Fashion Portrait",
                    trackType = TrackType.MAIN_VIDEO,
                    mediaType = MediaType.IMAGE,
                    drawableResName = "sample_portrait",
                    startTimeMs = 0L,
                    durationMs = 6000L,
                    colorGrading = ColorGradingParams(presetId = "clean_studio", exposure = 0.1f)
                ),
                ClipModel(
                    id = "clip_reel_txt",
                    name = "Input Title",
                    trackType = TrackType.TEXT,
                    mediaType = MediaType.TEXT,
                    startTimeMs = 500L,
                    durationMs = 4000L,
                    textConfig = TextClipConfig(text = "SUMMER VIBES", fontSizeSp = 30f, textColorHex = 0xFFFFD166)
                ),
                ClipModel(
                    id = "clip_reel_audio",
                    name = "Midnight Synthwave.wav",
                    trackType = TrackType.AUDIO,
                    mediaType = MediaType.AUDIO,
                    startTimeMs = 0L,
                    durationMs = 15000L,
                    waveformPeaks = AudioWaveformEngine.generatePeaks(77L, 90)
                )
            )
        )

        return listOf(p1, p2)
    }
}
