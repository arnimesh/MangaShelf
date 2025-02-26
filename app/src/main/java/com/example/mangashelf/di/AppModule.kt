package com.example.mangashelf.di

import android.content.Context
import androidx.room.Room
import com.example.mangashelf.data.local.DataStoreManager
import com.example.mangashelf.data.local.MangaDao
import com.example.mangashelf.data.local.MangaDatabase
import com.example.mangashelf.data.remote.MangaApiService
import com.example.mangashelf.data.repository.MangaRepositoryImpl
import com.example.mangashelf.domain.repository.MangaRepository
import com.example.mangashelf.util.NetworkUtils
import com.example.mangashelf.util.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideMangaDatabase(
        @ApplicationContext context: Context
    ): MangaDatabase {
        return Room.databaseBuilder(
            context,
            MangaDatabase::class.java,
            "manga_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMangaApi(): MangaApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request()
                Logger.d("API: Making request to ${request.url}")
                try {
                    val response = chain.proceed(request)
                    Logger.d("API: Response received, code=${response.code}")
                    response
                } catch (e: Exception) {
                    Logger.e("API: Request failed", e)
                    throw e
                }
            }
            .hostnameVerifier { _, _ -> true }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://www.jsonkeeper.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MangaApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMangaDao(database: MangaDatabase): MangaDao {
        return database.mangaDao()
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(
        @ApplicationContext context: Context
    ): NetworkUtils {
        return NetworkUtils(context)
    }

    @Provides
    @Singleton
    fun provideDataStoreManager(
        @ApplicationContext context: Context
    ): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideMangaRepository(
        api: MangaApiService,
        dao: MangaDao,
        networkUtils: NetworkUtils,
        dataStoreManager: DataStoreManager
    ): MangaRepository {
        return MangaRepositoryImpl(api, dao, networkUtils, dataStoreManager)
    }
} 