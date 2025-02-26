package com.example.mangashelf.presentation.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mangashelf.R
import com.example.mangashelf.domain.model.Manga
import com.example.mangashelf.domain.usecase.GetMangaListUseCase
import com.example.mangashelf.domain.usecase.RefreshMangaUseCase
import com.example.mangashelf.domain.usecase.UpdateMangaUseCase
import com.example.mangashelf.presentation.model.MangaListUiState
import com.example.mangashelf.presentation.model.SortOrder
import com.example.mangashelf.presentation.model.SortType
import com.example.mangashelf.util.DateUtils
import com.example.mangashelf.util.Logger
import com.example.mangashelf.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaListViewModel @Inject constructor(
    private val getMangaListUseCase: GetMangaListUseCase,
    private val refreshMangaUseCase: RefreshMangaUseCase,
    private val updateMangaUseCase: UpdateMangaUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MangaListUiState(isLoading = true))
    val uiState: StateFlow<MangaListUiState> = _uiState.asStateFlow()

    private val _availableYears = MutableStateFlow<Set<Int>>(emptySet())
    val availableYears: StateFlow<Set<Int>> = _availableYears.asStateFlow()

    private var allManga: List<Manga> = emptyList()

    init {
        loadMangas()
    }

    fun loadMangas() {
        Logger.d("ViewModel: Starting loadMangas")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                getMangaListUseCase().collect { result ->
                    Logger.d("ViewModel: Received result: $result")
                    when (result) {
                        is Result.Loading -> {
                            Logger.d("ViewModel: Loading state")
                            _uiState.update { it.copy(isLoading = true, error = null) }
                        }
                        is Result.Success -> {
                            allManga = result.data
                            updateAvailableYears(result.data)
                            updateUiState(result.data)
                        }
                        is Result.Error -> {
                            Logger.e("ViewModel: Error state: ${result.message}")
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("Connection reset") == true -> 
                        "Connection failed. Please try again."
                    e.message?.contains("No internet connection") == true -> 
                        "No internet connection"
                    else -> 
                        "Failed to load manga"
                }
                _uiState.update { it.copy(
                    isLoading = false,
                    error = errorMessage
                ) }
            }
        }
    }

    fun refreshManga() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                refreshMangaUseCase()
                getMangaListUseCase().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            allManga = result.data
                            updateAvailableYears(result.data)
                            updateUiState(result.data)
                        }
                        is Result.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = result.message
                                )
                            }
                        }
                        is Result.Loading -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = true,
                                    error = null
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("Connection reset") == true -> 
                        "Connection failed. Please try again."
                    e.message?.contains("No internet connection") == true ->
                        "No internet connection"
                    else -> 
                        "Failed to load manga"
                }
                _uiState.update { it.copy(
                    isLoading = false,
                    error = errorMessage
                ) }
            }
        }
    }

    fun toggleFavorite(manga: Manga) {
        viewModelScope.launch {
            try {
                updateMangaUseCase(manga.copy(isFavorite = !manga.isFavorite))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateSortType(newSortType: SortType, newSortOrder: SortOrder) {
        _uiState.update { currentState -> 
            currentState.copy(
                sortType = newSortType,
                sortOrder = newSortOrder,
                isLoading = false,
                error = null,
                mangas = sortMangasWithParams(
                    mangas = filterMangasByYear(allManga, currentState.selectedYear),
                    sortType = newSortType,
                    sortOrder = newSortOrder
                )
            )
        }
    }

    fun toggleSortOrder() {
        _uiState.update { currentState -> 
            currentState.copy(
                sortOrder = if (currentState.sortOrder == SortOrder.ASCENDING) 
                    SortOrder.DESCENDING else SortOrder.ASCENDING,
                isLoading = false,
                error = null,
                mangas = sortMangas(filterMangasByYear(allManga, currentState.selectedYear))
            )
        }
    }

    fun selectYear(year: Int?) {
        _uiState.update { it.copy(selectedYear = year) }
        updateUiState(allManga)
    }

    private fun sortMangas(mangas: List<Manga>): List<Manga> {
        val comparator = when (_uiState.value.sortType) {
            SortType.YEAR -> compareBy<Manga> { 
                DateUtils.timestampToYear(it.publishedChapterDate)
            }
            SortType.SCORE -> compareByDescending<Manga> { it.score }
            SortType.POPULARITY -> compareByDescending<Manga> { it.popularity }
        }

        return if (_uiState.value.sortOrder == SortOrder.ASCENDING) {
            mangas.sortedWith(comparator)
        } else {
            mangas.sortedWith(comparator.reversed())
        }
    }

    private fun getYearsWithManga(mangas: List<Manga>): Set<Int> {
        return mangas.mapNotNull { manga ->
            DateUtils.timestampToYear(manga.publishedChapterDate)
        }.toSortedSet()
    }

    private fun filterMangasByYear(mangas: List<Manga>, selectedYear: Int?): List<Manga> {
        if (selectedYear == null) return mangas
        
        return mangas.filter { manga ->
            DateUtils.timestampToYear(manga.publishedChapterDate) == selectedYear
        }
    }

    private fun updateAvailableYears(mangas: List<Manga>) {
        _availableYears.value = getYearsWithManga(mangas)
    }

    private fun updateUiState(mangas: List<Manga>) {
        val filteredMangas = filterMangasByYear(mangas, _uiState.value.selectedYear)
        val sortedMangas = sortMangas(filteredMangas)
        _uiState.update { currentState ->
            currentState.copy(
                mangas = sortedMangas,
                isLoading = false
            )
        }
    }

    // New helper function to sort with specific parameters
    private fun sortMangasWithParams(
        mangas: List<Manga>,
        sortType: SortType,
        sortOrder: SortOrder
    ): List<Manga> {
        val comparator = when (sortType) {
            SortType.YEAR -> compareBy<Manga> { 
                DateUtils.timestampToYear(it.publishedChapterDate)
            }
            SortType.SCORE -> compareByDescending<Manga> { it.score }
            SortType.POPULARITY -> compareByDescending<Manga> { it.popularity }
        }

        return if (sortOrder == SortOrder.ASCENDING) {
            mangas.sortedWith(comparator)
        } else {
            mangas.sortedWith(comparator.reversed())
        }
    }

    private fun showToast(@StringRes messageResId: Int) {
        Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
    }
} 