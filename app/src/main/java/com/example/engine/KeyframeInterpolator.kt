package com.example.engine

import com.example.model.ClipModel
import com.example.model.EasingType
import com.example.model.Keyframe
import com.example.model.TransformState
import com.example.model.TransitionType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object KeyframeInterpolator {

    fun interpolate(clip: ClipModel, timelineTimeMs: Long): TransformState {
        val relTimeMs = (timelineTimeMs - clip.startTimeMs).coerceAtLeast(0L)
        val keyframes = clip.keyframes.sortedBy { it.timeOffsetMs }

        if (keyframes.isEmpty()) {
            return applyTransition(clip, relTimeMs, TransformState())
        }

        val baseTransform: TransformState = when {
            relTimeMs <= keyframes.first().timeOffsetMs -> {
                val first = keyframes.first()
                TransformState(
                    scale = first.scale,
                    rotation = first.rotation,
                    translationX = first.translationX,
                    translationY = first.translationY,
                    opacity = first.opacity
                )
            }
            relTimeMs >= keyframes.last().timeOffsetMs -> {
                val last = keyframes.last()
                TransformState(
                    scale = last.scale,
                    rotation = last.rotation,
                    translationX = last.translationX,
                    translationY = last.translationY,
                    opacity = last.opacity
                )
            }
            else -> {
                var prev = keyframes.first()
                var next = keyframes.last()
                for (i in 0 until keyframes.size - 1) {
                    if (relTimeMs in keyframes[i].timeOffsetMs..keyframes[i + 1].timeOffsetMs) {
                        prev = keyframes[i]
                        next = keyframes[i + 1]
                        break
                    }
                }

                val duration = (next.timeOffsetMs - prev.timeOffsetMs).coerceAtLeast(1L)
                val rawProgress = (relTimeMs - prev.timeOffsetMs).toFloat() / duration
                val easedProgress = applyEasing(rawProgress.coerceIn(0f, 1f), prev.easing)

                TransformState(
                    scale = lerp(prev.scale, next.scale, easedProgress),
                    rotation = lerp(prev.rotation, next.rotation, easedProgress),
                    translationX = lerp(prev.translationX, next.translationX, easedProgress),
                    translationY = lerp(prev.translationY, next.translationY, easedProgress),
                    opacity = lerp(prev.opacity, next.opacity, easedProgress)
                )
            }
        }

        return applyTransition(clip, relTimeMs, baseTransform)
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }

    fun applyEasing(t: Float, easing: EasingType): Float {
        return when (easing) {
            EasingType.LINEAR -> t
            EasingType.EASE_IN -> t * t * t
            EasingType.EASE_OUT -> 1f - (1f - t) * (1f - t) * (1f - t)
            EasingType.EASE_IN_OUT -> {
                if (t < 0.5f) 4f * t * t * t
                else 1f - (-2f * t + 2f).let { it * it * it } / 2f
            }
            EasingType.BOUNCE -> {
                var p = t
                if (p < (1f / 2.75f)) {
                    7.5625f * p * p
                } else if (p < (2f / 2.75f)) {
                    p -= (1.5f / 2.75f)
                    7.5625f * p * p + 0.75f
                } else if (p < (2.5f / 2.75f)) {
                    p -= (2.25f / 2.75f)
                    7.5625f * p * p + 0.9375f
                } else {
                    p -= (2.625f / 2.75f)
                    7.5625f * p * p + 0.984375f
                }
            }
            EasingType.ELASTIC -> {
                if (t == 0f) 0f
                else if (t == 1f) 1f
                else {
                    val c4 = (2f * PI / 3f).toFloat()
                    val p = Math.pow(2.0, -10.0 * t).toFloat() * sin((t * 10f - 0.75f) * c4) + 1f
                    p.coerceIn(0f, 1.2f)
                }
            }
        }
    }

    private fun applyTransition(clip: ClipModel, relTimeMs: Long, current: TransformState): TransformState {
        if (clip.transitionIn == TransitionType.NONE || clip.transitionDurationMs <= 0L) {
            return current
        }

        if (relTimeMs >= clip.transitionDurationMs) {
            return current
        }

        val progress = (relTimeMs.toFloat() / clip.transitionDurationMs).coerceIn(0f, 1f)
        val eased = applyEasing(progress, EasingType.EASE_OUT)

        return when (clip.transitionIn) {
            TransitionType.DISSOLVE, TransitionType.FADE_BLACK -> {
                current.copy(opacity = current.opacity * eased)
            }
            TransitionType.FADE_WHITE -> {
                current.copy(opacity = current.opacity * eased)
            }
            TransitionType.SLIDE_LEFT -> {
                val offsetX = (1f - eased) * 400f
                current.copy(translationX = current.translationX + offsetX)
            }
            TransitionType.SLIDE_UP -> {
                val offsetY = (1f - eased) * 400f
                current.copy(translationY = current.translationY + offsetY)
            }
            TransitionType.ZOOM_IN -> {
                val scaleFactor = lerp(0.3f, 1f, eased)
                current.copy(
                    scale = current.scale * scaleFactor,
                    opacity = current.opacity * eased
                )
            }
            TransitionType.ZOOM_OUT -> {
                val scaleFactor = lerp(2.0f, 1f, eased)
                current.copy(
                    scale = current.scale * scaleFactor,
                    opacity = current.opacity * eased
                )
            }
            TransitionType.GLITCH -> {
                val jitter = if (progress < 0.8f && (relTimeMs / 50) % 2 == 0L) 15f else 0f
                current.copy(
                    translationX = current.translationX + jitter,
                    opacity = current.opacity * eased.coerceAtLeast(0.5f)
                )
            }
            TransitionType.NONE -> current
        }
    }
}
