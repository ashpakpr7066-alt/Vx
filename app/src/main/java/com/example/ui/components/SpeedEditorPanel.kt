package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClipModel
import com.example.ui.theme.*

@Composable
fun SpeedEditorPanel(
    clip: ClipModel,
    onSpeedChanged: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var speed by remember(clip.speed) { mutableFloatStateOf(clip.speed) }
    val presets = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f, 4.0f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = VNSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = VNCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clip Speed Curve",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VNTextPrimary
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = VNTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Speed Multiplier", fontSize = 13.sp, color = VNTextSecondary)
                Text(
                    String.format("%.2fx", speed),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VNCyan
                )
            }

            Slider(
                value = speed,
                onValueChange = {
                    speed = it
                    onSpeedChanged(it)
                },
                valueRange = 0.2f..4.0f,
                colors = SliderDefaults.colors(
                    thumbColor = VNCyan,
                    activeTrackColor = VNCyan,
                    inactiveTrackColor = VNBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                presets.take(5).forEach { p ->
                    FilterChip(
                        selected = Math.abs(speed - p) < 0.05f,
                        onClick = {
                            speed = p
                            onSpeedChanged(p)
                        },
                        label = { Text("${p}x", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VNCyan,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                presets.drop(5).forEach { p ->
                    FilterChip(
                        selected = Math.abs(speed - p) < 0.05f,
                        onClick = {
                            speed = p
                            onSpeedChanged(p)
                        },
                        label = { Text("${p}x", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VNCyan,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }
    }
}
