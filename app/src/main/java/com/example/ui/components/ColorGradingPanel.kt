package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun ColorGradingPanel(
    params: ColorGradingParams,
    onParamsChanged: (ColorGradingParams) -> Unit,
    onCompareStart: () -> Unit,
    onCompareEnd: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("LUTs & Presets", "Adjustments", "RGB Curves")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .testTag("color_grading_panel"),
        color = VNSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Compare & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Color Grading",
                        tint = VNCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Color Grading Studio",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VNTextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Hold to Compare Button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = VNAmber.copy(alpha = 0.2f),
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { onCompareStart() },
                                    onDragEnd = { onCompareEnd() },
                                    onDragCancel = { onCompareEnd() },
                                    onDrag = { _, _ -> }
                                )
                            }
                            .clickable { }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Compare,
                                contentDescription = "Hold to Compare",
                                tint = VNAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hold: Compare",
                                fontSize = 11.sp,
                                color = VNAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VNTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = VNSurfaceVariant,
                contentColor = VNCyan,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                when (selectedTab) {
                    0 -> PresetsTab(params = params, onParamsChanged = onParamsChanged)
                    1 -> AdjustmentsTab(params = params, onParamsChanged = onParamsChanged)
                    2 -> RgbCurvesTab(params = params, onParamsChanged = onParamsChanged)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reset Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onParamsChanged(ColorGradingParams()) },
                    colors = ButtonDefaults.textButtonColors(contentColor = VNTextSecondary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset All Adjustments", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun PresetsTab(
    params: ColorGradingParams,
    onParamsChanged: (ColorGradingParams) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Cinematic LUTs & Film Presets",
            fontSize = 12.sp,
            color = VNTextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "None" option
            item {
                val isNoneSelected = params.presetId == null
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        onParamsChanged(params.copy(presetId = null))
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF222836))
                            .border(
                                width = if (isNoneSelected) 2.dp else 1.dp,
                                color = if (isNoneSelected) VNCyan else VNBorder,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Original", fontSize = 11.sp, color = VNTextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("None", fontSize = 10.sp, color = if (isNoneSelected) VNCyan else VNTextSecondary)
                }
            }

            items(FilterPresets.ALL) { preset ->
                val isSelected = params.presetId == preset.id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        onParamsChanged(params.copy(presetId = preset.id))
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(preset.previewColorHex))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) VNCyan else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = preset.category,
                                fontSize = 9.sp,
                                color = Color.White,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset.name,
                        fontSize = 10.sp,
                        color = if (isSelected) VNCyan else VNTextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Intensity Slider
        if (params.presetId != null) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Preset Intensity", fontSize = 12.sp, color = VNTextPrimary)
                    Text(
                        "${(params.presetIntensity * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = VNCyan
                    )
                }
                Slider(
                    value = params.presetIntensity,
                    onValueChange = { onParamsChanged(params.copy(presetIntensity = it)) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = VNCyan,
                        activeTrackColor = VNCyan,
                        inactiveTrackColor = VNBorder
                    )
                )
            }
        }
    }
}

