package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun AudioEffectsPanel(
    clip: ClipModel,
    onVolumeChanged: (Float) -> Unit,
    onMuteToggled: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var volume by remember(clip.volume) { mutableFloatStateOf(clip.volume) }
    var isMuted by remember(clip.isMuted) { mutableStateOf(clip.isMuted) }

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
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = VNEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Audio & Volume Controls",
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

            // Mute Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mute Clip Track", fontSize = 13.sp, color = VNTextPrimary)
                Switch(
                    checked = isMuted,
                    onCheckedChange = {
                        isMuted = it
                        onMuteToggled(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VNEmerald,
                        checkedTrackColor = VNEmerald.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Volume Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Volume Level", fontSize = 13.sp, color = VNTextSecondary)
                Text(
                    "${(volume * 100).toInt()}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = VNEmerald
                )
            }

            Slider(
                value = volume,
                onValueChange = {
                    volume = it
                    onVolumeChanged(it)
                },
                valueRange = 0f..2.0f,
                colors = SliderDefaults.colors(
                    thumbColor = VNEmerald,
                    activeTrackColor = VNEmerald,
                    inactiveTrackColor = VNBorder
                )
            )
        }
    }
}
