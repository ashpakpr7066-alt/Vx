package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val project = state.project

    if (project == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VNBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = VNCyan)
        }
        return
    }

    var showAspectMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Minimal VN Top Navigation Bar
            Surface(
                color = Color(0xFF0F1117),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button & Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = VNTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = project.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VNTextPrimary,
                            maxLines = 1
                        )
                    }

                    // Top Action Buttons: Compare Hold & Export
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Compare (Raw vs Graded)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (state.isCompareMode) VNAmber else Color(0xFF1E2433),
                            modifier = Modifier.clickable { viewModel.setCompareMode(!state.isCompareMode) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Compare,
                                    contentDescription = "Compare",
                                    tint = if (state.isCompareMode) Color.Black else VNTextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.isCompareMode) "Graded" else "Raw",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isCompareMode) Color.Black else VNTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Export Button
                        Button(
                            onClick = { viewModel.openSheet(EditorSheet.EXPORT_DIALOG) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("export_button")
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0B0D13))
        ) {
            // 1. Preview Viewport (PlayerCanvas with VN Bounding Box)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                PlayerCanvas(
                    project = project,
                    currentTimeMs = state.currentTimeMs,
                    selectedClip = state.selectedClip,
                    isCompareMode = state.isCompareMode,
                    onClipSelected = { viewModel.selectClip(it) },
                    onTransformChanged = { scale, rot, tx, ty ->
                        viewModel.updateTransform(scale, rot, tx, ty)
                    },
                    onOpenTextEditor = {
                        viewModel.openSheet(EditorSheet.TEXT_EDITOR)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Playback Transport Bar directly matching the screenshot
            VNPlaybackTransportBar(
                currentTimeMs = state.currentTimeMs,
                totalDurationMs = project.maxDurationMs,
                isPlaying = state.isPlaying,
                canUndo = state.canUndo,
                canRedo = state.canRedo,
                onTogglePlay = { viewModel.togglePlay() },
                onSkipPrev = { viewModel.skipToPrevCut() },
                onSkipNext = { viewModel.skipToNextCut() },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onOpenAspectMenu = { showAspectMenu = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F121A))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Aspect ratio popup dropdown
            DropdownMenu(
                expanded = showAspectMenu,
                onDismissRequest = { showAspectMenu = false },
                modifier = Modifier.background(Color(0xFF1E2433))
            ) {
                AspectRatio.values().forEach { ratio ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${ratio.label} (${ratio.iconName})",
                                fontSize = 13.sp,
                                color = if (project.aspectRatio == ratio) VNCyan else VNTextPrimary,
                                fontWeight = if (project.aspectRatio == ratio) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            viewModel.setAspectRatio(ratio)
                            showAspectMenu = false
                        }
                    )
                }
            }

            // 3. Multi-Track Timeline with fixed left track sidebar & floating capsule
            TimelineView(
                project = project,
                currentTimeMs = state.currentTimeMs,
                selectedClip = state.selectedClip,
                pixelsPerSecond = state.pixelsPerSecond,
                onSeek = { viewModel.seek(it) },
                onClipSelected = { viewModel.selectClip(it) },
                onTrimClip = { clip, newStart, newDur -> viewModel.trimClip(clip, newStart, newDur) },
                onOpenTransitionPicker = { viewModel.openTransitionPicker(it) },
                onAddTrackClip = { track ->
                    when (track) {
                        TrackType.AUDIO -> viewModel.openSheet(EditorSheet.MEDIA_PICKER, TrackType.AUDIO)
                        TrackType.TEXT -> viewModel.addTextClip()
                        TrackType.PIP -> viewModel.openSheet(EditorSheet.MEDIA_PICKER, TrackType.PIP)
                        TrackType.MAIN_VIDEO -> viewModel.openSheet(EditorSheet.MEDIA_PICKER, TrackType.MAIN_VIDEO)
                    }
                },
                onToggleMuteAll = { viewModel.toggleMuteAll() },
                isMutedAll = state.isMutedAll,
                // Floating capsule menu actions:
                onReplaceClip = { viewModel.openSheet(EditorSheet.MEDIA_PICKER, state.selectedClip?.trackType ?: TrackType.MAIN_VIDEO) },
                onMotionClip = { viewModel.openSheet(EditorSheet.TRANSITION_PICKER) },
                onKeyframeClip = { viewModel.openSheet(EditorSheet.KEYFRAME_CURVES) },
                onCurveClip = { viewModel.openSheet(EditorSheet.SPEED_FLOW) },
                onLockClip = { viewModel.toggleLockSelectedClip() },
                onDuplicateClip = { viewModel.duplicateSelectedClip() },
                onDeleteClip = { viewModel.deleteSelectedClip() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            // 4. Bottom VN Pro Editing Action Toolbar (Exact icons & order from screenshot)
            VNProBottomToolbar(
                selectedClip = state.selectedClip,
                onAction = { action ->
                    when (action) {
                        VNEditorAction.TRIM -> {
                            // Focus on selected clip trim
                        }
                        VNEditorAction.FX -> viewModel.openSheet(EditorSheet.TRANSITION_PICKER)
                        VNEditorAction.SPLIT -> viewModel.splitSelectedClip()
                        VNEditorAction.DELETE -> viewModel.deleteSelectedClip()
                        VNEditorAction.FLOW -> viewModel.openSheet(EditorSheet.SPEED_FLOW)
                        VNEditorAction.COLOR -> viewModel.openSheet(EditorSheet.COLOR_GRADING)
                        VNEditorAction.CROP -> showAspectMenu = true
                        VNEditorAction.ROTATE -> viewModel.rotateSelectedClip90()
                        VNEditorAction.MIRROR -> viewModel.toggleMirrorSelectedClip()
                        VNEditorAction.FLIP -> viewModel.toggleFlipSelectedClip()
                        VNEditorAction.FIT -> {
                            val currentScale = state.selectedClip?.scaleFactor ?: 1f
                            val newScale = if (currentScale >= 1.2f) 1.0f else 1.35f
                            viewModel.updateTransform(newScale, 0f, 0f, 0f)
                        }
                        VNEditorAction.BORDER -> viewModel.openSheet(EditorSheet.BORDER_DIALOG)
                        VNEditorAction.BLUR -> {
                            // Toggle background blur
                        }
                        VNEditorAction.OPACITY -> viewModel.openSheet(EditorSheet.OPACITY_SLIDER)
                        VNEditorAction.POSITION -> viewModel.openSheet(EditorSheet.KEYFRAME_CURVES)
                        VNEditorAction.BLENDING -> {
                            // Cycle blend modes
                        }
                        VNEditorAction.MASK -> viewModel.openSheet(EditorSheet.TRANSITION_PICKER)
                        VNEditorAction.CHROMA -> {
                            // Toggle chroma green screen
                        }
                        VNEditorAction.MAIN_TRACK -> viewModel.toggleMainTrackPip()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0D13))
            )
        }

        // Sub-Sheets and Dialogs
        when (state.activeSheet) {
            EditorSheet.COLOR_GRADING -> {
                state.selectedClip?.let { clip ->
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeSheet() },
                        containerColor = VNSurface
                    ) {
                        ColorGradingPanel(
                            params = clip.colorGrading,
                            onParamsChanged = { viewModel.updateColorGrading(it) },
                            onCompareStart = { viewModel.setCompareMode(true) },
                            onCompareEnd = { viewModel.setCompareMode(false) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                }
            }
            EditorSheet.KEYFRAME_CURVES -> {
                state.selectedClip?.let { clip ->
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeSheet() },
                        containerColor = VNSurface
                    ) {
                        KeyframeCurveEditor(
                            clip = clip,
                            currentTimeMs = state.currentTimeMs,
                            onAddOrUpdateKeyframe = { viewModel.addOrUpdateKeyframe(it) },
                            onRemoveKeyframe = { viewModel.removeKeyframe(it) },
                            onSeekToKeyframe = { viewModel.seek(it) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                }
            }
            EditorSheet.SPEED_FLOW -> {
                state.selectedClip?.let { clip ->
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeSheet() },
                        containerColor = VNSurface
                    ) {
                        SpeedEditorPanel(
                            clip = clip,
                            onSpeedChanged = { viewModel.updateSpeed(it) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                }
            }
            EditorSheet.AUDIO_EFFECTS -> {
                state.selectedClip?.let { clip ->
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeSheet() },
                        containerColor = VNSurface
                    ) {
                        AudioEffectsPanel(
                            clip = clip,
                            onVolumeChanged = { viewModel.updateVolume(it) },
                            onMuteToggled = { viewModel.updateMute(it) },
                            onClose = { viewModel.closeSheet() }
                        )
                    }
                }
            }
            EditorSheet.TEXT_EDITOR -> {
                val clip = state.selectedClip
                val config = clip?.textConfig ?: TextClipConfig()
                TextEditorDialog(
                    config = config,
                    onSave = {
                        viewModel.updateTextConfig(it)
                        viewModel.closeSheet()
                    },
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            EditorSheet.MEDIA_PICKER -> {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.closeSheet() },
                    containerColor = VNSurface
                ) {
                    MediaPickerSheet(
                        targetTrack = state.targetTrackForMedia,
                        onAddMedia = { uri, res, name, type, dur ->
                            viewModel.addMedia(uri, res, name, type, dur)
                        },
                        onClose = { viewModel.closeSheet() }
                    )
                }
            }
            EditorSheet.TRANSITION_PICKER -> {
                val clip = state.clipForTransition ?: state.selectedClip
                if (clip != null) {
                    TransitionPickerDialog(
                        currentTransition = clip.transitionIn,
                        currentDurationMs = clip.transitionDurationMs,
                        onSelect = { trans, dur ->
                            viewModel.updateTransition(trans, dur)
                            viewModel.closeSheet()
                        },
                        onDismiss = { viewModel.closeSheet() }
                    )
                }
            }
            EditorSheet.EXPORT_DIALOG -> {
                ExportRenderDialog(
                    project = project,
                    onDismiss = { viewModel.closeSheet() }
                )
            }
            EditorSheet.OPACITY_SLIDER -> {
                state.selectedClip?.let { clip ->
                    AlertDialog(
                        onDismissRequest = { viewModel.closeSheet() },
                        containerColor = Color(0xFF1E2433),
                        title = { Text("Opacity", color = Color.White) },
                        text = {
                            var opacityVal by remember { mutableStateOf(clip.opacity) }
                            Column {
                                Text("${(opacityVal * 100).toInt()}%", color = VNCyan, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = opacityVal,
                                    onValueChange = {
                                        opacityVal = it
                                        viewModel.setOpacity(it)
                                    },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(thumbColor = VNCyan, activeTrackColor = VNCyan)
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.closeSheet() }) {
                                Text("Done", color = VNCyan)
                            }
                        }
                    )
                }
            }
            EditorSheet.BORDER_DIALOG -> {
                AlertDialog(
                    onDismissRequest = { viewModel.closeSheet() },
                    containerColor = Color(0xFF1E2433),
                    title = { Text("Border Frame", color = Color.White) },
                    text = {
                        Text("Add colored border frame around clip", color = VNTextSecondary)
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.closeSheet() }) {
                            Text("Apply", color = VNCyan)
                        }
                    }
                )
            }
            else -> {}
        }
    }
}

