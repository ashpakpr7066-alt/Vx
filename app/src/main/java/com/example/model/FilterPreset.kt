package com.example.model

data class FilterPreset(
    val id: String,
    val name: String,
    val category: String,
    val previewColorHex: Long,
    val exposure: Float = 0f,
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f,
    val brightness: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val vignette: Float = 0f
)

object FilterPresets {
    val ALL = listOf(
        FilterPreset(
            id = "teal_orange",
            name = "Teal & Orange",
            category = "Cinematic",
            previewColorHex = 0xFF0D9488,
            exposure = 0.05f,
            contrast = 1.25f,
            saturation = 1.2f,
            temperature = 0.25f,
            tint = -0.1f,
            highlights = 0.15f,
            shadows = -0.2f,
            vignette = 0.2f
        ),
        FilterPreset(
            id = "cyberpunk",
            name = "Cyberpunk Neon",
            category = "Creative",
            previewColorHex = 0xFFEC4899,
            exposure = 0.1f,
            contrast = 1.35f,
            saturation = 1.45f,
            temperature = -0.3f,
            tint = 0.45f,
            highlights = 0.2f,
            shadows = -0.3f,
            vignette = 0.35f
        ),
        FilterPreset(
            id = "kodak_vintage",
            name = "Kodak Film",
            category = "Vintage",
            previewColorHex = 0xFFD97706,
            exposure = 0.08f,
            contrast = 0.95f,
            saturation = 0.88f,
            temperature = 0.35f,
            tint = 0.08f,
            highlights = -0.1f,
            shadows = 0.25f,
            vignette = 0.25f
        ),
        FilterPreset(
            id = "moody_noir",
            name = "Moody Noir",
            category = "Monochrome",
            previewColorHex = 0xFF475569,
            exposure = -0.05f,
            contrast = 1.45f,
            saturation = 0.0f,
            temperature = 0.0f,
            tint = 0.0f,
            highlights = 0.1f,
            shadows = -0.35f,
            vignette = 0.45f
        ),
        FilterPreset(
            id = "sunset_gold",
            name = "Sunset Gold",
            category = "Warm",
            previewColorHex = 0xFFF59E0B,
            exposure = 0.12f,
            contrast = 1.15f,
            saturation = 1.3f,
            temperature = 0.55f,
            tint = 0.15f,
            highlights = 0.25f,
            shadows = -0.1f,
            vignette = 0.15f
        ),
        FilterPreset(
            id = "emerald_forest",
            name = "Emerald Forest",
            category = "Nature",
            previewColorHex = 0xFF059669,
            exposure = -0.05f,
            contrast = 1.2f,
            saturation = 1.1f,
            temperature = -0.2f,
            tint = -0.35f,
            highlights = -0.1f,
            shadows = -0.15f,
            vignette = 0.3f
        ),
        FilterPreset(
            id = "bleach_bypass",
            name = "Bleach Bypass",
            category = "Cinematic",
            previewColorHex = 0xFF64748B,
            exposure = 0.0f,
            contrast = 1.5f,
            saturation = 0.45f,
            temperature = -0.1f,
            tint = -0.05f,
            highlights = 0.3f,
            shadows = -0.2f,
            vignette = 0.3f
        ),
        FilterPreset(
            id = "clean_studio",
            name = "Studio Clean",
            category = "Portrait",
            previewColorHex = 0xFF60A5FA,
            exposure = 0.15f,
            contrast = 1.05f,
            saturation = 1.05f,
            temperature = -0.05f,
            tint = 0.05f,
            highlights = 0.1f,
            shadows = 0.1f,
            vignette = 0.0f
        )
    )

    fun getById(id: String?): FilterPreset? = ALL.find { it.id == id }
}
