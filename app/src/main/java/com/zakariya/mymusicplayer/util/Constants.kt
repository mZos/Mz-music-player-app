package com.zakariya.mymusicplayer.util

import android.provider.MediaStore

object Constants {

    const val PREF_NAME = "com.zakariya.mymusicplayer.SHARED_PREF"
    const val POSITION_KEY = "position"
    const val SAVE_CURRENT_SONG_KEY = "Save currently playing song"
    const val REQ_CODE = 0
    const val NOTIFICATION_CHANNEL_ID = "Music Player"
    const val NOTIFICATION_CHANNEL_NAME = "Music"
    const val NOTIFICATION_ID = 1

    @Suppress("DEPRECATION")
    val baseProjection = arrayOf(
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.ArtistColumns.ARTIST,
        MediaStore.Audio.AlbumColumns.ALBUM
    )
}


