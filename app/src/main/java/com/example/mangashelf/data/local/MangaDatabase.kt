package com.example.mangashelf.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mangashelf.data.model.MangaEntity

@Database(entities = [MangaEntity::class], version = 1)
abstract class MangaDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
} 