/**
 * VN Playback & Transport Bar
 * Left: Timecode 0:06.92 / 1:03.19
 * Center: [⏮] [▶] [⏭]
 * Right: [⊶ Ratio] [↩ Undo] [↪ Redo]
 */
@Composable
private fun VNPlaybackTransportBar(
    currentTimeMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onTogglePlay: () -> Unit,
    onSkipPrev: () -> Unit,
    onSkipNext: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOpenAspectMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    fun formatVnTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val min = totalSeconds / 60
        val sec = totalSeconds % 60
        val hundredths = (ms % 1000) / 10
        return String.format("%d:%02d.%02d", min, sec, hundredths)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Timecode Readout "0:06.92 / 1:03.19"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatVnTime(currentTimeMs),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = " / ${formatVnTime(totalDurationMs)}",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.White.copy(alpha = 0.5f)
            )
        }

        // Center: Transport Buttons [⏮] [▶] [⏭]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onSkipPrev,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Cut",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onSkipNext,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Cut",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Right: Format/Ratio [⊶], Undo [↩], Redo [↪]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onOpenAspectMenu,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AspectRatio,
                    contentDescription = "Ratio",
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

enum class VNEditorAction {
    TRIM,
    FX,
    SPLIT,
    DELETE,
    FLOW,
    COLOR,
    CROP,
    ROTATE,
    MIRROR,
    FLIP,
    FIT,
    BORDER,
    BLUR,
    OPACITY,
    POSITION,
    BLENDING,
    MASK,
    CHROMA,
    MAIN_TRACK
}

