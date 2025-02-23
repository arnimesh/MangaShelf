package com.example.mangashelf.data.repository

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.example.mangashelf.R
import com.example.mangashelf.data.local.DataStoreManager
import com.example.mangashelf.data.local.MangaDao
import com.example.mangashelf.data.mapper.toManga
import com.example.mangashelf.data.mapper.toMangaEntity
import com.example.mangashelf.data.remote.MangaApiService
import com.example.mangashelf.domain.model.Manga
import com.example.mangashelf.domain.repository.MangaRepository
import com.example.mangashelf.util.NetworkUtils
import com.example.mangashelf.util.Result
import com.example.mangashelf.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MangaRepositoryImpl @Inject constructor(
    private val api: MangaApiService,
    private val dao: MangaDao,
    private val networkUtils: NetworkUtils,
    private val dataStoreManager: DataStoreManager,
    @ApplicationContext private val context: Context
) : MangaRepository {

    companion object {
        private const val CACHE_TIMEOUT_HOURS = 24L
    }

    override fun getAllManga(): Flow<Result<List<Manga>>> = flow {
        Logger.d("Repository: Starting getAllManga")
        emit(Result.Loading())
        
        // Get initial database data
        val initialData = dao.getAllManga().first()
        Logger.d("Repository: Database returned ${initialData.size} items")
        
        // If database is empty or cache is stale, refresh from API
        if (initialData.isEmpty() || shouldRefreshCache()) {
            Logger.d("Repository: Database empty or cache stale, refreshing from API")
            try {
                refreshManga()
            } catch (e: Exception) {
                Logger.e("Repository: Refresh failed", e)
                emit(Result.Error("Unable to refresh: ${e.localizedMessage}"))
            }
        }
        
        // Emit database updates
        emitAll(dao.getAllManga()
            .map { entities -> 
                Logger.d("Repository: Database returned ${entities.size} items")
                Result.Success(entities.map { it.toManga() })
            }
        )
    }.catch { e ->
        Logger.e("Repository: Error in getAllManga", e)
        emit(Result.Error("Error: ${e.localizedMessage}"))
    }

    override fun getFavorites(): Flow<Result<List<Manga>>> = flow {
        emit(Result.Loading())
        emitAll(dao.getFavorites()
            .map { entities -> Result.Success(entities.map { it.toManga() }) }
        )
    }.catch { e ->
        emit(Result.Error("Error: ${e.localizedMessage}"))
    }

    override suspend fun refreshManga() {
        if (!networkUtils.isNetworkAvailable()) {
            Logger.d("Repository: No network available")
            showToast(R.string.error_no_internet)
            throw Exception("No internet connection")
        }

        try {
            val response = api.getMangaList()
            Logger.d("Repository: API response received, success=${response.isSuccessful}")
            if (response.isSuccessful) {
                response.body()?.let { dtos ->
                    Logger.d("Repository: Received ${dtos.size} items from API")
                    dao.insertAll(dtos.map { it.toMangaEntity() })
                    dataStoreManager.updateLastSyncTime(System.currentTimeMillis())
                } ?: run {
                    showToast(R.string.error_loading_manga)
                    throw Exception("Empty response from server")
                }
            } else {
                showToast(R.string.error_loading_manga)
                throw Exception("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            Logger.e("Repository: Error in refreshManga", e)
            showToast(R.string.error_loading_manga)
            throw Exception("Failed to refresh: ${e.localizedMessage}")
        }
    }

    override suspend fun updateManga(manga: Manga) {
        try {
            dao.updateManga(manga.toMangaEntity())
        } catch (e: Exception) {
            throw Exception("Failed to update manga: ${e.localizedMessage}")
        }
    }

    private suspend fun shouldRefreshCache(): Boolean {
        val lastSync = dataStoreManager.lastSyncTime.first()
        val timeSinceLastSync = System.currentTimeMillis() - lastSync
        return timeSinceLastSync > TimeUnit.HOURS.toMillis(CACHE_TIMEOUT_HOURS)
    }

    private suspend fun showToast(@StringRes messageResId: Int) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
        }
    }
} 