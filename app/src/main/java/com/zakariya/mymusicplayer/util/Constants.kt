package com.zakariya.mymusicplayer.util

import android.provider.MediaStore

const val PREF_NAME = "com.zakariya.mymusicplayer.SHARED_PREF"
const val POSITION_KEY = "position"
const val REQ_CODE = 0
const val RES_POS_KEY = "resumePosition"


@Suppress("DEPRECATION")
val baseProjection = arrayOf(
    MediaStore.Audio.AudioColumns.TITLE,
    MediaStore.Audio.AudioColumns._ID,
    MediaStore.Audio.AudioColumns.DATA,
    MediaStore.Audio.ArtistColumns.ARTIST
)