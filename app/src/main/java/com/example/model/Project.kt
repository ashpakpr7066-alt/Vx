package com.example.model

data class Project(
    val id: String,
    val name: String,
    val aspectRatio: AspectRatio = AspectRatio.RATIO_16_9,
    val durationMs: Long = 10000L,
    val updatedAt: Long = System.currentTimeMillis(),
    val fps: Int = 30,
    val clips: List<ClipModel> = emptyList()
) {
    val maxDurationMs: Long
        get() {
            val maxClipEnd = clips.maxOfOrNull { it.endTimeMs } ?: 0L
            return Math.max(durationMs, maxClipEnd)
        }
}
