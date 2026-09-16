package com.example.model

enum class TrackType(val displayName: String, val laneIndex: Int) {
    AUDIO("Music & SFX", 0),
    TEXT("Text / Subtitles", 1),
    PIP("Sticker / PIP", 2),
    MAIN_VIDEO("Main Video", 3)
}

enum class MediaType {
    VIDEO,
    IMAGE,
    TEXT,
    AUDIO,
    COLOR_SOLID
}

data class TextClipConfig(
    val text: String = "Input Title",
    val fontSizeSp: Float = 32f,
    val textColorHex: Long = 0xFFFFFFFF,
    val backgroundColorHex: Long = 0x88000000,
    val hasBackgroundBox: Boolean = false,
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val alignment: Int = 1 // 0: Left, 1: Center, 2: Right
)

data class ClipModel(
    val id: String,
    val name: String,
    val trackType: TrackType,
    val trackIndex: Int = 0,
    val mediaType: MediaType,
    val mediaUri: String? = null,
    val drawableResName: String? = null,
    val solidColorHex: Long = 0xFF1E293B,
    val startTimeMs: Long,
    val durationMs: Long,
    val sourceTrimStartMs: Long = 0L,
    val sourceTrimEndMs: Long = 0L,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val isMirrored: Boolean = false,
    val isFlipped: Boolean = false,
    val opacity: Float = 1.0f,
    val rotationAngle: Float = 0f,
    val scaleFactor: Float = 1.0f,
    val keyframes: List<Keyframe> = emptyList(),
    val colorGrading: ColorGradingParams = ColorGradingParams(),
    val transitionIn: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 600L,
    val textConfig: TextClipConfig? = null,
    val waveformPeaks: List<Float> = emptyList()
) {
    val endTimeMs: Long get() = startTimeMs + durationMs

    fun hasKeyframes(): Boolean = keyframes.isNotEmpty()

    fun findKeyframeAtOrNear(timeOffsetMs: Long, toleranceMs: Long = 100L): Keyframe? {
        return keyframes.firstOrNull { Math.abs(it.timeOffsetMs - timeOffsetMs) <= toleranceMs }
    }
}
