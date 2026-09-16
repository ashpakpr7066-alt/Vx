package com.example.model

enum class TransitionType(val displayName: String, val defaultDurationMs: Long = 600L) {
    NONE("None", 0L),
    DISSOLVE("Dissolve", 600L),
    FADE_BLACK("Fade Black", 600L),
    FADE_WHITE("Flash White", 400L),
    SLIDE_LEFT("Slide Left", 500L),
    SLIDE_UP("Slide Up", 500L),
    ZOOM_IN("Zoom In", 500L),
    ZOOM_OUT("Zoom Out", 500L),
    GLITCH("Glitch Cut", 400L)
}