@Composable
private fun AdjustmentsTab(
    params: ColorGradingParams,
    onParamsChanged: (ColorGradingParams) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Exposure
        GradingSlider(
            label = "Exposure",
            value = params.exposure,
            valueRange = -1.0f..1.0f,
            onValueChange = { onParamsChanged(params.copy(exposure = it)) }
        )

        // Contrast
        GradingSlider(
            label = "Contrast",
            value = params.contrast,
            valueRange = 0.5f..1.8f,
            defaultValue = 1.0f,
            onValueChange = { onParamsChanged(params.copy(contrast = it)) }
        )

        // Brightness
        GradingSlider(
            label = "Brightness",
            value = params.brightness,
            valueRange = -0.5f..0.5f,
            onValueChange = { onParamsChanged(params.copy(brightness = it)) }
        )

        // Saturation
        GradingSlider(
            label = "Saturation",
            value = params.saturation,
            valueRange = 0.0f..2.0f,
            defaultValue = 1.0f,
            onValueChange = { onParamsChanged(params.copy(saturation = it)) }
        )

        // Temperature (Cool -> Warm)
        GradingSlider(
            label = "Temperature (Cool / Warm)",
            value = params.temperature,
            valueRange = -1.0f..1.0f,
            accentColor = if (params.temperature > 0) VNAmber else VNCyan,
            onValueChange = { onParamsChanged(params.copy(temperature = it)) }
        )

        // Tint (Green -> Magenta)
        GradingSlider(
            label = "Tint (Green / Magenta)",
            value = params.tint,
            valueRange = -1.0f..1.0f,
            accentColor = if (params.tint > 0) VNPurple else VNEmerald,
            onValueChange = { onParamsChanged(params.copy(tint = it)) }
        )

        // Highlights
        GradingSlider(
            label = "Highlights",
            value = params.highlights,
            valueRange = -1.0f..1.0f,
            onValueChange = { onParamsChanged(params.copy(highlights = it)) }
        )

        // Shadows
        GradingSlider(
            label = "Shadows",
            value = params.shadows,
            valueRange = -1.0f..1.0f,
            onValueChange = { onParamsChanged(params.copy(shadows = it)) }
        )

        // Vignette
        GradingSlider(
            label = "Vignette",
            value = params.vignette,
            valueRange = 0.0f..1.0f,
            onValueChange = { onParamsChanged(params.copy(vignette = it)) }
        )
    }
}

@Composable
private fun RgbCurvesTab(
    params: ColorGradingParams,
    onParamsChanged: (ColorGradingParams) -> Unit
) {
    var activeChannel by remember { mutableIntStateOf(0) } // 0: Master, 1: Red, 2: Green, 3: Blue
    val channelColors = listOf(Color.White, Color(0xFFEF4444), Color(0xFF10B981), Color(0xFF38BDF8))
    val channelNames = listOf("Master (RGB)", "Red", "Green", "Blue")

    Column(modifier = Modifier.fillMaxSize()) {
        // Channel Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            channelNames.forEachIndexed { idx, name ->
                val isSelected = activeChannel == idx
                FilterChip(
                    selected = isSelected,
                    onClick = { activeChannel = idx },
                    label = { Text(name, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = channelColors[idx].copy(alpha = 0.25f),
                        selectedLabelColor = channelColors[idx]
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2D Interactive Tone Curve Graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F131A))
                .border(1.dp, VNBorder, RoundedCornerShape(8.dp))
        ) {
            val color = channelColors[activeChannel]

            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val w = size.width
                val h = size.height

                // Draw Grid
                for (i in 1..3) {
                    val lineX = (w / 4f) * i
                    val lineY = (h / 4f) * i
                    drawLine(Color.White.copy(alpha = 0.1f), Offset(lineX, 0f), Offset(lineX, h), 1f)
                    drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, lineY), Offset(w, lineY), 1f)
                }
                // Diagonal baseline
                drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, h), Offset(w, 0f), 1.5f)

                // Draw Tone Curve Line
                val path = Path().apply {
                    moveTo(0f, h)
                    // Cubic Bezier curve simulation
                    cubicTo(w * 0.3f, h * 0.7f, w * 0.7f, h * 0.3f, w, 0f)
                }
                drawPath(path, color, style = Stroke(width = 3f))

                // Control points
                drawCircle(color, radius = 6f, center = Offset(0f, h))
                drawCircle(color, radius = 6f, center = Offset(w * 0.5f, h * 0.5f))
                drawCircle(color, radius = 6f, center = Offset(w, 0f))
            }
        }
    }
}

@Composable
private fun GradingSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    defaultValue: Float = 0f,
    accentColor: Color = VNCyan,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, color = VNTextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%.2f", value),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                if (value != defaultValue) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Reset",
                        tint = VNTextTertiary,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onValueChange(defaultValue) }
                    )
                }
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = VNBorder
            ),
            modifier = Modifier.height(26.dp)
        )
    }
}
