package com.zakariya.mymusicplayer.ui.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zakariya.mymusicplayer.PlayerHelper.PlayBack.getSongFromSharedStorage
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.adapter.SongsAdapter
import com.zakariya.mymusicplayer.model.Songs
import com.zakariya.mymusicplayer.ui.fragment.PlayerFragment.Companion.mediaPlayer
import com.zakariya.mymusicplayer.util.OnSongClickListener
import com.zakariya.mymusicplayer.util.POSITION_KEY
import com.zakariya.mymusicplayer.util.PREF_NAME
import kotlinx.android.synthetic.main.fragment_song.view.*

@Suppress("DEPRECATION")
class SongFragment : Fragment(R.layout.fragment_song) {
    private var listOfSongs = arrayListOf<Songs>()
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        var currentPlyingSongPosition = -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        listOfSongs = getSongFromSharedStorage(requireContext())

        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        view.rvSongs.layoutManager = LinearLayoutManager(activity as Context)
        val listener = object : OnSongClickListener {
            override fun onSongClickListener(position: Int, songPath: String) {
                Toast.makeText(requireContext(), "clicked", Toast.LENGTH_SHORT).show()
                currentPlyingSongPosition = position

                mediaPlayer?.reset()
                with(sharedPreferences.edit()) {
                    putInt(POSITION_KEY, currentPlyingSongPosition)
                    apply()
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.playerFragmentContainer, PlayerFragment())
                    .commit()
            }
        }
        view.rvSongs.adapter = SongsAdapter(activity as Context, listOfSongs, listener)
    }
}