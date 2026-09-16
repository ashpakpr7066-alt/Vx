package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.window.Dialog
import com.example.model.Project
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ExportRenderDialog(
    project: Project,
    onDismiss: () -> Unit
) {
    var selectedRes by remember { mutableStateOf("1080p FHD") }
    var selectedFps by remember { mutableIntStateOf(60) }
    var isRendering by remember { mutableStateOf(false) }
    var renderProgress by remember { mutableFloatStateOf(0f) }
    var currentFrame by remember { mutableIntStateOf(0) }
    var isDone by remember { mutableStateOf(false) }

    val resolutions = listOf("720p HD", "1080p FHD", "4K UHD")
    val fpsOptions = listOf(24, 30, 60)
    val totalFrames = ((project.durationMs / 1000f) * selectedFps).toInt().coerceAtLeast(60)

    // Simulate realistic hardware rendering pipeline
    LaunchedEffect(isRendering) {
        if (isRendering) {
            renderProgress = 0f
            currentFrame = 0
            while (renderProgress < 1.0f) {
                delay(60L)
                renderProgress += 0.035f
                currentFrame = (renderProgress.coerceIn(0f, 1f) * totalFrames).toInt()
            }
            renderProgress = 1.0f
            currentFrame = totalFrames
            delay(300L)
            isRendering = false
            isDone = true
        }
    }

    Dialog(onDismissRequest = { if (!isRendering) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = VNSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, VNBorder),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isDone) {
                    // Success View
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = VNEmerald,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Export Complete!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VNTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Rendered at $selectedRes @ ${selectedFps}fps with keyframe animations & cinematic LUTs applied.",
                        fontSize = 12.sp,
                        color = VNTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = VNCyan, contentColor = Color.Black)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                } else if (isRendering) {
                    // Rendering progress View
                    Text(
                        text = "Rendering Timeline...",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = VNTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Encoding Frame $currentFrame / $totalFrames",
                        fontSize = 12.sp,
                        color = VNCyan
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    LinearProgressIndicator(
                        progress = { renderProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = VNCyan,
                        trackColor = VNSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${(renderProgress * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = VNTextSecondary
                    )
                } else {
                    // Settings View
                    Text(
                        text = "Export Project",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = VNTextPrimary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Resolution
                    Text("Resolution", fontSize = 12.sp, color = VNTextSecondary, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        resolutions.forEach { res ->
                            FilterChip(
                                selected = selectedRes == res,
                                onClick = { selectedRes = res },
                                label = { Text(res, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VNCyan,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Frame Rate
                    Text("Frame Rate", fontSize = 12.sp, color = VNTextSecondary, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fpsOptions.forEach { fps ->
                            FilterChip(
                                selected = selectedFps == fps,
                                onClick = { selectedFps = fps },
                                label = { Text("${fps} fps", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VNCyan,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // File size estimate
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = VNSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimated File Size", fontSize = 12.sp, color = VNTextSecondary)
                            Text("~24.5 MB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VNTextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = VNTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { isRendering = true },
                            colors = ButtonDefaults.buttonColors(containerColor = VNCyan, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Video", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
