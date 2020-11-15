package com.zakariya.mymusicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zakariya.mymusicplayer.services.ACTION_NEXT
import com.zakariya.mymusicplayer.services.ACTION_PLAY_PAUSE
import com.zakariya.mymusicplayer.services.ACTION_PREVIOUS
import com.zakariya.mymusicplayer.util.MusicPlayerRemote

class NotificationActionBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != null) {
            when (intent.action) {
                ACTION_PREVIOUS -> {
                    MusicPlayerRemote.playPreviousSong()
                }

                ACTION_PLAY_PAUSE -> {
                    MusicPlayerRemote.playPause()
                }

                ACTION_NEXT -> {
                    MusicPlayerRemote.playNextSong()
                }

                else -> Unit
            }
        }
    }
}