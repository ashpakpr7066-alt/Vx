package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.engine.ColorGradingEngine
import com.example.engine.KeyframeInterpolator
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.atan2

@Composable
fun PlayerCanvas(
    project: Project,
    currentTimeMs: Long,
    selectedClip: ClipModel?,
    isCompareMode: Boolean,
    onClipSelected: (ClipModel) -> Unit,
    onTransformChanged: (scale: Float, rotation: Float, translationX: Float, translationY: Float) -> Unit,
    onOpenTextEditor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val aspectRatio = project.aspectRatio
    var isFullscreen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(VNBackground)
            .padding(if (isFullscreen) 0.dp else 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Aspect Ratio Box Container
        Box(
            modifier = Modifier
                .aspectRatio(aspectRatio.ratio)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .border(1.dp, VNBorder, RoundedCornerShape(8.dp))
                .clipToBounds()
                .testTag("player_viewport")
        ) {
            // Active clips at currentTimeMs
            val activeClips = project.clips.filter { clip ->
                currentTimeMs in clip.startTimeMs until clip.endTimeMs
            }

            val mainVideoClips = activeClips.filter { it.trackType == TrackType.MAIN_VIDEO }
            val pipClips = activeClips.filter { it.trackType == TrackType.PIP }
            val textClips = activeClips.filter { it.trackType == TrackType.TEXT }

            // 1. Main Video Layer
            for (clip in mainVideoClips) {
                RenderClipLayer(
                    clip = clip,
                    currentTimeMs = currentTimeMs,
                    isSelected = clip.id == selectedClip?.id,
                    isCompareMode = isCompareMode,
                    onClick = { onClipSelected(clip) },
                    onTransformChanged = onTransformChanged
                )
            }

            // 2. PIP Overlay Layer
            for (clip in pipClips) {
                RenderClipLayer(
                    clip = clip,
                    currentTimeMs = currentTimeMs,
                    isSelected = clip.id == selectedClip?.id,
                    isCompareMode = isCompareMode,
                    onClick = { onClipSelected(clip) },
                    onTransformChanged = onTransformChanged
                )
            }

            // 3. Text & Subtitle Layer
            for (clip in textClips) {
                RenderTextLayer(
                    clip = clip,
                    currentTimeMs = currentTimeMs,
                    isSelected = clip.id == selectedClip?.id,
                    onClick = {
                        onClipSelected(clip)
                        onOpenTextEditor()
                    },
                    onTransformChanged = onTransformChanged
                )
            }

            // Safe Margins / Grid overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
            )

            // Aspect Ratio badge in top-left corner
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = aspectRatio.label,
                    color = VNTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Compare badge if active
            if (isCompareMode) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = VNAmber.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "ORIGINAL (RAW)",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Fullscreen Expand Toggle in bottom-right corner (matches VN screenshot)
            IconButton(
                onClick = { isFullscreen = !isFullscreen },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RenderClipLayer(
    clip: ClipModel,
    currentTimeMs: Long,
    isSelected: Boolean,
    isCompareMode: Boolean,
    onClick: () -> Unit,
    onTransformChanged: (scale: Float, rotation: Float, translationX: Float, translationY: Float) -> Unit
) {
    val context = LocalContext.current
    val transform = KeyframeInterpolator.interpolate(clip, currentTimeMs)
    val colorFilter = if (isCompareMode) {
        null
    } else {
        ColorGradingEngine.createColorFilter(clip.colorGrading)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = transform.scale * (if (clip.isMirrored) -1f else 1f)
                scaleY = transform.scale * (if (clip.isFlipped) -1f else 1f)
                rotationZ = transform.rotation + clip.rotationAngle
                translationX = transform.translationX
                translationY = transform.translationY
                alpha = transform.opacity * clip.opacity
            }
            .pointerInput(clip.id, isSelected) {
                if (isSelected && !clip.isLocked) {
                    detectTransformGestures { _, pan, zoom, rotation ->
                        onTransformChanged(
                            (transform.scale * zoom).coerceIn(0.2f, 4.0f),
                            transform.rotation + rotation,
                            transform.translationX + pan.x,
                            transform.translationY + pan.y
                        )
                    }
                }
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Render Image, Video Frame, or Solid Color
        if (clip.mediaUri != null && clip.mediaUri.isNotEmpty()) {
            AsyncImage(
                model = clip.mediaUri,
                contentDescription = clip.name,
                contentScale = if (clip.trackType == TrackType.PIP) ContentScale.Fit else ContentScale.Crop,
                colorFilter = colorFilter,
                modifier = Modifier.fillMaxSize()
            )
        } else if (clip.drawableResName != null) {
            val resId = remember(clip.drawableResName) {
                context.resources.getIdentifier(clip.drawableResName, "drawable", context.packageName)
            }
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = clip.name,
                    contentScale = if (clip.trackType == TrackType.PIP) ContentScale.Fit else ContentScale.Crop,
                    colorFilter = colorFilter,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(clip.solidColorHex))
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(clip.solidColorHex))
            )
        }

        // VN Signature Bounding Box Widget for selected PIP / overlay clips
        if (isSelected && clip.trackType == TrackType.PIP) {
            VNTransformBoundingBox(
                transform = transform,
                onTransformChanged = onTransformChanged
            )
        }
    }
}

