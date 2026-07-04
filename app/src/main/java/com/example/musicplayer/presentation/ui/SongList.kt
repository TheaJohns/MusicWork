package com.example.musicplayer.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.musicplayer.data.model.Song

@Composable
fun SongList(
    songs: List<Song>,
    onPlay: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        Text(
            text = "暂无搜索结果",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )
    } else {
        LazyColumn {
            items(songs) { song ->
                SongItem(song = song, onPlay = onPlay)
            }
        }
    }
}