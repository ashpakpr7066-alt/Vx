package com.example.model

enum class EasingType(val displayName: String) {
    LINEAR("Linear"),
    EASE_IN("Ease In"),
    EASE_OUT("Ease Out"),
    EASE_IN_OUT("Ease In-Out"),
    BOUNCE("Bounce"),
    ELASTIC("Elastic")
}

data class Keyframe(
    val id: String,
    val timeOffsetMs: Long,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val opacity: Float = 1.0f,
    val easing: EasingType = EasingType.EASE_IN_OUT
) {
    fun copyWith(
        scale: Float = this.scale,
        rotation: Float = this.rotation,
        translationX: Float = this.translationX,
        translationY: Float = this.translationY,
        opacity: Float = this.opacity,
        easing: EasingType = this.easing
    ): Keyframe {
        return copy(
            scale = scale,
            rotation = rotation,
            translationX = translationX,
            translationY = translationY,
            opacity = opacity,
            easing = easing
        )
    }
}

data class TransformState(
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val opacity: Float = 1.0f
)
