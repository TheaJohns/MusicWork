package com.example.musicplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicplayer.data.repository.MusicRepository
import com.example.musicplayer.domain.usecase.UseCases
import com.example.musicplayer.presentation.player.AudioPlayer

class MusicViewModelFactory(
    private val audioPlayer: AudioPlayer
) : ViewModelProvider.Factory {
    private val musicRepository = MusicRepository()
    private val useCases = UseCases(musicRepository)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            return MusicViewModel(useCases, audioPlayer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}