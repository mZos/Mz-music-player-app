package com.zakariya.mymusicplayer.model

data class Song(
    val id: String,
    val name: String,
    val path: String,
    val artistName: String?,
    val albumName: String?
)