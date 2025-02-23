package com.example.mangashelf.domain.usecase

import com.example.mangashelf.domain.repository.MangaRepository
import javax.inject.Inject

class RefreshMangaUseCase @Inject constructor(
    private val repository: MangaRepository
) {
    suspend operator fun invoke() {
        repository.refreshManga()
    }
} 