@Composable
private fun RenderTextLayer(
    clip: ClipModel,
    currentTimeMs: Long,
    isSelected: Boolean,
    onClick: () -> Unit,
    onTransformChanged: (scale: Float, rotation: Float, translationX: Float, translationY: Float) -> Unit
) {
    val transform = KeyframeInterpolator.interpolate(clip, currentTimeMs)
    val config = clip.textConfig ?: TextClipConfig()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = transform.scale
                scaleY = transform.scale
                rotationZ = transform.rotation + clip.rotationAngle
                translationX = transform.translationX
                translationY = transform.translationY
                alpha = transform.opacity * clip.opacity
            }
            .pointerInput(clip.id, isSelected) {
                if (isSelected && !clip.isLocked) {
                    detectTransformGestures { _, pan, zoom, rotation ->
                        onTransformChanged(
                            (transform.scale * zoom).coerceIn(0.2f, 4.0f),
                            transform.rotation + rotation,
                            transform.translationX + pan.x,
                            transform.translationY + pan.y
                        )
                    }
                }
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val textAlign = when (config.alignment) {
            0 -> TextAlign.Start
            2 -> TextAlign.End
            else -> TextAlign.Center
        }

        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (config.hasBackgroundBox) Color(config.backgroundColorHex) else Color.Transparent,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = config.text,
                    fontSize = config.fontSizeSp.sp,
                    fontWeight = if (config.isBold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (config.isItalic) FontStyle.Italic else FontStyle.Normal,
                    color = Color(config.textColorHex),
                    textAlign = textAlign,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Authentic VN Bounding Box matching screenshot
            if (isSelected) {
                VNTransformBoundingBox(
                    transform = transform,
                    onTransformChanged = onTransformChanged
                )
            }
        }
    }
}

/**
 * Authentic VN Video Editor Bounding Box
 * - Crisp white rectangular border
 * - 4 solid white corner circles for resizing
 * - Top center drag handle pill
 * - Bottom center rotation handle: circular white button with rotation icon (⟳)
 */
@Composable
private fun VNTransformBoundingBox(
    transform: TransformState,
    onTransformChanged: (scale: Float, rotation: Float, translationX: Float, translationY: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(1.5.dp, Color.White)
    ) {
        // 1. Top-Left Corner Handle
        Box(
            modifier = Modifier
                .offset(x = (-7).dp, y = (-7).dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.TopStart)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaScale = -dragAmount.y * 0.01f
                        onTransformChanged(
                            (transform.scale + deltaScale).coerceIn(0.2f, 4.0f),
                            transform.rotation,
                            transform.translationX,
                            transform.translationY
                        )
                    }
                }
        )

        // 2. Top-Right Corner Handle
        Box(
            modifier = Modifier
                .offset(x = 7.dp, y = (-7).dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.TopEnd)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaScale = -dragAmount.y * 0.01f
                        onTransformChanged(
                            (transform.scale + deltaScale).coerceIn(0.2f, 4.0f),
                            transform.rotation,
                            transform.translationX,
                            transform.translationY
                        )
                    }
                }
        )

        // 3. Bottom-Left Corner Handle
        Box(
            modifier = Modifier
                .offset(x = (-7).dp, y = 7.dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.BottomStart)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaScale = dragAmount.y * 0.01f
                        onTransformChanged(
                            (transform.scale + deltaScale).coerceIn(0.2f, 4.0f),
                            transform.rotation,
                            transform.translationX,
                            transform.translationY
                        )
                    }
                }
        )

        // 4. Bottom-Right Corner Handle
        Box(
            modifier = Modifier
                .offset(x = 7.dp, y = 7.dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(Color.White)
                .align(Alignment.BottomEnd)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaScale = dragAmount.y * 0.01f
                        onTransformChanged(
                            (transform.scale + deltaScale).coerceIn(0.2f, 4.0f),
                            transform.rotation,
                            transform.translationX,
                            transform.translationY
                        )
                    }
                }
        )

        // 5. Top Center Horizontal Pill Handle
        Box(
            modifier = Modifier
                .offset(y = (-4).dp)
                .width(22.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .align(Alignment.TopCenter)
        )

        // 6. Bottom Rotation Handle (circle with circular arrow icon ⟳ hanging below)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Connecting stem
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(10.dp)
                    .background(Color.White)
            )
            // Circular rotate button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val angleDelta = dragAmount.x * 0.8f
                            onTransformChanged(
                                transform.scale,
                                (transform.rotation + angleDelta),
                                transform.translationX,
                                transform.translationY
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rotate",
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
