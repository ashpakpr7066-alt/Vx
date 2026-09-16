package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun KeyframeCurveEditor(
    clip: ClipModel,
    currentTimeMs: Long,
    onAddOrUpdateKeyframe: (Keyframe) -> Unit,
    onRemoveKeyframe: (String) -> Unit,
    onSeekToKeyframe: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val relTimeMs = (currentTimeMs - clip.startTimeMs).coerceIn(0L, clip.durationMs)
    val sortedKeyframes = remember(clip.keyframes) { clip.keyframes.sortedBy { it.timeOffsetMs } }
    val existingKeyframe = remember(clip.keyframes, relTimeMs) {
        clip.findKeyframeAtOrNear(relTimeMs, toleranceMs = 150L)
    }

    // Current working values (either from existing keyframe or interpolated values)
    var currentScale by remember(existingKeyframe, relTimeMs) {
        mutableStateOf(existingKeyframe?.scale ?: 1.0f)
    }
    var currentRotation by remember(existingKeyframe, relTimeMs) {
        mutableStateOf(existingKeyframe?.rotation ?: 0f)
    }
    var currentTransX by remember(existingKeyframe, relTimeMs) {
        mutableStateOf(existingKeyframe?.translationX ?: 0f)
    }
    var currentTransY by remember(existingKeyframe, relTimeMs) {
        mutableStateOf(existingKeyframe?.translationY ?: 0f)
    }
    var currentOpacity by remember(existingKeyframe, relTimeMs) {
        mutableStateOf(existingKeyframe?.opacity ?: 1.0f)
    }
    var currentEasing by remember(existingKeyframe, relTimeMs) {
        mutableStateOf(existingKeyframe?.easing ?: EasingType.EASE_IN_OUT)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .testTag("keyframe_curve_editor"),
        color = VNSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = "Keyframe",
                        tint = VNAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Keyframe Animation",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VNTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = VNAmber.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${clip.keyframes.size} Points",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = VNAmber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = VNTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation and Add/Remove Bar
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = VNSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Keyframe button
                    val prevKf = sortedKeyframes.lastOrNull { it.timeOffsetMs < relTimeMs - 50L }
                    IconButton(
                        onClick = { prevKf?.let { onSeekToKeyframe(clip.startTimeMs + it.timeOffsetMs) } },
                        enabled = prevKf != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Prev Keyframe",
                            tint = if (prevKf != null) VNCyan else VNTextTertiary
                        )
                    }

                    // Main Add/Remove Diamond Button
                    Button(
                        onClick = {
                            if (existingKeyframe != null) {
                                onRemoveKeyframe(existingKeyframe.id)
                            } else {
                                val newKf = Keyframe(
                                    id = "kf_" + System.currentTimeMillis(),
                                    timeOffsetMs = relTimeMs,
                                    scale = currentScale,
                                    rotation = currentRotation,
                                    translationX = currentTransX,
                                    translationY = currentTransY,
                                    opacity = currentOpacity,
                                    easing = currentEasing
                                )
                                onAddOrUpdateKeyframe(newKf)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (existingKeyframe != null) VNRose else VNAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("keyframe_action_button")
                    ) {
                        Icon(
                            imageVector = if (existingKeyframe != null) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (existingKeyframe != null) "Remove Keyframe" else "+ Add Keyframe",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Next Keyframe button
                    val nextKf = sortedKeyframes.firstOrNull { it.timeOffsetMs > relTimeMs + 50L }
                    IconButton(
                        onClick = { nextKf?.let { onSeekToKeyframe(clip.startTimeMs + it.timeOffsetMs) } },
                        enabled = nextKf != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Keyframe",
                            tint = if (nextKf != null) VNCyan else VNTextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Easing Curve Selector
            Text(
                text = "Easing Transition Curve",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = VNTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EasingType.values().forEach { easing ->
                    val isSelected = currentEasing == easing
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            currentEasing = easing
                            existingKeyframe?.let { kf ->
                                onAddOrUpdateKeyframe(kf.copy(easing = easing))
                            }
                        },
                        label = { Text(easing.displayName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VNCyan,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Property Sliders (Scale, Rotation, Position X, Position Y, Opacity)
            // 1. Scale
            KeyframeSlider(
                label = "Scale (Zoom)",
                value = currentScale,
                valueRange = 0.2f..3.0f,
                displayFormat = "%.2fx",
                onValueChange = {
                    currentScale = it
                    existingKeyframe?.let { kf -> onAddOrUpdateKeyframe(kf.copy(scale = it)) }
                }
            )

            // 2. Rotation
            KeyframeSlider(
                label = "Rotation",
                value = currentRotation,
                valueRange = -180f..180f,
                displayFormat = "%.0f°",
                onValueChange = {
                    currentRotation = it
                    existingKeyframe?.let { kf -> onAddOrUpdateKeyframe(kf.copy(rotation = it)) }
                }
            )

            // 3. Translation X
            KeyframeSlider(
                label = "Position X",
                value = currentTransX,
                valueRange = -300f..300f,
                displayFormat = "%.0f px",
                onValueChange = {
                    currentTransX = it
                    existingKeyframe?.let { kf -> onAddOrUpdateKeyframe(kf.copy(translationX = it)) }
                }
            )

            // 4. Translation Y
            KeyframeSlider(
                label = "Position Y",
                value = currentTransY,
                valueRange = -300f..300f,
                displayFormat = "%.0f px",
                onValueChange = {
                    currentTransY = it
                    existingKeyframe?.let { kf -> onAddOrUpdateKeyframe(kf.copy(translationY = it)) }
                }
            )

            // 5. Opacity
            KeyframeSlider(
                label = "Opacity",
                value = currentOpacity,
                valueRange = 0f..1.0f,
                displayFormat = "${(currentOpacity * 100).toInt()}%",
                onValueChange = {
                    currentOpacity = it
                    existingKeyframe?.let { kf -> onAddOrUpdateKeyframe(kf.copy(opacity = it)) }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick reset transforms button
            OutlinedButton(
                onClick = {
                    currentScale = 1.0f
                    currentRotation = 0f
                    currentTransX = 0f
                    currentTransY = 0f
                    currentOpacity = 1.0f
                    existingKeyframe?.let { kf ->
                        onAddOrUpdateKeyframe(
                            kf.copy(
                                scale = 1.0f,
                                rotation = 0f,
                                translationX = 0f,
                                translationY = 0f,
                                opacity = 1.0f
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VNTextSecondary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Transform to Default", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun KeyframeSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    displayFormat: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 12.sp, color = VNTextPrimary)
            Text(
                text = String.format(displayFormat, value),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VNCyan
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = VNCyan,
                activeTrackColor = VNCyan,
                inactiveTrackColor = VNBorder
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}
