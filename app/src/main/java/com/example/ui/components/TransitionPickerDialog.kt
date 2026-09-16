package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.window.Dialog
import com.example.model.TransitionType
import com.example.ui.theme.*

@Composable
fun TransitionPickerDialog(
    currentTransition: TransitionType,
    currentDurationMs: Long,
    onSelect: (TransitionType, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTransition by remember { mutableStateOf(currentTransition) }
    var durationMs by remember { mutableLongStateOf(currentDurationMs) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = VNSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, VNBorder),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Clip Cut Transition",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = VNTextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(TransitionType.values()) { trans ->
                        val isSelected = selectedTransition == trans
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) VNCyan.copy(alpha = 0.2f) else VNSurfaceVariant)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) VNCyan else VNBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTransition = trans }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = trans.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) VNCyan else VNTextPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTransition != TransitionType.NONE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Duration", fontSize = 12.sp, color = VNTextSecondary)
                        Text("${durationMs}ms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VNCyan)
                    }
                    Slider(
                        value = durationMs.toFloat(),
                        onValueChange = { durationMs = it.toLong() },
                        valueRange = 200f..1500f,
                        steps = 12,
                        colors = SliderDefaults.colors(
                            thumbColor = VNCyan,
                            activeTrackColor = VNCyan,
                            inactiveTrackColor = VNBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = VNTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSelect(selectedTransition, durationMs) },
                        colors = ButtonDefaults.buttonColors(containerColor = VNCyan, contentColor = Color.Black)
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
