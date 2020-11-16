@file:Suppress("DEPRECATION")

package com.zakariya.mymusicplayer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
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
import android.text.Html
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media.app.NotificationCompat
import com.zakariya.mymusicplayer.NotificationActionBroadcastReceiver
import com.zakariya.mymusicplayer.PlayerHelper.getSongThumbnail
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.model.Song
import com.zakariya.mymusicplayer.ui.activity.MainActivity
import com.zakariya.mymusicplayer.util.Constants.CURRENT_SONG_DURATION_KEY
import com.zakariya.mymusicplayer.util.Constants.NOTIFICATION_CHANNEL_ID
import com.zakariya.mymusicplayer.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.zakariya.mymusicplayer.util.Constants.NOTIFICATION_ID
import com.zakariya.mymusicplayer.util.Constants.POSITION_KEY
import com.zakariya.mymusicplayer.util.Constants.PREF_NAME
import com.zakariya.mymusicplayer.util.PlayPauseStateNotifier
import com.zakariya.mymusicplayer.util.SharedPreferenceUtil
import com.zakariya.mymusicplayer.util.SongChangeNotifier
import java.io.IOException

@Suppress("DEPRECATION")

const val ACTION_PREVIOUS = "action previous"
const val ACTION_PLAY_PAUSE = "action play pause"
const val ACTION_NEXT = "action next"
const val ACTION_MAIN = "action main"

