package com.example.mangashelf.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangashelf.domain.model.Manga
import com.example.mangashelf.domain.usecase.UpdateMangaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaDetailViewModel @Inject constructor(
    private val updateMangaUseCase: UpdateMangaUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _manga = MutableStateFlow<Manga?>(null)
    val manga: StateFlow<Manga?> = _manga.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun setManga(manga: Manga) {
        _manga.value = manga
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                _manga.value?.let { manga ->
                    val updatedManga = manga.copy(isFavorite = !manga.isFavorite)
                    updateMangaUseCase(updatedManga)
                    _manga.value = updatedManga
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun toggleReadStatus() {
        viewModelScope.launch {
            try {
                _manga.value?.let { manga ->
                    val updatedManga = manga.copy(isRead = !manga.isRead)
                    updateMangaUseCase(updatedManga)
                    _manga.value = updatedManga
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
} 