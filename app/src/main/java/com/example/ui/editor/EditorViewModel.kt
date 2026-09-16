package com.example.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class EditorSheet {
    NONE,
    COLOR_GRADING,
    KEYFRAME_CURVES,
    SPEED_FLOW,
    AUDIO_EFFECTS,
    TEXT_EDITOR,
    TRANSITION_PICKER,
    MEDIA_PICKER,
    EXPORT_DIALOG,
    OPACITY_SLIDER,
    CROP_DIALOG,
    BORDER_DIALOG
}

data class EditorUiState(
    val project: Project? = null,
    val currentTimeMs: Long = 6920L, // Matches 0:06.92 in screenshot!
    val isPlaying: Boolean = false,
    val selectedClipId: String? = null,
    val activeSheet: EditorSheet = EditorSheet.NONE,
    val isCompareMode: Boolean = false,
    val isMutedAll: Boolean = false,
    val pixelsPerSecond: Float = 80f,
    val clipForTransition: ClipModel? = null,
    val targetTrackForMedia: TrackType = TrackType.MAIN_VIDEO,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false
) {
    val selectedClip: ClipModel?
        get() = project?.clips?.find { it.id == selectedClipId }
}

class EditorViewModel(
    private val repository: ProjectRepository,
    private val projectId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private val undoStack = mutableListOf<Project>()
    private val redoStack = mutableListOf<Project>()

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            val project = repository.getProject(projectId)
            // Select sticker/PIP or text clip by default to match the screenshot
            val defaultSelected = project?.clips?.find { it.trackType == TrackType.PIP }
                ?: project?.clips?.find { it.trackType == TrackType.TEXT }
                ?: project?.clips?.firstOrNull()
            _uiState.update {
                it.copy(
                    project = project,
                    selectedClipId = defaultSelected?.id,
                    currentTimeMs = 6920L // 0:06.92 from screenshot
                )
            }
        }
    }

    private fun pushUndoState(currentProj: Project) {
        undoStack.add(currentProj)
        if (undoStack.size > 30) undoStack.removeAt(0)
        redoStack.clear()
        _uiState.update { it.copy(canUndo = undoStack.isNotEmpty(), canRedo = false) }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value.project ?: return
        val previous = undoStack.removeAt(undoStack.size - 1)
        redoStack.add(current)
        _uiState.update {
            it.copy(
                project = previous,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        saveProjectAsync(previous)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.project ?: return
        val next = redoStack.removeAt(redoStack.size - 1)
        undoStack.add(current)
        _uiState.update {
            it.copy(
                project = next,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        saveProjectAsync(next)
    }

    fun togglePlay() {
        if (_uiState.value.isPlaying) pause() else play()
    }

    fun play() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = true) }
        playbackJob = viewModelScope.launch {
            while (isActive) {
                delay(33L) // ~30 fps
                val current = _uiState.value.currentTimeMs
                val maxDur = _uiState.value.project?.maxDurationMs ?: 15000L
                val next = current + 33L
                if (next >= maxDur) {
                    _uiState.update { it.copy(currentTimeMs = 0L, isPlaying = false) }
                    break
                } else {
                    _uiState.update { it.copy(currentTimeMs = next) }
                }
            }
        }
    }

    fun pause() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun seek(timeMs: Long) {
        val maxDur = _uiState.value.project?.maxDurationMs ?: 15000L
        _uiState.update { it.copy(currentTimeMs = timeMs.coerceIn(0L, maxDur)) }
    }

    // Skip to previous cut / keyframe (⏮ button in VN)
    fun skipToPrevCut() {
        val proj = _uiState.value.project ?: return
        val current = _uiState.value.currentTimeMs
        val cutPoints = mutableSetOf<Long>(0L)
        for (c in proj.clips) {
            cutPoints.add(c.startTimeMs)
            cutPoints.add(c.endTimeMs)
            for (kf in c.keyframes) {
                cutPoints.add(c.startTimeMs + kf.timeOffsetMs)
            }
        }
        val prev = cutPoints.filter { it < current - 150L }.maxOrNull() ?: 0L
        seek(prev)
    }

    // Skip to next cut / keyframe (⏭ button in VN)
    fun skipToNextCut() {
        val proj = _uiState.value.project ?: return
        val current = _uiState.value.currentTimeMs
        val maxDur = proj.maxDurationMs
        val cutPoints = mutableSetOf<Long>(maxDur)
        for (c in proj.clips) {
            cutPoints.add(c.startTimeMs)
            cutPoints.add(c.endTimeMs)
            for (kf in c.keyframes) {
                cutPoints.add(c.startTimeMs + kf.timeOffsetMs)
            }
        }
        val next = cutPoints.filter { it > current + 150L }.minOrNull() ?: maxDur
        seek(next)
    }

    fun selectClip(clip: ClipModel?) {
        _uiState.update { it.copy(selectedClipId = clip?.id) }
    }

    fun setCompareMode(enabled: Boolean) {
        _uiState.update { it.copy(isCompareMode = enabled) }
    }

    fun toggleMuteAll() {
        _uiState.update { it.copy(isMutedAll = !it.isMutedAll) }
    }

    fun setZoom(pixelsPerSec: Float) {
        _uiState.update { it.copy(pixelsPerSecond = pixelsPerSec.coerceIn(30f, 200f)) }
    }

    fun openSheet(sheet: EditorSheet, targetTrack: TrackType = TrackType.MAIN_VIDEO) {
        _uiState.update { it.copy(activeSheet = sheet, targetTrackForMedia = targetTrack) }
    }

    fun closeSheet() {
        _uiState.update { it.copy(activeSheet = EditorSheet.NONE, clipForTransition = null) }
    }

    fun openTransitionPicker(clip: ClipModel) {
        _uiState.update { it.copy(activeSheet = EditorSheet.TRANSITION_PICKER, clipForTransition = clip) }
    }

    fun setAspectRatio(aspectRatio: AspectRatio) {
        val proj = _uiState.value.project ?: return
        pushUndoState(proj)
        val updated = proj.copy(aspectRatio = aspectRatio)
        _uiState.update { it.copy(project = updated) }
        saveProjectAsync(updated)
    }

    // Split clip at current playhead
    fun splitSelectedClip() {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return
        val currentT = _uiState.value.currentTimeMs

        if (currentT <= clip.startTimeMs + 200L || currentT >= clip.endTimeMs - 200L) return

        pushUndoState(proj)
        val firstDuration = currentT - clip.startTimeMs
        val secondDuration = clip.durationMs - firstDuration

        val firstClip = clip.copy(
            durationMs = firstDuration,
            keyframes = clip.keyframes.filter { it.timeOffsetMs <= firstDuration }
        )
        val secondClip = clip.copy(
            id = "clip_" + UUID.randomUUID().toString().take(6),
            startTimeMs = currentT,
            durationMs = secondDuration,
            keyframes = clip.keyframes
                .filter { it.timeOffsetMs > firstDuration }
                .map { it.copy(timeOffsetMs = it.timeOffsetMs - firstDuration) },
            transitionIn = TransitionType.NONE
        )

        val updatedClips = proj.clips.map { if (it.id == clip.id) firstClip else it } + secondClip
        val updatedProject = proj.copy(clips = updatedClips)
        _uiState.update { it.copy(project = updatedProject, selectedClipId = secondClip.id) }
        saveProjectAsync(updatedProject)
    }

    fun deleteSelectedClip() {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return
        pushUndoState(proj)
        val updatedClips = proj.clips.filter { it.id != clip.id }
        val updatedProject = proj.copy(clips = updatedClips)
        _uiState.update { it.copy(project = updatedProject, selectedClipId = updatedClips.firstOrNull()?.id) }
        saveProjectAsync(updatedProject)
    }

    fun duplicateSelectedClip() {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return
        pushUndoState(proj)
        val duplicate = clip.copy(
            id = "clip_" + UUID.randomUUID().toString().take(6),
            name = "${clip.name} (Copy)",
            startTimeMs = clip.endTimeMs
        )
        val updatedClips = proj.clips + duplicate
        val updatedProject = proj.copy(clips = updatedClips)
        _uiState.update { it.copy(project = updatedProject, selectedClipId = duplicate.id) }
        saveProjectAsync(updatedProject)
    }

    fun trimClip(clip: ClipModel, newStartMs: Long, newDurationMs: Long) {
        val proj = _uiState.value.project ?: return
        val updated = clip.copy(startTimeMs = newStartMs, durationMs = newDurationMs)
        val updatedClips = proj.clips.map { if (it.id == clip.id) updated else it }
        val updatedProject = proj.copy(clips = updatedClips)
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun toggleLockSelectedClip() {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return
        pushUndoState(proj)
        val updated = clip.copy(isLocked = !clip.isLocked)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updated else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun toggleMirrorSelectedClip() {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return
        pushUndoState(proj)
        val updated = clip.copy(isMirrored = !clip.isMirrored)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updated else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun toggleFlipSelectedClip() {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return
        pushUndoState(proj)
        val updated = clip.copy(isFlipped = !clip.isFlipped)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updated else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun rotateSelectedClip90() {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return
        pushUndoState(proj)
        val updated = clip.copy(rotationAngle = (clip.rotationAngle + 90f) % 360f)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updated else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun toggleMainTrackPip() {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return
        pushUndoState(proj)
        val newTrack = if (clip.trackType == TrackType.MAIN_VIDEO) TrackType.PIP else TrackType.MAIN_VIDEO
        val updated = clip.copy(trackType = newTrack)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updated else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun setOpacity(opacity: Float) {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return
        val updated = clip.copy(opacity = opacity.coerceIn(0f, 1f))
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updated else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun updateTransform(scale: Float, rotation: Float, tx: Float, ty: Float) {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return
        val relTime = (_uiState.value.currentTimeMs - clip.startTimeMs).coerceIn(0L, clip.durationMs)

        val existingKf = clip.findKeyframeAtOrNear(relTime)
        val updatedKeyframes = if (existingKf != null) {
            clip.keyframes.map {
                if (it.id == existingKf.id) it.copy(scale = scale, rotation = rotation, translationX = tx, translationY = ty)
                else it
            }
        } else {
            clip.keyframes + Keyframe(
                id = "kf_" + System.currentTimeMillis(),
                timeOffsetMs = relTime,
                scale = scale,
                rotation = rotation,
                translationX = tx,
                translationY = ty
            )
        }

        val updatedClip = clip.copy(keyframes = updatedKeyframes)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updatedClip else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun addOrUpdateKeyframe(keyframe: Keyframe) {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return

        pushUndoState(proj)
        val existing = clip.keyframes.find { it.id == keyframe.id || Math.abs(it.timeOffsetMs - keyframe.timeOffsetMs) < 100L }
        val updatedKeyframes = if (existing != null) {
            clip.keyframes.map { if (it.id == existing.id) keyframe else it }
        } else {
            clip.keyframes + keyframe
        }.sortedBy { it.timeOffsetMs }

        val updatedClip = clip.copy(keyframes = updatedKeyframes)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updatedClip else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun removeKeyframe(keyframeId: String) {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return

        pushUndoState(proj)
        val updatedKeyframes = clip.keyframes.filter { it.id != keyframeId }
        val updatedClip = clip.copy(keyframes = updatedKeyframes)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updatedClip else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun updateColorGrading(params: ColorGradingParams) {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return

        val updatedClip = clip.copy(colorGrading = params)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updatedClip else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun updateSpeed(speed: Float) {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return

        val updatedClip = clip.copy(speed = speed)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updatedClip else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun updateVolume(volume: Float) {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return

        val updatedClip = clip.copy(volume = volume)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updatedClip else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun updateMute(isMuted: Boolean) {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return

        val updatedClip = clip.copy(isMuted = isMuted)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updatedClip else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun updateTextConfig(config: TextClipConfig) {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.selectedClip ?: return

        val updatedClip = clip.copy(textConfig = config)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updatedClip else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun updateTransition(transition: TransitionType, durationMs: Long) {
        val proj = _uiState.value.project ?: return
        val clip = _uiState.value.clipForTransition ?: return

        val updatedClip = clip.copy(transitionIn = transition, transitionDurationMs = durationMs)
        val updatedProject = proj.copy(clips = proj.clips.map { if (it.id == clip.id) updatedClip else it })
        _uiState.update { it.copy(project = updatedProject) }
        saveProjectAsync(updatedProject)
    }

    fun addMedia(
        mediaUri: String?,
        drawableResName: String?,
        name: String,
        type: MediaType,
        durationMs: Long
    ) {
        val proj = _uiState.value.project ?: return
        val trackType = _uiState.value.targetTrackForMedia
        val currentT = _uiState.value.currentTimeMs

        pushUndoState(proj)
        val newClip = ClipModel(
            id = "clip_" + UUID.randomUUID().toString().take(6),
            name = name,
            trackType = trackType,
            mediaType = type,
            mediaUri = mediaUri,
            drawableResName = drawableResName,
            startTimeMs = currentT,
            durationMs = durationMs
        )
        val updatedClips = proj.clips + newClip
        val updatedProject = proj.copy(clips = updatedClips)
        _uiState.update { it.copy(project = updatedProject, selectedClipId = newClip.id) }
        saveProjectAsync(updatedProject)
    }

    fun addTextClip() {
        val proj = _uiState.value.project ?: return
        val currentT = _uiState.value.currentTimeMs

        pushUndoState(proj)
        val newClip = ClipModel(
            id = "clip_txt_" + UUID.randomUUID().toString().take(6),
            name = "Input Title",
            trackType = TrackType.TEXT,
            mediaType = MediaType.TEXT,
            startTimeMs = currentT,
            durationMs = 4000L,
            textConfig = TextClipConfig("Input Title", fontSizeSp = 32f, textColorHex = 0xFFFFFFFF)
        )
        val updatedClips = proj.clips + newClip
        val updatedProject = proj.copy(clips = updatedClips)
        _uiState.update { it.copy(project = updatedProject, selectedClipId = newClip.id) }
        saveProjectAsync(updatedProject)
    }

    private fun saveProjectAsync(project: Project) {
        viewModelScope.launch {
            repository.saveProject(project)
        }
    }

    class Factory(
        private val repository: ProjectRepository,
        private val projectId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditorViewModel(repository, projectId) as T
        }
    }
}
