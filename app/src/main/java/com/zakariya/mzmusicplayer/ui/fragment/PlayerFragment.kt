package com.zakariya.mzmusicplayer.ui.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.zakariya.mzmusicplayer.R
import com.zakariya.mzmusicplayer.model.Song
import com.zakariya.mzmusicplayer.repository.SongRepository
import com.zakariya.mzmusicplayer.services.PlayerService
import com.zakariya.mzmusicplayer.ui.SongViewModel
import com.zakariya.mzmusicplayer.ui.SongViewModelFactory
import com.zakariya.mzmusicplayer.util.*
import com.zakariya.mzmusicplayer.util.Constants.PREF_NAME
import com.zakariya.mzmusicplayer.util.PlayerHelper.getSongThumbnail
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerFragment : Fragment(R.layout.fragment_player), View.OnClickListener, SongChangeNotifier,
    PlayPauseStateNotifier, SeekCompletionNotifier {

    private lateinit var viewModel: SongViewModel
    private lateinit var sharedPreferences: SharedPreferences

    private val playerService: PlayerService?
        get() = MusicPlayerRemote.playerService
    private val currentSong: Song?
        get() = PlayerHelper.getCurrentSong(sharedPreferences)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (playerService != null) {
            playerService!!.setSongChangeCallback(this)
            playerService!!.setPlayPauseStateCallback(this)
            playerService!!.setSeekCompleteNotifierCallback(this)
        }

        val repository = SongRepository(requireContext())
        val viewModelFactory = SongViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SongViewModel::class.java)

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (SharedPreferenceUtil.getCurrentSong(sharedPreferences) != null) {
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
                        MusicPlayerRemote.seekTo(progress)
                        txtStartDuration.text = millisToString(progress)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) = Unit
                override fun onStopTrackingTouch(p0: SeekBar?) = Unit
            })
        } else {
            rlPlayer.visibility = View.GONE
            txtEmptySong.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        if (activity != null) {
            updateUi()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fabPlayPause -> {
                MusicPlayerRemote.playPause()
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
            setUpSeekBar()
            setUpPlayPauseButton()
        }
        if (playerService != null)
            playerService!!.restartNotification()
    }

    override fun onPlayPauseStateChange() {
        if (activity != null) {
            setUpSeekBar()
            setUpPlayPauseButton()
        }
        if (playerService != null) {
            playerService!!.setMediaSessionAction()
            playerService!!.restartNotification()
        }
    }

    override fun onSeekComplete() {
        if (activity != null)
            setUpSeekBar()
    }

    private fun updateUi() {
        if (currentSong != null) {
            val imgByte = getSongThumbnail(currentSong!!.path)
            Glide.with(requireContext()).asBitmap().load(imgByte).error(R.drawable.ic_album)
                .into(imgThumbnail)
        }

        setUpSeekBar()

        if (currentSong != null) {
            txtSongTitle.text = currentSong!!.name
            setUpSeekBar()
            txtArtistName.text = currentSong!!.artistName
        }
    }

    private fun setUpSeekBar() = lifecycleScope.launch(Dispatchers.Main) {
        txtEndDuration.text = millisToString(MusicPlayerRemote.songDurationMillis)
        txtStartDuration.text = millisToString(MusicPlayerRemote.currentSongPositionMillis)
        seekBar.max = MusicPlayerRemote.songDurationMillis
        if (playerService?.mediaPlayer != null) {
            try {
                seekBar.progress = MusicPlayerRemote.currentSongPositionMillis
                while (playerService?.mediaPlayer!!.isPlaying) {
                    txtStartDuration.text =
                        millisToString(MusicPlayerRemote.currentSongPositionMillis)
                    seekBar.progress = MusicPlayerRemote.currentSongPositionMillis
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
        if (playerService != null &&
            playerService!!.mediaPlayer != null &&
            playerService!!.isPlaying()
        ) {
            fabPlayPause.setImageResource(R.drawable.ic_pause)
        } else {
            fabPlayPause.setImageResource(R.drawable.ic_play)
        }
    }
}