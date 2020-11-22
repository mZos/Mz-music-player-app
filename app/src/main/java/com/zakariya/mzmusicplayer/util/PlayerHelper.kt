package com.zakariya.mzmusicplayer.util

import android.content.SharedPreferences
import android.media.MediaMetadataRetriever
import com.zakariya.mzmusicplayer.model.Song


object PlayerHelper {
    fun getCurrentSong(sharedPreferences: SharedPreferences): Song? {
        return if (MusicPlayerRemote.playerService?.mediaPlayer != null && MusicPlayerRemote.playerService?.currentSong != null) {
            MusicPlayerRemote.playerService?.currentSong
        } else {
            SharedPreferenceUtil.getCurrentSong(sharedPreferences)
        }
    }

    fun getSongThumbnail(songPath: String): ByteArray? {
        var imgByte: ByteArray?
        MediaMetadataRetriever().also {
            try {
                it.setDataSource(songPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            imgByte = it.embeddedPicture
            it.release()
        }
        return imgByte
    }
}

