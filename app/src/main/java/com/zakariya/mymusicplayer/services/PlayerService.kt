package com.zakariya.mymusicplayer.services

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.IOException

class PlayerService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener,
    AudioManager.OnAudioFocusChangeListener {

    private val iBinder: IBinder = LocalBinder()
    var mediaPlayer: MediaPlayer? = null
    private var mediaFile: String? = ""
    private var resumePosition: Int = 0
    private lateinit var audioManager: AudioManager
    private val TAG = "PlayerService"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onBind(p0: Intent?): IBinder? {
        return iBinder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //Getting song path from SongFragment
        mediaFile = intent?.getStringExtra("songPath")

        //Request audio focus
        if (!requestAudioFocus()) {
            stopSelf()
        }

        if (mediaFile != null && mediaFile != "") {
            initMediaPlayer()
            Log.i(TAG, "Initialized")
        }


        if (intent == null) {
            if (mediaPlayer!!.isPlaying)
                pauseMusic()
            else
                resumeMusic()
        }

        return START_STICKY
    }

    override fun onCompletion(p0: MediaPlayer?) {
        Log.i(TAG, "Song Completed")
    }

    override fun onPrepared(p0: MediaPlayer?) {
        playMusic()
    }

    override fun onSeekComplete(p0: MediaPlayer?) {

    }

    override fun onInfo(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        return false
    }

    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mediaPlayer == null) initMediaPlayer() else if (!mediaPlayer!!.isPlaying) mediaPlayer?.start()
                mediaPlayer?.setVolume(1.0f, 1.0f)
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                if (mediaPlayer!!.isPlaying) mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                if (mediaPlayer!!.isPlaying) mediaPlayer?.pause()

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                if (mediaPlayer!!.isPlaying) mediaPlayer?.setVolume(0.1f, 0.1f)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        removeAudioFocus()
    }

    inner class LocalBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    private fun initMediaPlayer() {
        mediaPlayer?.reset()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnPreparedListener(this)
        mediaPlayer?.setOnSeekCompleteListener(this)
        mediaPlayer?.setOnInfoListener(this)
        mediaPlayer?.setOnCompletionListener(this)

        try {
            mediaPlayer?.setDataSource(mediaFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mediaPlayer?.prepare()
    }

    fun playMusic() {
        if (!mediaPlayer!!.isPlaying)
            mediaPlayer?.start()
    }

    fun pauseMusic() {
        if (mediaPlayer!!.isPlaying)
            mediaPlayer?.pause()
        resumePosition = mediaPlayer!!.currentPosition
    }

    fun resumeMusic() {
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer?.seekTo(resumePosition)
            mediaPlayer?.start()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocus(): Boolean {
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build()
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocusRequest(
                    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build()
                )
    }
}