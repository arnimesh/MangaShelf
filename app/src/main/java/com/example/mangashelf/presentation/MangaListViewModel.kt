package com.example.mangashelf.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangashelf.domain.model.Manga
import com.example.mangashelf.domain.repository.MangaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}

sealed class UiState {
    data object Loading : UiState()
    data class Success(val data: List<Manga>) : UiState()
    data class Error(val exception: Throwable) : UiState()
}

@HiltViewModel
class MangaListViewModel @Inject constructor(
    private val repository: MangaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun refreshManga() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.refreshManga()
                repository.getAllManga().collect { result ->
                    _uiState.value = when (result) {
                        is com.example.mangashelf.util.Result.Success -> UiState.Success(result.data)
                        is com.example.mangashelf.util.Result.Error -> UiState.Error(Exception(result.message))
                        is com.example.mangashelf.util.Result.Loading -> UiState.Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e)
            }
        }
    }
} 