package com.example.mangashelf.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manga")
data class MangaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val image: String,
    val score: Double,
    val popularity: Int,
    val publishedChapterDate: Long,
    val category: String,
    val isFavorite: Boolean = false,
    val isRead: Boolean = false
) 