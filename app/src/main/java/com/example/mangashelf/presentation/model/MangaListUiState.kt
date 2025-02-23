package com.example.mangashelf.presentation.model

import com.example.mangashelf.domain.model.Manga

data class MangaListUiState(
    val mangas: List<Manga> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedYear: Int? = null,
    val sortType: SortType = SortType.YEAR,
    val sortOrder: SortOrder = SortOrder.ASCENDING
)

enum class SortType {
    YEAR, SCORE, POPULARITY
}

enum class SortOrder {
    ASCENDING, DESCENDING
} 