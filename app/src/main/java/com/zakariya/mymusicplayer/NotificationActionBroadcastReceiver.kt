package com.zakariya.mymusicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.zakariya.mymusicplayer.services.ACTION_NEXT
import com.zakariya.mymusicplayer.services.ACTION_PLAY_PAUSE
import com.zakariya.mymusicplayer.services.ACTION_PREVIOUS

class NotificationActionBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != null) {
            when (intent.action) {
                ACTION_PREVIOUS -> {
                    Toast.makeText(context, "Previous", Toast.LENGTH_SHORT).show()
                    Log.i("Broadcast", "previous")
                }

                ACTION_PLAY_PAUSE -> {
                    Toast.makeText(context, "Play Pause", Toast.LENGTH_SHORT).show()
                    Log.i("Broadcast", "play pause")
                }

                ACTION_NEXT -> {
                    Toast.makeText(context, "Next", Toast.LENGTH_SHORT).show()
                    Log.i("Broadcast", "next")
                }

                else -> Unit
            }
        }
    }
}