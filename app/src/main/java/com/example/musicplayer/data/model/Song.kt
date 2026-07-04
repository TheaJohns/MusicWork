package com.example.musicplayer.data.model

data class Song(
    val author: String,
    val link: String,
    val pic: String,
    val type: String,
    val title: String,
    val lrc: String,
    val songid: Long,
    val url: String
)