/**
 * Exact Bottom Toolbar from VN Editor Screenshot
 * Tools: Trim, FX, Split, Delete, Flow, Color, Crop, Rotate, Mirror, Flip, Fit, Border, Blur, Opacity, Position, Blending, Mask, Chroma, Main Track
 */
@Composable
private fun VNProBottomToolbar(
    selectedClip: ClipModel?,
    onAction: (VNEditorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Trim
        VNToolbarItem(
            label = "Trim",
            icon = Icons.Default.Code,
            onClick = { onAction(VNEditorAction.TRIM) }
        )

        // 2. FX
        VNToolbarItem(
            label = "FX",
            icon = Icons.Default.StarOutline,
            onClick = { onAction(VNEditorAction.FX) }
        )

        // 3. Split
        VNToolbarItem(
            label = "Split",
            icon = Icons.Default.ContentCut,
            onClick = { onAction(VNEditorAction.SPLIT) }
        )

        // 4. Delete
        VNToolbarItem(
            label = "Delete",
            icon = Icons.Default.DeleteOutline,
            onClick = { onAction(VNEditorAction.DELETE) }
        )

        // 5. Flow / Speed
        VNToolbarItem(
            label = "Flow",
            icon = Icons.Default.Speed,
            onClick = { onAction(VNEditorAction.FLOW) }
        )

        // 6. Color
        VNToolbarItem(
            label = "Color",
            icon = Icons.Default.Square,
            onClick = { onAction(VNEditorAction.COLOR) }
        )

        // 7. Crop
        VNToolbarItem(
            label = "Crop",
            icon = Icons.Default.Crop,
            onClick = { onAction(VNEditorAction.CROP) }
        )

        // 8. Rotate
        VNToolbarItem(
            label = "Rotate",
            icon = Icons.Default.RotateRight,
            onClick = { onAction(VNEditorAction.ROTATE) }
        )

        // 9. Mirror
        VNToolbarItem(
            label = "Mirror",
            icon = Icons.Default.FlipCameraAndroid,
            onClick = { onAction(VNEditorAction.MIRROR) }
        )

        // 10. Flip
        VNToolbarItem(
            label = "Flip",
            icon = Icons.Default.SwapVert,
            onClick = { onAction(VNEditorAction.FLIP) }
        )

        // 11. Fit
        VNToolbarItem(
            label = "Fit",
            icon = Icons.Default.FitScreen,
            onClick = { onAction(VNEditorAction.FIT) }
        )

        // 12. Border
        VNToolbarItem(
            label = "Border",
            icon = Icons.Default.CropSquare,
            onClick = { onAction(VNEditorAction.BORDER) }
        )

        // 13. Blur
        VNToolbarItem(
            label = "Blur",
            icon = Icons.Default.BlurOn,
            onClick = { onAction(VNEditorAction.BLUR) }
        )

        // 14. Opacity
        VNToolbarItem(
            label = "Opacity",
            icon = Icons.Default.Tonality,
            onClick = { onAction(VNEditorAction.OPACITY) }
        )

        // 15. Position
        VNToolbarItem(
            label = "Position",
            icon = Icons.Default.OpenWith,
            onClick = { onAction(VNEditorAction.POSITION) }
        )

        // 16. Blending
        VNToolbarItem(
            label = "Blending",
            icon = Icons.Default.Layers,
            onClick = { onAction(VNEditorAction.BLENDING) }
        )

        // 17. Mask
        VNToolbarItem(
            label = "Mask",
            icon = Icons.Default.FilterFrames,
            onClick = { onAction(VNEditorAction.MASK) }
        )

        // 18. Chroma
        VNToolbarItem(
            label = "Chroma",
            icon = Icons.Default.Colorize,
            onClick = { onAction(VNEditorAction.CHROMA) }
        )

        // 19. Main Track
        VNToolbarItem(
            label = "Main Track",
            icon = Icons.Default.SwapCalls,
            onClick = { onAction(VNEditorAction.MAIN_TRACK) }
        )
    }
}

@Composable
private fun VNToolbarItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
