package com.example.musicplayer.domain.usecase

import com.example.musicplayer.data.repository.MusicRepository

class UseCases(
    musicRepository: MusicRepository
) {
    val searchMusic = SearchMusicUseCase(musicRepository)
}