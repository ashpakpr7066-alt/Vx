package com.example.data

import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

object ProjectJsonAdapter {

    fun clipsToJson(clips: List<ClipModel>): String {
        val array = JSONArray()
        for (clip in clips) {
            val obj = JSONObject().apply {
                put("id", clip.id)
                put("name", clip.name)
                put("trackType", clip.trackType.name)
                put("trackIndex", clip.trackIndex)
                put("mediaType", clip.mediaType.name)
                put("mediaUri", clip.mediaUri ?: "")
                put("drawableResName", clip.drawableResName ?: "")
                put("solidColorHex", clip.solidColorHex)
                put("startTimeMs", clip.startTimeMs)
                put("durationMs", clip.durationMs)
                put("sourceTrimStartMs", clip.sourceTrimStartMs)
                put("sourceTrimEndMs", clip.sourceTrimEndMs)
                put("speed", clip.speed.toDouble())
                put("volume", clip.volume.toDouble())
                put("isMuted", clip.isMuted)
                put("transitionIn", clip.transitionIn.name)
                put("transitionDurationMs", clip.transitionDurationMs)

                // Keyframes array
                val kfArray = JSONArray()
                for (kf in clip.keyframes) {
                    val kfObj = JSONObject().apply {
                        put("id", kf.id)
                        put("timeOffsetMs", kf.timeOffsetMs)
                        put("scale", kf.scale.toDouble())
                        put("rotation", kf.rotation.toDouble())
                        put("translationX", kf.translationX.toDouble())
                        put("translationY", kf.translationY.toDouble())
                        put("opacity", kf.opacity.toDouble())
                        put("easing", kf.easing.name)
                    }
                    kfArray.put(kfObj)
                }
                put("keyframes", kfArray)

                // Color grading
                val cg = clip.colorGrading
                val cgObj = JSONObject().apply {
                    put("exposure", cg.exposure.toDouble())
                    put("contrast", cg.contrast.toDouble())
                    put("saturation", cg.saturation.toDouble())
                    put("brightness", cg.brightness.toDouble())
                    put("temperature", cg.temperature.toDouble())
                    put("tint", cg.tint.toDouble())
                    put("highlights", cg.highlights.toDouble())
                    put("shadows", cg.shadows.toDouble())
                    put("vignette", cg.vignette.toDouble())
                    put("sharpness", cg.sharpness.toDouble())
                    put("hueShift", cg.hueShift.toDouble())
                    put("presetId", cg.presetId ?: "")
                    put("presetIntensity", cg.presetIntensity.toDouble())
                }
                put("colorGrading", cgObj)

                // Text config
                clip.textConfig?.let { tc ->
                    val tcObj = JSONObject().apply {
                        put("text", tc.text)
                        put("fontSizeSp", tc.fontSizeSp.toDouble())
                        put("textColorHex", tc.textColorHex)
                        put("backgroundColorHex", tc.backgroundColorHex)
                        put("hasBackgroundBox", tc.hasBackgroundBox)
                        put("isBold", tc.isBold)
                        put("isItalic", tc.isItalic)
                        put("alignment", tc.alignment)
                    }
                    put("textConfig", tcObj)
                }
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToClips(jsonStr: String): List<ClipModel> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<ClipModel>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val trackType = try {
                    TrackType.valueOf(obj.getString("trackType"))
                } catch (e: Exception) {
                    TrackType.MAIN_VIDEO
                }
                val trackIndex = obj.optInt("trackIndex", 0)
                val mediaType = try {
                    MediaType.valueOf(obj.getString("mediaType"))
                } catch (e: Exception) {
                    MediaType.IMAGE
                }
                val mediaUri = obj.optString("mediaUri").takeIf { it.isNotEmpty() }
                val drawableResName = obj.optString("drawableResName").takeIf { it.isNotEmpty() }
                val solidColorHex = obj.optLong("solidColorHex", 0xFF1E293B)
                val startTimeMs = obj.getLong("startTimeMs")
                val durationMs = obj.getLong("durationMs")
                val sourceTrimStartMs = obj.optLong("sourceTrimStartMs", 0L)
                val sourceTrimEndMs = obj.optLong("sourceTrimEndMs", 0L)
                val speed = obj.optDouble("speed", 1.0).toFloat()
                val volume = obj.optDouble("volume", 1.0).toFloat()
                val isMuted = obj.optBoolean("isMuted", false)
                val transitionIn = try {
                    TransitionType.valueOf(obj.optString("transitionIn", "NONE"))
                } catch (e: Exception) {
                    TransitionType.NONE
                }
                val transitionDurationMs = obj.optLong("transitionDurationMs", 600L)

                // Keyframes
                val keyframes = mutableListOf<Keyframe>()
                val kfArray = obj.optJSONArray("keyframes")
                if (kfArray != null) {
                    for (k in 0 until kfArray.length()) {
                        val kfObj = kfArray.getJSONObject(k)
                        val kf = Keyframe(
                            id = kfObj.optString("id", "kf_$k"),
                            timeOffsetMs = kfObj.getLong("timeOffsetMs"),
                            scale = kfObj.optDouble("scale", 1.0).toFloat(),
                            rotation = kfObj.optDouble("rotation", 0.0).toFloat(),
                            translationX = kfObj.optDouble("translationX", 0.0).toFloat(),
                            translationY = kfObj.optDouble("translationY", 0.0).toFloat(),
                            opacity = kfObj.optDouble("opacity", 1.0).toFloat(),
                            easing = try {
                                EasingType.valueOf(kfObj.optString("easing", "EASE_IN_OUT"))
                            } catch (e: Exception) {
                                EasingType.EASE_IN_OUT
                            }
                        )
                        keyframes.add(kf)
                    }
                }

                // Color grading
                val cgObj = obj.optJSONObject("colorGrading")
                val colorGrading = if (cgObj != null) {
                    ColorGradingParams(
                        exposure = cgObj.optDouble("exposure", 0.0).toFloat(),
                        contrast = cgObj.optDouble("contrast", 1.0).toFloat(),
                        saturation = cgObj.optDouble("saturation", 1.0).toFloat(),
                        brightness = cgObj.optDouble("brightness", 0.0).toFloat(),
                        temperature = cgObj.optDouble("temperature", 0.0).toFloat(),
                        tint = cgObj.optDouble("tint", 0.0).toFloat(),
                        highlights = cgObj.optDouble("highlights", 0.0).toFloat(),
                        shadows = cgObj.optDouble("shadows", 0.0).toFloat(),
                        vignette = cgObj.optDouble("vignette", 0.0).toFloat(),
                        sharpness = cgObj.optDouble("sharpness", 0.0).toFloat(),
                        hueShift = cgObj.optDouble("hueShift", 0.0).toFloat(),
                        presetId = cgObj.optString("presetId").takeIf { it.isNotEmpty() },
                        presetIntensity = cgObj.optDouble("presetIntensity", 1.0).toFloat()
                    )
                } else ColorGradingParams()

                // Text config
                val tcObj = obj.optJSONObject("textConfig")
                val textConfig = if (tcObj != null) {
                    TextClipConfig(
                        text = tcObj.optString("text", "Title"),
                        fontSizeSp = tcObj.optDouble("fontSizeSp", 28.0).toFloat(),
                        textColorHex = tcObj.optLong("textColorHex", 0xFFFFFFFF),
                        backgroundColorHex = tcObj.optLong("backgroundColorHex", 0x88000000),
                        hasBackgroundBox = tcObj.optBoolean("hasBackgroundBox", false),
                        isBold = tcObj.optBoolean("isBold", true),
                        isItalic = tcObj.optBoolean("isItalic", false),
                        alignment = tcObj.optInt("alignment", 1)
                    )
                } else null

                list.add(
                    ClipModel(
                        id = id,
                        name = name,
                        trackType = trackType,
                        trackIndex = trackIndex,
                        mediaType = mediaType,
                        mediaUri = mediaUri,
                        drawableResName = drawableResName,
                        solidColorHex = solidColorHex,
                        startTimeMs = startTimeMs,
                        durationMs = durationMs,
                        sourceTrimStartMs = sourceTrimStartMs,
                        sourceTrimEndMs = sourceTrimEndMs,
                        speed = speed,
                        volume = volume,
                        isMuted = isMuted,
                        keyframes = keyframes,
                        colorGrading = colorGrading,
                        transitionIn = transitionIn,
                        transitionDurationMs = transitionDurationMs,
                        textConfig = textConfig
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
