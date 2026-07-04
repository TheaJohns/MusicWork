package com.example.musicplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.domain.usecase.UseCases
import com.example.musicplayer.presentation.player.AudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MusicViewModel(
    private val useCases: UseCases,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val currentSong: StateFlow<Song?> = audioPlayer.currentSong
    val isPlaying: StateFlow<Boolean> = audioPlayer.isPlaying
    val currentPosition: StateFlow<Int> = audioPlayer.currentPosition
    val duration: StateFlow<Int> = audioPlayer.duration
    val isPreparing: StateFlow<Boolean> = audioPlayer.isPreparing

    init {
        viewModelScope.launch {
            audioPlayer.error.collectLatest { error ->
                _error.value = error
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchMusic(query: String) {
        if (query.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            val result = useCases.searchMusic(query)
            _isLoading.value = false
            result.onSuccess { songs ->
                _searchResults.value = songs
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun playSong(song: Song) {
        audioPlayer.play(song)
    }

    fun togglePlayPause() {
        if (audioPlayer.isPlaying.value) {
            audioPlayer.pause()
        } else {
            audioPlayer.resume()
        }
    }

    fun stopPlaying() {
        audioPlayer.stop()
    }

    fun updateProgress() {
        audioPlayer.updateProgress()
    }

    fun seekTo(position: Int) {
        audioPlayer.seekTo(position)
    }
}