package com.example.musicplayer.domain.usecase

import com.example.musicplayer.data.model.Song
import com.example.musicplayer.data.repository.MusicRepository

class SearchMusicUseCase(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(query: String): Result<List<Song>> {
        return musicRepository.searchMusic(query)
    }
}