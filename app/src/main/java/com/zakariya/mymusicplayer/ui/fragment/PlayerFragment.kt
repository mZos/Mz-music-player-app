package com.zakariya.mymusicplayer.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.zakariya.mymusicplayer.PlayerHelper
import com.zakariya.mymusicplayer.PlayerHelper.getSongThumbnail
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.model.Song
import com.zakariya.mymusicplayer.repository.SongRepository
import com.zakariya.mymusicplayer.services.PlayerService
import com.zakariya.mymusicplayer.ui.SongViewModel
import com.zakariya.mymusicplayer.ui.SongViewModelFactory
import com.zakariya.mymusicplayer.util.Constants.PREF_NAME
import com.zakariya.mymusicplayer.util.MusicPlayerRemote
import com.zakariya.mymusicplayer.util.PlayPauseStateNotifier
import com.zakariya.mymusicplayer.util.SongChangeNotifier
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerFragment : Fragment(R.layout.fragment_player), View.OnClickListener, SongChangeNotifier,
    PlayPauseStateNotifier {

    private val TAG = "My" + this::class.java.simpleName

    private val playerService: PlayerService?
        get() = MusicPlayerRemote.playerService
    private val currentSong: Song?
        get() = PlayerHelper.getCurrentSong(sharedPreferences)

    private lateinit var viewModel: SongViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (MusicPlayerRemote.playerService != null) {
            MusicPlayerRemote.playerService?.setSongChangeCallback(this)
            MusicPlayerRemote.playerService?.setPlayPauseStateCallback(this)
        }

        val repository = SongRepository(requireContext())
        val viewModelFactory = SongViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SongViewModel::class.java)

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtSongTitle.isSelected = true
        txtArtistName.isSelected = true

        updateUi()

        fabPlayPause.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnPrevious.setOnClickListener(this)
        btnPlayList.setOnClickListener(this)
        btnShuffle.setOnClickListener(this)

        setUpPlayPauseButton()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    playerService?.mediaPlayer?.seekTo(progress)
                    txtStartDuration.text = millisToString(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fabPlayPause -> {
                MusicPlayerRemote.playPause()
                setUpSeekBar()
                setUpPlayPauseButton()
            }
            R.id.btnNext -> {
                MusicPlayerRemote.playNextSong()
            }
            R.id.btnPrevious -> {
                MusicPlayerRemote.playPreviousSong()
            }
            R.id.btnShuffle -> {
                Toast.makeText(requireContext(), "Coming Soon", Toast.LENGTH_SHORT).show()
            }
            R.id.btnPlayList -> {
                Toast.makeText(requireContext(), "Coming Soon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCurrentSongChange() {
        if (activity != null) {
            updateUi()
            setUpPlayPauseButton()
        }
        MusicPlayerRemote.playerService?.restartNotification()
    }

    override fun onPlayPauseStateChange() {
        if (activity != null) {
            setUpPlayPauseButton()
        }
        MusicPlayerRemote.playerService?.restartNotification()
    }

    private fun iLog(message: String) = Log.i(TAG, message)

    @SuppressLint("SetTextI18n")
    private fun updateUi() {
        if (currentSong != null) {
            val imgByte = getSongThumbnail(currentSong!!.path)
            Glide.with(requireContext()).asBitmap().load(imgByte).error(R.drawable.ic_album)
                .into(imgThumbnail)
        }

        txtEndDuration.text = millisToString(MusicPlayerRemote.songDurationMillis)
        setUpSeekBar()

        if (currentSong != null) {
            txtSongTitle.text = currentSong!!.name
            setUpSeekBar()
            txtArtistName.text = currentSong!!.artistName
        }
        txtStartDuration.text = "00:00"
    }

    private fun setUpSeekBar() = lifecycleScope.launch(Dispatchers.Main) {
        seekBar.max = MusicPlayerRemote.songDurationMillis
        if (playerService?.mediaPlayer != null) {
            try {
                while (playerService?.mediaPlayer!!.isPlaying) {
                    txtStartDuration.text =
                        millisToString(playerService?.mediaPlayer?.currentPosition!!)
                    seekBar.progress = playerService?.mediaPlayer?.currentPosition!!
                    delay(100)
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
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

    private fun setUpPlayPauseButton() {
        if (MusicPlayerRemote.playerService != null && MusicPlayerRemote.playerService?.mediaPlayer != null) {
            if (MusicPlayerRemote.playerService?.mediaPlayer!!.isPlaying) {
                fabPlayPause.setImageResource(R.drawable.ic_pause)
            } else {
                fabPlayPause.setImageResource(R.drawable.ic_play)
            }
        }
    }
}