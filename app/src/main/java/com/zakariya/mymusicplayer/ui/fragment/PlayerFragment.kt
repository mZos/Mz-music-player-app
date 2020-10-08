package com.zakariya.mymusicplayer.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.repository.SongRepository
import com.zakariya.mymusicplayer.services.PlayerService
import com.zakariya.mymusicplayer.ui.SongViewModel
import com.zakariya.mymusicplayer.ui.SongViewModelFactory
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerFragment : Fragment(R.layout.fragment_player), View.OnClickListener {

    val TAG = this::class.java.simpleName
    private var playerService: PlayerService? = null

    private lateinit var viewModel: SongViewModel

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as PlayerService.LocalBinder
            playerService = binder.getService()
            iLog("Connected")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            playerService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = SongRepository(requireContext())
        val viewModelFactory = SongViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SongViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtSongTitle.isSelected = true

        fabPlayPause.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnPrevious.setOnClickListener(this)
        btnPlayList.setOnClickListener(this)
        btnShuffle.setOnClickListener(this)

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

//        if (songPosition != -1) {
//            val songTitle = listOfSongs[songPosition].name
//            txtSongTitle.text = songTitle
//            txtArtistName.text = listOfSongs[songPosition].artistName
//
//            this.lifecycleScope.launch(Dispatchers.IO) {
//                val imgByte = getSongThumbnail(listOfSongs[songPosition].path)
//                withContext(Dispatchers.Main) {
//                    Glide.with(requireContext()).asBitmap().load(imgByte).error(R.drawable.ic_album)
//                        .into(imgThumbnail)
//                }
//            }
//        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fabPlayPause -> {

            }
            R.id.btnNext -> {

            }
            R.id.btnPrevious -> {

            }
            R.id.btnShuffle -> {

            }
            R.id.btnPlayList -> {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (playerService == null) {
            iLog("Bounded")
            val playerServiceIntent = Intent(requireActivity(), PlayerService::class.java)
            requireActivity().bindService(
                playerServiceIntent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onPause() {
        super.onPause()
        iLog("onPause")
        if (playerService != null) {
            iLog("unbounded")
            requireActivity().unbindService(serviceConnection)
            playerService = null
        }
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

    private fun setUpSeekBar() = this.lifecycleScope.launch(Dispatchers.Main) {
        while (playerService?.mediaPlayer!!.isPlaying) {
            txtStartDuration.text = millisToString(playerService?.mediaPlayer?.currentPosition!!)
            seekBar.progress = playerService?.mediaPlayer?.currentPosition!!
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
        if (playerService?.mediaPlayer!!.isPlaying) {
            fabPlayPause.setImageResource(R.drawable.ic_pause)
        } else {
            fabPlayPause.setImageResource(R.drawable.ic_play)
        }
    }

    private fun playAudio() {
        //Check is service is active
        if (playerService == null) {
            //requireActivity().startService(playerIntent)
        } else {
            //Service is active
            //Send media with BroadcastReceiver
        }
    }
}