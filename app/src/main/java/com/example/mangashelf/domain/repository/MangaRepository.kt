package com.example.mangashelf.domain.repository

import com.example.mangashelf.domain.model.Manga
import com.example.mangashelf.util.Result
import kotlinx.coroutines.flow.Flow

interface MangaRepository {
    fun getAllManga(): Flow<Result<List<Manga>>>
    fun getFavorites(): Flow<Result<List<Manga>>>
    suspend fun refreshManga()
    suspend fun updateManga(manga: Manga)
} 