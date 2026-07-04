package com.example.musicplayer.data.model

data class SearchResponse(
    val code: Int,
    val message: String,
    val result: List<Song>
)