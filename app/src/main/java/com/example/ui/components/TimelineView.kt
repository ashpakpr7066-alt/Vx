package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun TimelineView(
    project: Project,
    currentTimeMs: Long,
    selectedClip: ClipModel?,
    pixelsPerSecond: Float,
    onSeek: (Long) -> Unit,
    onClipSelected: (ClipModel) -> Unit,
    onTrimClip: (clip: ClipModel, newStartMs: Long, newDurationMs: Long) -> Unit,
    onOpenTransitionPicker: (ClipModel) -> Unit,
    onAddTrackClip: (TrackType) -> Unit,
    onToggleMuteAll: () -> Unit = {},
    isMutedAll: Boolean = false,
    // Floating capsule actions:
    onReplaceClip: () -> Unit = {},
    onMotionClip: () -> Unit = {},
    onKeyframeClip: () -> Unit = {},
    onCurveClip: () -> Unit = {},
    onLockClip: () -> Unit = {},
    onDuplicateClip: () -> Unit = {},
    onDeleteClip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()
    val totalDurationMs = project.maxDurationMs.coerceAtLeast(15000L)
    val timelineWidthDp = with(density) {
        ((totalDurationMs / 1000f) * pixelsPerSecond).toDp() + 600.dp
    }

    // Auto-scroll timeline to keep playhead in view while playing
    LaunchedEffect(currentTimeMs) {
        val playheadOffsetPx = (currentTimeMs / 1000f) * pixelsPerSecond
        val targetScroll = (playheadOffsetPx - 250).coerceAtLeast(0f).toInt()
        if (Math.abs(scrollState.value - targetScroll) > 400) {
            scrollState.scrollTo(targetScroll)
        }
    }

    // VN 4-Track Order: AUDIO (top), TEXT, PIP, MAIN_VIDEO (bottom)
    val trackOrder = listOf(TrackType.AUDIO, TrackType.TEXT, TrackType.PIP, TrackType.MAIN_VIDEO)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F1117))
            .testTag("timeline_view")
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 1. Fixed Left Track Header Sidebar (VN Iconic Sidebar)
            Column(
                modifier = Modifier
                    .width(58.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF131722))
                    .border(androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF232A3B)))
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Track 1: Audio / Music Header
                TrackHeaderButton(
                    icon = Icons.Default.MusicNote,
                    label = "Music",
                    bgColor = Color(0xFF2A1B4E),
                    iconColor = Color(0xFFA78BFA),
                    onClick = { onAddTrackClip(TrackType.AUDIO) },
                    modifier = Modifier.height(34.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Track 2: Text Header
                TrackHeaderButton(
                    icon = Icons.Default.TextFields,
                    label = "Text",
                    bgColor = Color(0xFF0D3330),
                    iconColor = Color(0xFF2DD4BF),
                    onClick = { onAddTrackClip(TrackType.TEXT) },
                    modifier = Modifier.height(34.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Track 3: Sticker / PIP Header
                TrackHeaderButton(
                    icon = Icons.Default.AutoAwesome,
                    label = "PIP",
                    bgColor = Color(0xFF132A4A),
                    iconColor = Color(0xFF38BDF8),
                    onClick = { onAddTrackClip(TrackType.PIP) },
                    modifier = Modifier.height(38.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Track 4: Main Video Header + Cover button + Mute
                TrackHeaderButton(
                    icon = Icons.Default.MovieFilter,
                    label = "Main",
                    bgColor = Color(0xFF1E293B),
                    iconColor = Color.White,
                    onClick = { onAddTrackClip(TrackType.MAIN_VIDEO) },
                    modifier = Modifier.height(58.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Left controls: Cover & Mute
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute all icon
                    IconButton(
                        onClick = onToggleMuteAll,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (isMutedAll) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Mute",
                            tint = if (isMutedAll) VNRose else VNTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 2. Scrollable Timeline Tracks Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(scrollState)
                    .pointerInput(totalDurationMs, pixelsPerSecond) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val clickX = change.position.x
                            val newTimeMs = ((clickX / pixelsPerSecond) * 1000f)
                                .toLong()
                                .coerceIn(0L, totalDurationMs)
                            onSeek(newTimeMs)
                        }
                    }
            ) {
                Column(
                    modifier = Modifier
                        .width(timelineWidthDp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                ) {
                    // Track 1: Audio Lane
                    TrackLane(
                        trackType = TrackType.AUDIO,
                        clips = project.clips.filter { it.trackType == TrackType.AUDIO },
                        currentTimeMs = currentTimeMs,
                        selectedClip = selectedClip,
                        pixelsPerSecond = pixelsPerSecond,
                        onClipSelected = onClipSelected,
                        onTrimClip = onTrimClip,
                        onOpenTransitionPicker = onOpenTransitionPicker,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Track 2: Text / Subtitle Lane
                    TrackLane(
                        trackType = TrackType.TEXT,
                        clips = project.clips.filter { it.trackType == TrackType.TEXT },
                        currentTimeMs = currentTimeMs,
                        selectedClip = selectedClip,
                        pixelsPerSecond = pixelsPerSecond,
                        onClipSelected = onClipSelected,
                        onTrimClip = onTrimClip,
                        onOpenTransitionPicker = onOpenTransitionPicker,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Track 3: Sticker / PIP Lane
                    TrackLane(
                        trackType = TrackType.PIP,
                        clips = project.clips.filter { it.trackType == TrackType.PIP },
                        currentTimeMs = currentTimeMs,
                        selectedClip = selectedClip,
                        pixelsPerSecond = pixelsPerSecond,
                        onClipSelected = onClipSelected,
                        onTrimClip = onTrimClip,
                        onOpenTransitionPicker = onOpenTransitionPicker,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Track 4: Main Video Track Lane (Filmstrip)
                    MainVideoTrackLane(
                        clips = project.clips.filter { it.trackType == TrackType.MAIN_VIDEO },
                        currentTimeMs = currentTimeMs,
                        selectedClip = selectedClip,
                        pixelsPerSecond = pixelsPerSecond,
                        onClipSelected = onClipSelected,
                        onTrimClip = onTrimClip,
                        onOpenTransitionPicker = onOpenTransitionPicker,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bottom Time Ruler (Seconds numbers 00:00, 00:01, 00:02...)
                    TimeRuler(
                        totalDurationMs = totalDurationMs,
                        pixelsPerSecond = pixelsPerSecond,
                        onSeek = onSeek,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp)
                    )
                }

                // White Playhead Needle spanning all tracks
                val playheadOffsetDp = with(density) {
                    ((currentTimeMs / 1000f) * pixelsPerSecond).toDp()
                }

                Box(
                    modifier = Modifier
                        .offset(x = playheadOffsetDp - 1.dp)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.White)
                ) {
                    // Playhead top tip
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                // Authentic VN Floating Context Menu Capsule above selected clip
                if (selectedClip != null) {
                    val clipStartDp = with(density) {
                        ((selectedClip.startTimeMs / 1000f) * pixelsPerSecond).toDp()
                    }
                    val clipWidthDp = with(density) {
                        ((selectedClip.durationMs / 1000f) * pixelsPerSecond).toDp()
                    }
                    val popupCenterDp = (clipStartDp + (clipWidthDp / 2f) - 160.dp).coerceAtLeast(10.dp)

                    val trackYOffset = when (selectedClip.trackType) {
                        TrackType.AUDIO -> 0.dp
                        TrackType.TEXT -> 20.dp
                        TrackType.PIP -> 45.dp
                        TrackType.MAIN_VIDEO -> 75.dp
                    }

                    VNFloatingActionCapsule(
                        isLocked = selectedClip.isLocked,
                        onReplace = onReplaceClip,
                        onMotion = onMotionClip,
                        onKeyframe = onKeyframeClip,
                        onCurve = onCurveClip,
                        onLock = onLockClip,
                        onDuplicate = onDuplicateClip,
                        onDelete = onDeleteClip,
                        modifier = Modifier
                            .offset(x = popupCenterDp, y = trackYOffset)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackHeaderButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "+",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = iconColor.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Authentic Floating Blue Action Capsule Menu from VN Editor
 * Options: [Replace] [Motion] [Keyframe] [Curve] [Lock] [Duplicate] [Delete]
 */
@Composable
private fun VNFloatingActionCapsule(
    isLocked: Boolean,
    onReplace: () -> Unit,
    onMotion: () -> Unit,
    onKeyframe: () -> Unit,
    onCurve: () -> Unit,
    onLock: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1D4ED8), // Vivid VN Blue Capsule
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6)),
        shadowElevation = 8.dp,
        modifier = modifier.height(42.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. Replace
            CapsuleActionItem(
                icon = Icons.Default.Cached,
                label = "Replace",
                onClick = onReplace
            )

            // 2. Motion
            CapsuleActionItem(
                icon = Icons.Default.Animation,
                label = "Motion",
                onClick = onMotion
            )

            // 3. Keyframe
            CapsuleActionItem(
                icon = Icons.Default.Diamond,
                label = "Keyframe",
                onClick = onKeyframe
            )

            // 4. Curve
            CapsuleActionItem(
                icon = Icons.Default.Timeline,
                label = "Curve",
                onClick = onCurve
            )

            // 5. Lock
            CapsuleActionItem(
                icon = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                label = if (isLocked) "Unlock" else "Lock",
                onClick = onLock
            )

            // 6. Duplicate
            CapsuleActionItem(
                icon = Icons.Default.ControlPointDuplicate,
                label = "Duplicate",
                onClick = onDuplicate
            )

            // 7. Delete
            CapsuleActionItem(
                icon = Icons.Default.DeleteOutline,
                label = "Delete",
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun CapsuleActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun TrackLane(
    trackType: TrackType,
    clips: List<ClipModel>,
    currentTimeMs: Long,
    selectedClip: ClipModel?,
    pixelsPerSecond: Float,
    onClipSelected: (ClipModel) -> Unit,
    onTrimClip: (clip: ClipModel, newStartMs: Long, newDurationMs: Long) -> Unit,
    onOpenTransitionPicker: (ClipModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val laneBg = when (trackType) {
        TrackType.AUDIO -> Color(0xFF1E1533)
        TrackType.TEXT -> Color(0xFF0F2323)
        TrackType.PIP -> Color(0xFF101B2E)
        TrackType.MAIN_VIDEO -> Color(0xFF131A29)
    }

    Box(
        modifier = modifier
            .background(laneBg, RoundedCornerShape(4.dp))
            .border(0.5.dp, Color(0xFF242C3F), RoundedCornerShape(4.dp))
    ) {
        for (clip in clips) {
            val clipStartDp = with(density) {
                ((clip.startTimeMs / 1000f) * pixelsPerSecond).toDp()
            }
            val clipWidthDp = with(density) {
                ((clip.durationMs / 1000f) * pixelsPerSecond).toDp().coerceAtLeast(36.dp)
            }
            val isSelected = clip.id == selectedClip?.id

            TimelineClipItem(
                clip = clip,
                isSelected = isSelected,
                currentTimeMs = currentTimeMs,
                pixelsPerSecond = pixelsPerSecond,
                onSelect = { onClipSelected(clip) },
                onTrim = { newStart, newDur -> onTrimClip(clip, newStart, newDur) },
                onTransitionClick = { onOpenTransitionPicker(clip) },
                modifier = Modifier
                    .offset(x = clipStartDp)
                    .width(clipWidthDp)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * Main Video Filmstrip Lane (Track 4)
 * Shows repeating frame thumbnails, cut transitions, and duration badges
 */
@Composable
private fun MainVideoTrackLane(
    clips: List<ClipModel>,
    currentTimeMs: Long,
    selectedClip: ClipModel?,
    pixelsPerSecond: Float,
    onClipSelected: (ClipModel) -> Unit,
    onTrimClip: (clip: ClipModel, newStartMs: Long, newDurationMs: Long) -> Unit,
    onOpenTransitionPicker: (ClipModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .background(Color(0xFF0E131E), RoundedCornerShape(4.dp))
            .border(0.5.dp, Color(0xFF202738), RoundedCornerShape(4.dp))
    ) {
        for (clip in clips) {
            val clipStartDp = with(density) {
                ((clip.startTimeMs / 1000f) * pixelsPerSecond).toDp()
            }
            val clipWidthDp = with(density) {
                ((clip.durationMs / 1000f) * pixelsPerSecond).toDp().coerceAtLeast(50.dp)
            }
            val isSelected = clip.id == selectedClip?.id

            FilmstripClipItem(
                clip = clip,
                isSelected = isSelected,
                currentTimeMs = currentTimeMs,
                pixelsPerSecond = pixelsPerSecond,
                onSelect = { onClipSelected(clip) },
                onTrim = { newStart, newDur -> onTrimClip(clip, newStart, newDur) },
                onTransitionClick = { onOpenTransitionPicker(clip) },
                modifier = Modifier
                    .offset(x = clipStartDp)
                    .width(clipWidthDp)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun FilmstripClipItem(
    clip: ClipModel,
    isSelected: Boolean,
    currentTimeMs: Long,
    pixelsPerSecond: Float,
    onSelect: () -> Unit,
    onTrim: (Long, Long) -> Unit,
    onTransitionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF1E293B))
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onSelect() }
            .testTag("clip_${clip.id}")
    ) {
        // Continuous filmstrip frames
        Row(modifier = Modifier.fillMaxSize()) {
            val resId = remember(clip.drawableResName) {
                if (clip.drawableResName != null) {
                    context.resources.getIdentifier(clip.drawableResName, "drawable", context.packageName)
                } else 0
            }

            if (clip.mediaUri != null && clip.mediaUri.isNotEmpty()) {
                AsyncImage(
                    model = clip.mediaUri,
                    contentDescription = clip.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.7f)
                )
            } else if (resId != 0) {
                // Repeat thumbnail slices to give filmstrip effect
                for (i in 0..10) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = clip.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(55.dp)
                            .fillMaxHeight()
                            .border(0.5.dp, Color.Black.copy(alpha = 0.3f))
                            .alpha(0.75f)
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color(clip.solidColorHex)))
            }
        }

        // Duration pill in bottom-left (e.g. 13.02) matching screenshot
        Surface(
            shape = RoundedCornerShape(3.dp),
            color = Color.Black.copy(alpha = 0.75f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
        ) {
            Text(
                text = String.format("%.2f", clip.durationMs / 1000f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }

        // Keyframe diamond markers
        for (kf in clip.keyframes) {
            val kfOffsetDp = with(density) {
                ((kf.timeOffsetMs / 1000f) * pixelsPerSecond).toDp()
            }
            Box(
                modifier = Modifier
                    .offset(x = kfOffsetDp - 4.dp)
                    .align(Alignment.TopStart)
                    .padding(top = 2.dp)
                    .size(8.dp)
                    .background(VNAmber, RoundedCornerShape(1.dp))
                    .border(0.5.dp, Color.Black, RoundedCornerShape(1.dp))
            )
        }

        // Transition indicator
        if (clip.transitionIn != TransitionType.NONE) {
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-8).dp)
                    .size(18.dp)
                    .clickable { onTransitionClick() },
                shape = CircleShape,
                color = Color(0xFF38BDF8)
            ) {
                Icon(
                    imageVector = Icons.Default.Transform,
                    contentDescription = "Transition",
                    tint = Color.Black,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }

        // Selected Trim Drag Handles (Chevron style < and >)
        if (isSelected) {
            // Left Chevron Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(14.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF38BDF8))
                    .pointerInput(clip.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaMs = ((dragAmount.x / pixelsPerSecond) * 1000f).toLong()
                            val newStart = (clip.startTimeMs + deltaMs).coerceAtLeast(0L)
                            val newDuration = (clip.durationMs - deltaMs).coerceAtLeast(500L)
                            onTrim(newStart, newDuration)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("<", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            // Right Chevron Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(14.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF38BDF8))
                    .pointerInput(clip.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaMs = ((dragAmount.x / pixelsPerSecond) * 1000f).toLong()
                            val newDuration = (clip.durationMs + deltaMs).coerceAtLeast(500L)
                            onTrim(clip.startTimeMs, newDuration)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(">", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
private fun TimelineClipItem(
    clip: ClipModel,
    isSelected: Boolean,
    currentTimeMs: Long,
    pixelsPerSecond: Float,
    onSelect: () -> Unit,
    onTrim: (Long, Long) -> Unit,
    onTransitionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Specific styling per track:
    // Audio: Purple with waveform
    // Text: Teal pill with title
    // PIP: Blue clip with star icon ★ 3.00
    val itemBgColor = when (clip.trackType) {
        TrackType.AUDIO -> Color(0xFF4C1D95)
        TrackType.TEXT -> Color(0xFF115E59)
        TrackType.PIP -> if (isSelected) Color(0xFF2563EB) else Color(0xFF1D4ED8)
        TrackType.MAIN_VIDEO -> Color(0xFF1E293B)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(itemBgColor)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onSelect() }
            .testTag("clip_${clip.id}")
    ) {
        when (clip.trackType) {
            TrackType.AUDIO -> {
                // Audio Waveform and audio filename
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = clip.name,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = String.format("%.2f", clip.durationMs / 1000f),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 9.sp
                    )
                }
            }

            TrackType.TEXT -> {
                // Subtitle / Text clip pill
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = clip.textConfig?.text ?: clip.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            TrackType.PIP -> {
                // PIP / Sticker clip: exactly as in screenshot: [ < ★ 3.00 > ]
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.2f", clip.durationMs / 1000f),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            TrackType.MAIN_VIDEO -> {}
        }

        // Selected Chevron Handles (< and >)
        if (isSelected) {
            // Left Chevron Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(14.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF38BDF8))
                    .pointerInput(clip.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaMs = ((dragAmount.x / pixelsPerSecond) * 1000f).toLong()
                            val newStart = (clip.startTimeMs + deltaMs).coerceAtLeast(0L)
                            val newDuration = (clip.durationMs - deltaMs).coerceAtLeast(500L)
                            onTrim(newStart, newDuration)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("<", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }

            // Right Chevron Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(14.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF38BDF8))
                    .pointerInput(clip.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaMs = ((dragAmount.x / pixelsPerSecond) * 1000f).toLong()
                            val newDuration = (clip.durationMs + deltaMs).coerceAtLeast(500L)
                            onTrim(clip.startTimeMs, newDuration)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(">", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

/**
 * Bottom Time Ruler with second timestamps (00:00, 00:01, 00:02...)
 */
@Composable
private fun TimeRuler(
    totalDurationMs: Long,
    pixelsPerSecond: Float,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(Color(0xFF0F1117))
            .pointerInput(totalDurationMs, pixelsPerSecond) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val clickX = change.position.x
                    val newTimeMs = ((clickX / pixelsPerSecond) * 1000f)
                        .toLong()
                        .coerceIn(0L, totalDurationMs)
                    onSeek(newTimeMs)
                }
            }
    ) {
        val totalSeconds = (totalDurationMs / 1000).toInt() + 2
        for (sec in 0..totalSeconds) {
            val x = sec * pixelsPerSecond
            // Major second tick
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(x, size.height * 0.3f),
                end = Offset(x, size.height),
                strokeWidth = 1.5f
            )

            // Sub-second minor ticks
            for (sub in 1..4) {
                val subX = x + (sub * (pixelsPerSecond / 5f))
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(subX, size.height * 0.6f),
                    end = Offset(subX, size.height),
                    strokeWidth = 1f
                )
            }
        }
    }
}
