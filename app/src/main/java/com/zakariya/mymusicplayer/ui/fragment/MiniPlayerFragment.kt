package com.zakariya.mymusicplayer.ui.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.ui.fragment.PlayerFragment.Companion.listOfSongs
import com.zakariya.mymusicplayer.ui.fragment.PlayerFragment.Companion.mediaPlayer
import com.zakariya.mymusicplayer.util.POSITION_KEY
import com.zakariya.mymusicplayer.util.PREF_NAME
import kotlinx.android.synthetic.main.fragment_mini_player.*
import kotlinx.android.synthetic.main.fragment_mini_player.view.*

open class MiniPlayerFragment : Fragment(R.layout.fragment_mini_player) {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val songPosition = sharedPreferences.getInt(POSITION_KEY, -1)

        view.miniPlayerTitle.isSelected = true

        if (songPosition != -1)
            miniPlayerTitle.text =
                listOfSongs[songPosition].name + " . " + listOfSongs[songPosition].artistName

        if (mediaPlayer!!.isPlaying) {
            miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_play)
        }

        miniPlayerPlayPauseButton.setOnClickListener {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer?.pause()
                miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_play)
            } else {
                mediaPlayer?.start()
                miniPlayerPlayPauseButton.setImageResource(R.drawable.ic_pause)
            }
        }
    }
}