class PlayerService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener,
    AudioManager.OnAudioFocusChangeListener {

    private val TAG = "My" + this::class.java.simpleName

    private lateinit var currentSongChangeNotifier: SongChangeNotifier
    private lateinit var playPauseStateNotifier: PlayPauseStateNotifier
    private lateinit var audioManager: AudioManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mediaSessionCompat: MediaSessionCompat

    lateinit var focusRequest: AudioFocusRequest

    private var focus: Int? = null
    private var position = -1
    private var originalSongList: List<Song> = ArrayList()
    private val iBinder: IBinder = LocalBinder()

    var mediaPlayer: MediaPlayer? = null
    var currentSong: Song? = null
    var listOfAllSong: List<Song> = ArrayList()
    var isPlaying = false

    val songFromSharedPreferences: Song?
        get() = SharedPreferenceUtil.getCurrentSong(sharedPreferences)

    val savedPosition: Int
        get() = SharedPreferenceUtil.getPosition(sharedPreferences)

    private fun iLog(m: String) = Log.i(TAG, m)

    override fun onBind(p0: Intent?): IBinder? {
        return iBinder
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaSessionCompat = MediaSessionCompat(this, "Music")

        if (currentSong == null) {
            currentSong = SharedPreferenceUtil.getCurrentSong(sharedPreferences)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCompletion(mp: MediaPlayer?) {
        playNext()
    }

    override fun onPrepared(mp: MediaPlayer?) {

    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        mediaPlayer?.stop()
        with(sharedPreferences.edit()) {
            putInt(POSITION_KEY, position)
            apply()
        }
        stopForeground(false)
        return false
    }

    override fun onSeekComplete(mp: MediaPlayer?) {

    }

    override fun onAudioFocusChange(focusState: Int) {
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                iLog("Gained")
                if (mediaPlayer == null) initMediaPlayer(songFromSharedPreferences!!.path)
                else if (!mediaPlayer!!.isPlaying) {
                    mediaPlayer?.start()
                    startForegroundService()
                    notifyPlayPauseStateChanged()
                }
                mediaPlayer?.setVolume(1.0f, 1.0f)
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                iLog("Loss....")
                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    mediaPlayer?.stop()
                    notifyPlayPauseStateChanged()
                    stopForeground(false)
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {

                iLog("loss transient")
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.pause()
                    stopForeground(false)
                    notifyPlayPauseStateChanged()
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                iLog("CAn duck")
                if (mediaPlayer!!.isPlaying) mediaPlayer?.setVolume(0.1f, 0.1f)
            }

            AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                iLog("lost delayed")
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.pause()
                    stopForeground(false)
                    notifyPlayPauseStateChanged()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        iLog("onDestroy")
        stopForeground(true)
        if (mediaPlayer != null) {
            with(sharedPreferences.edit()) {
                putInt(POSITION_KEY, position)
                putInt(CURRENT_SONG_DURATION_KEY, mediaPlayer!!.currentPosition)
                apply()
            }
            mediaPlayer?.stop()
            notifyPlayPauseStateChanged()
            mediaPlayer?.release()
        }
        if (requestAudioFocus())
            removeAudioFocus()
    }

    fun playPause() {
        this.playPauseMusic()
    }

    fun playNext() {
        this.playNextSong()
    }

    fun playPrevious() {
        this.playPreviousSong()
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

    fun getAllSongs(songList: List<Song>?, clickedPosition: Int) {
        if (!songList.isNullOrEmpty() && clickedPosition < songList.size && clickedPosition >= 0) {
            this.position = clickedPosition
            originalSongList = ArrayList(songList)
            this.listOfAllSong = ArrayList(originalSongList)
            initMediaPlayer(position)
            startForegroundService()
        } else if (!songList.isNullOrEmpty()) {
            originalSongList = ArrayList(songList)
            this.listOfAllSong = ArrayList(originalSongList)
        } else {
            this.listOfAllSong = emptyList()
        }

    }

    //initialize MediaPlayer and play
    private fun initMediaPlayer(position: Int) {
        this.currentSong = listOfAllSong[position]
        SharedPreferenceUtil.saveCurrentSong(currentSong!!, sharedPreferences)

        mediaPlayer?.reset()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnPreparedListener(this)
        mediaPlayer?.setOnSeekCompleteListener(this)
        mediaPlayer?.setOnCompletionListener(this)
        mediaPlayer?.setOnErrorListener(this)
        try {
            mediaPlayer?.setDataSource(currentSong?.path)
            mediaPlayer?.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        play()
        notifyPlayPauseStateChanged()
        isPlaying = true
    }

    //Only initialize
    fun initMediaPlayer(songPath: String) {
        mediaPlayer?.reset()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnPreparedListener(this)
        mediaPlayer?.setOnSeekCompleteListener(this)
        mediaPlayer?.setOnCompletionListener(this)
        try {
            mediaPlayer?.setDataSource(songPath)
            mediaPlayer?.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun getSongDurationMillis(): Int {
        if (mediaPlayer != null) {
            return mediaPlayer!!.duration
        }
        return -1
    }

    fun play() {
        if (!requestAudioFocus()) stopSelf()
        mediaPlayer?.start()
    }

    fun playPauseMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer?.pause()
                notifyPlayPauseStateChanged()

                with(sharedPreferences.edit()) {
                    putInt(POSITION_KEY, position)
                    putInt(CURRENT_SONG_DURATION_KEY, mediaPlayer!!.currentPosition)
                    apply()
                }

                if (requestAudioFocus())
                    removeAudioFocus()
                stopForeground(false)
                isPlaying = true
            } else {
                play()
                notifyPlayPauseStateChanged()
                startForegroundService()
                isPlaying = true
            }
        } else {
            initMediaPlayer(position)
            startForegroundService()
            isPlaying = true
        }
    }

    fun playNextSong() {
        initMediaPlayer(getNextPosition())
        notifyCurrentSongChanged()
    }

    fun playPreviousSong() {
        initMediaPlayer(getPreviousPosition())
        notifyCurrentSongChanged()
    }

    fun notifyCurrentSongChanged() {
        currentSongChangeNotifier.onCurrentSongChange()
    }

    fun notifyPlayPauseStateChanged() {
        playPauseStateNotifier.onPlayPauseStateChange()
    }

    fun getNextPosition(): Int {
        var nextPosition = position + 1
        if (position == -1) {
            nextPosition = sharedPreferences.getInt(POSITION_KEY, 0) + 1
        }

        if (listOfAllSong.isNotEmpty()) {
            if (nextPosition > listOfAllSong.lastIndex)
                nextPosition = 0
        }
        position = nextPosition
        return nextPosition
    }

    fun getPreviousPosition(): Int {
        var prePosition = position - 1
        if (position == -1) {
            prePosition = sharedPreferences.getInt(POSITION_KEY, 0) - 1
        }

        if (prePosition == -1)
            prePosition = listOfAllSong.lastIndex

        if (listOfAllSong.isNotEmpty()) {
            if (position == 0)
                prePosition = listOfAllSong.lastIndex
        }
        position = prePosition
        return prePosition
    }

    fun setSongChangeCallback(callback: SongChangeNotifier) {
        this.currentSongChangeNotifier = callback
    }

    fun setPlayPauseStateCallback(callback: PlayPauseStateNotifier) {
        this.playPauseStateNotifier = callback
    }

    fun restartNotification() {
        startForegroundService()
    }

    @JvmName("isPlaying1")
    fun isPlaying(): Boolean {
        if (mediaPlayer != null) {
            return mediaPlayer!!.isPlaying
        }
        return false
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val mainIntent = Intent(this, MainActivity::class.java).also {
            it.action = ACTION_MAIN
        }
        val mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, 0)

        val previousIntent = Intent(this, NotificationActionBroadcastReceiver::class.java).also {
            it.action = ACTION_PREVIOUS
        }
        val previousPendingIntent =
            PendingIntent.getBroadcast(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val playPauseIntent = Intent(this, NotificationActionBroadcastReceiver::class.java).also {
            it.action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent =
            PendingIntent.getBroadcast(this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(this, NotificationActionBroadcastReceiver::class.java).also {
            it.action = ACTION_NEXT
        }
        val nextPendingIntent =
            PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)


        val imgByte = getSongThumbnail(currentSong!!.path)
        val bitmap = if (imgByte != null)
            BitmapFactory.decodeByteArray(imgByte, 0, imgByte.size)
        else
            BitmapFactory.decodeResource(this.resources, R.drawable.ic_album)

        var playPauseDrawable = R.drawable.ic_pause_bigger
        if (mediaPlayer != null)
            playPauseDrawable = if (mediaPlayer!!.isPlaying) {
                R.drawable.ic_pause_bigger
            } else {
                R.drawable.ic_play_bigger
            }

        val subText = if (currentSong?.albumName != null) currentSong?.albumName else ""

        val builder = androidx.core.app.NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID).setOngoing(true).apply {
                setContentIntent(mainPendingIntent)
                priority = androidx.core.app.NotificationCompat.PRIORITY_MAX
                setCategory(androidx.core.app.NotificationCompat.CATEGORY_SERVICE)
                setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
                setContentTitle(Html.fromHtml("<b>" + currentSong!!.name + "</b>"))
                setContentText(currentSong!!.artistName)
                setSubText(Html.fromHtml("<b>$subText</b>"))
                setOngoing(isPlaying())
                addAction(R.drawable.ic_previous, "Previous", previousPendingIntent)
                addAction(playPauseDrawable, "Play", playPausePendingIntent)
                addAction(R.drawable.ic_next, "Next", nextPendingIntent)
                setLargeIcon(bitmap)
                setSmallIcon(R.drawable.ic_song)
                setShowWhen(false)
                // Take advantage of MediaStyle features
                setStyle(
                    NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2)
                )
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
        channel.description = "The playing notification provides actions for play/pause etc."
        channel.enableLights(false)
        channel.enableVibration(false)
        channel.setShowBadge(false)
        notificationManager.createNotificationChannel(channel)
    }

    inner class LocalBinder : Binder() {
        fun getService(): PlayerService {
            return this@PlayerService
        }
    }
}