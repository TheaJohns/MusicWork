package com.example.musicplayer.data.api

import com.example.musicplayer.data.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {
    @GET("searchMusic")
    suspend fun searchMusic(@Query("name") name: String): SearchResponse
}