package com.zakariya.mzmusicplayer.util

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zakariya.mzmusicplayer.model.Song
import com.zakariya.mzmusicplayer.util.Constants.SAVE_CURRENT_SONG_KEY


object SharedPreferenceUtil {

    fun saveCurrentSong(currentSong: Song, sharedPreferences: SharedPreferences) {
        Gson().apply {
            val songJson = toJson(currentSong)
            with(sharedPreferences.edit()) {
                putString(SAVE_CURRENT_SONG_KEY, songJson)
                apply()
            }
        }
    }

    fun getCurrentSong(sharedPreferences: SharedPreferences): Song? {
        val songJson = sharedPreferences.getString(SAVE_CURRENT_SONG_KEY, null)
        val type = object : TypeToken<Song?>() {}.type
        Gson().apply {
            return fromJson(songJson, type)
        }
    }

    fun saveCurrentPosition(sharedPreferences: SharedPreferences, currentPosition: Int) {
        with(sharedPreferences.edit()) {
            putInt(Constants.CURRENT_SONG_DURATION_KEY, currentPosition)
            apply()
        }
    }

    fun getPosition(sharedPreferences: SharedPreferences): Int {
        return sharedPreferences.getInt(Constants.POSITION_KEY, 0)
    }
}