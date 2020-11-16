package com.zakariya.mymusicplayer.util

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zakariya.mymusicplayer.model.Song
import com.zakariya.mymusicplayer.util.Constants.SAVE_CURRENT_SONG_KEY


object SharedPreferenceUtil {

    fun saveCurrentSong(currentSong: Song, sharedPreferences: SharedPreferences) {
        Gson().apply {
            val songJson = toJson(currentSong)
            Log.i("MyPlayerService", songJson)
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

    fun getPosition(sharedPreferences: SharedPreferences): Int {
        return sharedPreferences.getInt(Constants.POSITION_KEY, -1)
    }
}