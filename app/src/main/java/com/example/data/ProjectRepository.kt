package com.example.data

import com.example.model.AspectRatio
import com.example.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class ProjectRepository(private val dao: ProjectDao) {

    val allProjects: Flow<List<Project>> = dao.getAllProjects().map { entities ->
        entities.map { toDomain(it) }
    }

    suspend fun ensureInitialData() = withContext(Dispatchers.IO) {
        val count = dao.getCount()
        if (count == 0) {
            val defaults = DemoMediaProvider.createInitialProjects()
            for (p in defaults) {
                dao.insertProject(toEntity(p))
            }
        }
    }

    suspend fun getProject(id: String): Project? = withContext(Dispatchers.IO) {
        val entity = dao.getProjectById(id) ?: return@withContext null
        toDomain(entity)
    }

    suspend fun saveProject(project: Project) = withContext(Dispatchers.IO) {
        dao.insertProject(toEntity(project.copy(updatedAt = System.currentTimeMillis())))
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        dao.deleteProjectById(id)
    }

    suspend fun duplicateProject(id: String): Project? = withContext(Dispatchers.IO) {
        val original = getProject(id) ?: return@withContext null
        val newId = "proj_" + UUID.randomUUID().toString().take(8)
        val copy = original.copy(
            id = newId,
            name = "${original.name} (Copy)",
            updatedAt = System.currentTimeMillis()
        )
        saveProject(copy)
        copy
    }

    suspend fun createNewProject(name: String, aspectRatio: AspectRatio): Project = withContext(Dispatchers.IO) {
        val id = "proj_" + UUID.randomUUID().toString().take(8)
        val initialClip = DemoMediaProvider.STOCK_LIBRARY.first()
        val defaultClips = listOf(
            com.example.model.ClipModel(
                id = "clip_" + UUID.randomUUID().toString().take(6),
                name = initialClip.title,
                trackType = com.example.model.TrackType.MAIN_VIDEO,
                mediaType = initialClip.mediaType,
                drawableResName = initialClip.drawableResName,
                startTimeMs = 0L,
                durationMs = 5000L
            )
        )
        val project = Project(
            id = id,
            name = name,
            aspectRatio = aspectRatio,
            durationMs = 5000L,
            updatedAt = System.currentTimeMillis(),
            fps = 30,
            clips = defaultClips
        )
        saveProject(project)
        project
    }

    private fun toDomain(entity: ProjectEntity): Project {
        val ratio = try {
            AspectRatio.valueOf(entity.aspectRatio)
        } catch (e: Exception) {
            AspectRatio.RATIO_16_9
        }
        val clips = ProjectJsonAdapter.jsonToClips(entity.clipsJson)
        return Project(
            id = entity.id,
            name = entity.name,
            aspectRatio = ratio,
            durationMs = entity.durationMs,
            updatedAt = entity.updatedAt,
            fps = entity.fps,
            clips = clips
        )
    }

    private fun toEntity(project: Project): ProjectEntity {
        val thumbnail = project.clips.firstOrNull { it.trackType == com.example.model.TrackType.MAIN_VIDEO }?.drawableResName
        return ProjectEntity(
            id = project.id,
            name = project.name,
            aspectRatio = project.aspectRatio.name,
            durationMs = project.maxDurationMs,
            updatedAt = project.updatedAt,
            fps = project.fps,
            thumbnailRes = thumbnail,
            clipsJson = ProjectJsonAdapter.clipsToJson(project.clips)
        )
    }
}
