package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val aspectRatio: String,
    val durationMs: Long,
    val updatedAt: Long,
    val fps: Int,
    val thumbnailRes: String? = null,
    val clipsJson: String
)
