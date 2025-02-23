package com.example.mangashelf.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Manga(
    val id: String,
    val title: String,
    val image: String,
    val score: Double,
    val popularity: Int,
    val publishedChapterDate: Long,
    val category: String,
    val isFavorite: Boolean,
    val isRead: Boolean
) : Parcelable 