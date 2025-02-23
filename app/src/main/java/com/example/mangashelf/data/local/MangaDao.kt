package com.example.mangashelf.data.local

import androidx.room.*
import com.example.mangashelf.data.model.MangaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga")
    fun getAllManga(): Flow<List<MangaEntity>>

    @Query("SELECT * FROM manga WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<MangaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mangas: List<MangaEntity>)

    @Update
    suspend fun updateManga(manga: MangaEntity)
} 