package com.zakariya.mymusicplayer.util

import android.provider.MediaStore

object Constants {

    private const val packageName = "com.zakariya.mymusicplayer"
    const val PREF_NAME = "$packageName.SHARED_PREF"
    const val POSITION_KEY = "${packageName}.position"
    const val CURRENT_SONG_DURATION_KEY = "$packageName.currentSongDurationKey"
    const val SAVE_CURRENT_SONG_KEY = "Save currently playing song"
    const val REQ_CODE = 0
    const val NOTIFICATION_CHANNEL_ID = "${packageName}.Music Player"
    const val NOTIFICATION_CHANNEL_NAME = "${packageName}.Music"
    const val NOTIFICATION_ID = 1

    @Suppress("DEPRECATION")
    val baseProjection = arrayOf(
        MediaStore.Audio.AudioColumns.TITLE,
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.DATA,
        MediaStore.Audio.ArtistColumns.ARTIST,
        MediaStore.Audio.AlbumColumns.ALBUM,
        MediaStore.Audio.AudioColumns.DURATION
    )
}


