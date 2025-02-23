package com.example.mangashelf.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MangaDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("image") val image: String,
    @SerializedName("score") val score: Double,
    @SerializedName("popularity") val popularity: Int,
    @SerializedName("publishedChapterDate") val publishedChapterDate: Long,
    @SerializedName("category") val category: String
) 