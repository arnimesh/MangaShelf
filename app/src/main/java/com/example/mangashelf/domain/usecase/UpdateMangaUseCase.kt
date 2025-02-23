package com.example.mangashelf.domain.usecase

import com.example.mangashelf.domain.model.Manga
import com.example.mangashelf.domain.repository.MangaRepository
import javax.inject.Inject

class UpdateMangaUseCase @Inject constructor(
    private val repository: MangaRepository
) {
    suspend operator fun invoke(manga: Manga) {
        repository.updateManga(manga)
    }
} 