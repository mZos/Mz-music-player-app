@file:Suppress("DEPRECATION")

package com.zakariya.mymusicplayer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.media.app.NotificationCompat
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.util.Constants.NOTIFICATION_CHANNEL_ID
import com.zakariya.mymusicplayer.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.zakariya.mymusicplayer.util.Constants.NOTIFICATION_ID
import java.io.IOException

@Suppress("DEPRECATION")
class PlayerService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnSeekCompleteListener, AudioManager.OnAudioFocusChangeListener {

    private val iBinder: IBinder = LocalBinder()
    var mediaPlayer: MediaPlayer? = null
    private var mediaFile: String? = ""
    private var resumePosition: Int = 0
    private lateinit var audioManager: AudioManager
    private val TAG = "PlayerService"
    lateinit var focusRequest: AudioFocusRequest
    private var focus: Int? = null
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var mediaSessionCompat: MediaSessionCompat

    override fun onBind(p0: Intent?): IBinder? {
        return iBinder
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaSessionCompat = MediaSessionCompat(this, "Music")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Getting song path from SongFragment
        mediaFile = intent?.getStringExtra("songPath")
        //Request audio focus
        //  if (!requestAudioFocus()) stopSelf()
        if (mediaFile != null && mediaFile != "") initMediaPlayer()



        startForegroundService()
        return START_STICKY
    }

    override fun onCompletion(p0: MediaPlayer?) {
        Log.i(TAG, "Song Completed")
    }

    override fun onPrepared(p0: MediaPlayer?) {
    }

    override fun onSeekComplete(p0: MediaPlayer?) {

    }

    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mediaPlayer == null) initMediaPlayer()
                else if (!mediaPlayer!!.isPlaying) mediaPlayer?.start()
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

            AudioManager.AUDIOFOCUS_REQUEST_DELAYED ->
                if (mediaPlayer!!.isPlaying) mediaPlayer?.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        if (requestAudioFocus())
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
        mediaPlayer?.setOnCompletionListener(this)
        Toast.makeText(this, "Initialized", Toast.LENGTH_SHORT).show()
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

    private fun requestAudioFocus(): Boolean {
        val audioAttributes = AudioAttributes.Builder().run {
            setUsage(AudioAttributes.USAGE_MEDIA)
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            build()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(audioAttributes)
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(this@PlayerService)
                build()
            }
            focus = audioManager.requestAudioFocus(focusRequest)
        } else {
            focus = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        if (focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return true
        return false
    }

    private fun removeAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    audioManager.abandonAudioFocusRequest(focusRequest)
        else
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this)
    }

    private fun startForegroundService() {

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }


        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.img)

        val builder = androidx.core.app.NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID).apply {
                setContentTitle("Test Title")
                setContentText("Test Subtitle")
                setSubText("Test Subtext")
                addAction(R.drawable.ic_previous, "Previous", null)
                addAction(R.drawable.ic_play, "Play", null)
                addAction(R.drawable.ic_next, "Next", null)
                setLargeIcon(bitmap)
                setSmallIcon(R.drawable.ic_play)

                // Take advantage of MediaStyle features
                setStyle(
                    NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)
                )
                setAutoCancel(false)
            }

        startForeground(NOTIFICATION_ID, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}