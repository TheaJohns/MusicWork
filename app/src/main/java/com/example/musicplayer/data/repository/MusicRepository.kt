package com.example.musicplayer.data.repository

import com.example.musicplayer.data.api.ApiClient
import com.example.musicplayer.data.model.Song

class MusicRepository {
    suspend fun searchMusic(query: String): Result<List<Song>> {
        return try {
            val response = ApiClient.musicApiService.searchMusic(query)
            if (response.code == 200) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}