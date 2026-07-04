package com.example.musicplayer

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.musicplayer.presentation.player.AudioPlayer
import com.example.musicplayer.presentation.ui.App
import com.example.musicplayer.presentation.viewmodel.MusicViewModelFactory

class MainActivity : ComponentActivity() {
    private val audioPlayer = AudioPlayer()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(viewModelFactory = MusicViewModelFactory(audioPlayer))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }
}