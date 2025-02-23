package com.example.mangashelf.data.mapper

import com.example.mangashelf.data.model.MangaEntity
import com.example.mangashelf.data.remote.dto.MangaDto
import com.example.mangashelf.domain.model.Manga

fun MangaDto.toMangaEntity() = MangaEntity(
    id = id,
    title = title,
    image = image,
    score = score,
    popularity = popularity,
    publishedChapterDate = publishedChapterDate,
    category = category,
    isFavorite = false,
    isRead = false
)

fun MangaEntity.toManga() = Manga(
    id = id,
    title = title,
    image = image,
    score = score,
    popularity = popularity,
    publishedChapterDate = publishedChapterDate,
    category = category,
    isFavorite = isFavorite,
    isRead = isRead
)

fun Manga.toMangaEntity() = MangaEntity(
    id = id,
    title = title,
    image = image,
    score = score,
    popularity = popularity,
    publishedChapterDate = publishedChapterDate,
    category = category,
    isFavorite = isFavorite,
    isRead = isRead
) 