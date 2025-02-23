package com.example.mangashelf.data.remote

import com.example.mangashelf.data.remote.dto.MangaDto
import com.example.mangashelf.util.Logger
import retrofit2.Response
import retrofit2.http.GET

interface MangaApiService {
    @GET("b/KEJO")
    suspend fun getMangaList(): Response<List<MangaDto>>
} 