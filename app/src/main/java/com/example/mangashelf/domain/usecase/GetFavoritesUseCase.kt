package com.example.mangashelf.domain.usecase

import com.example.mangashelf.domain.model.Manga
import com.example.mangashelf.domain.repository.MangaRepository
import com.example.mangashelf.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val repository: MangaRepository
) {
    operator fun invoke(): Flow<Result<List<Manga>>> {
        return repository.getFavorites()
    }
} 