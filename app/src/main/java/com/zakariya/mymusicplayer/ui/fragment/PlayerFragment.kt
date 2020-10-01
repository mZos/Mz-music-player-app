package com.zakariya.mymusicplayer.ui.fragment

import android.content.*
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.zakariya.mymusicplayer.PlayerHelper.PlayBack.getSongFromSharedStorage
import com.zakariya.mymusicplayer.PlayerHelper.PlayBack.initMediaPlayer
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.model.Songs
import com.zakariya.mymusicplayer.services.PlayerService
import com.zakariya.mymusicplayer.ui.fragment.SongFragment.Companion.currentPlyingSongPosition
import com.zakariya.mymusicplayer.util.POSITION_KEY
import com.zakariya.mymusicplayer.util.PREF_NAME
import com.zakariya.mymusicplayer.util.RES_POS_KEY
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PlayerFragment : Fragment(R.layout.fragment_player), View.OnClickListener {
    val TAG = "PlayerFragment"
    private lateinit var playerService: PlayerService
    private var isServiceBounded: Boolean = false

    val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.LocalBinder
            playerService = binder.getService()
            isServiceBounded = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isServiceBounded = false
        }
    }

    companion object {
        var mediaPlayer: MediaPlayer? = MediaPlayer()
        var listOfSongs: ArrayList<Songs> = arrayListOf()
    }

    var songPosition = -1
    private var resumePosition: Int = 0
    private var savedResumePosition = 0
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        listOfSongs = getSongFromSharedStorage(requireContext())

        songPosition = sharedPreferences.getInt(POSITION_KEY, -1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtSongTitle.isSelected = true

        if (currentPlyingSongPosition != -1) {
            initMediaPlayer(mediaPlayer, listOfSongs[songPosition].path)
            mediaPlayer?.start()
            seekBar.max = mediaPlayer!!.duration
            setUpSeekBar()
            iLog("1")
            saveCurrentPosition()
        } else if (songPosition != -1) {
            initMediaPlayer(mediaPlayer, listOfSongs[songPosition].path)
            savedResumePosition = sharedPreferences.getInt(RES_POS_KEY, 0)
            seekBar.max = mediaPlayer!!.duration
            mediaPlayer?.seekTo(savedResumePosition)
            iLog("2")
            seekBar.progress = savedResumePosition
            txtStartDuration.text = millisToString(savedResumePosition)
        }

        setPlayPauseImageResource()

        fabPlayPause.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnPrevious.setOnClickListener(this)
        btnPlayList.setOnClickListener(this)
        btnShuffle.setOnClickListener(this)

        txtEndDuration.text = millisToString(mediaPlayer?.duration!!)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    txtStartDuration.text = millisToString(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        if (songPosition != -1) {
            val songTitle = listOfSongs[songPosition].name
            txtSongTitle.text = songTitle
            txtArtistName.text = listOfSongs[songPosition].artistName

            this.lifecycleScope.launch(Dispatchers.IO) {
                val imgByte = getSongThumbnail(listOfSongs[songPosition].path)
                withContext(Dispatchers.Main) {
                    Glide.with(requireContext()).asBitmap().load(imgByte).error(R.drawable.ic_album)
                        .into(imgThumbnail)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().bindService(
            Intent(requireActivity(), PlayerService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        isServiceBounded = true
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unbindService(serviceConnection)
        isServiceBounded = false
    }

    private fun getSongThumbnail(songPath: String): ByteArray? {
        var imgByte: ByteArray?
        MediaMetadataRetriever().also {
            it.setDataSource(songPath)
            imgByte = it.embeddedPicture
            it.release()
        }
        return imgByte
    }

    private fun iLog(m: String) = Log.i(TAG, m)

    private fun saveCurrentPosition() = requireActivity().lifecycleScope.launch(Dispatchers.IO) {
        while (mediaPlayer!!.isPlaying) {
            resumePosition = mediaPlayer?.currentPosition!!
            with(sharedPreferences.edit()) {
                resumePosition?.let { putInt(RES_POS_KEY, it) }
                apply()
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fabPlayPause -> {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer?.pause()
                    fabPlayPause.setImageResource(R.drawable.ic_play)
                } else {
                    mediaPlayer?.start()
                    setUpSeekBar()
                    saveCurrentPosition()
                    fabPlayPause.setImageResource(R.drawable.ic_pause)
                }
            }
            R.id.btnNext -> {
                if (isServiceBounded)
                    Toast.makeText(
                        requireContext(),
                        "service is bounded and started",
                        Toast.LENGTH_SHORT
                    ).show()
                requireActivity().startService(
                    Intent(
                        requireActivity(),
                        PlayerService::class.java
                    ).putExtra("songPath", listOfSongs[songPosition + 1].path)
                )
            }
            R.id.btnPrevious -> {
                Toast.makeText(requireContext(), "previous CLicked", Toast.LENGTH_SHORT).show()
                playerService.pauseMusic()
            }
            R.id.btnShuffle -> {
                Toast.makeText(requireContext(), "shuffle CLicked", Toast.LENGTH_SHORT).show()
                playerService.resumeMusic()
            }
            R.id.btnPlayList -> {
                Toast.makeText(requireContext(), "playlist CLicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpSeekBar() = this.lifecycleScope.launch(Dispatchers.Main) {
        while (mediaPlayer!!.isPlaying) {
            txtStartDuration.text = millisToString(mediaPlayer?.currentPosition!!)
            seekBar.progress = mediaPlayer?.currentPosition!!
            delay(100)
        }
    }

    private fun millisToString(duration: Int): String {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60

        var timeString = "$minutes:"
        if (minutes < 10) {
            timeString = "0$minutes:"
        }
        if (seconds < 10) timeString += "0"
        timeString += seconds

        return timeString
    }

    private fun setPlayPauseImageResource() {
        if (mediaPlayer!!.isPlaying) {
            fabPlayPause.setImageResource(R.drawable.ic_pause)
        } else {
            fabPlayPause.setImageResource(R.drawable.ic_play)
        }
    }
}