package com.zakariya.mymusicplayer.services

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log

class MediaSessionCallback(private val context: Context, private val service: PlayerService) :
    MediaSessionCompat.Callback() {
    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
        iLog("seek")
        service.seekTo(pos.toInt())
    }

    private fun iLog(msg: String) {
        Log.i(this::class.java.simpleName, msg)
    }
}