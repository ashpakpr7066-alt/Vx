package com.example.model

enum class AspectRatio(
    val label: String,
    val iconName: String,
    val widthRatio: Float,
    val heightRatio: Float
) {
    RATIO_16_9("16:9", "Landscape", 16f, 9f),
    RATIO_9_16("9:16", "Story/Reels", 9f, 16f),
    RATIO_1_1("1:1", "Square", 1f, 1f),
    RATIO_4_5("4:5", "Portrait", 4f, 5f),
    RATIO_21_9("21:9", "Cinematic", 21f, 9f);

    val ratio: Float get() = widthRatio / heightRatio
}
