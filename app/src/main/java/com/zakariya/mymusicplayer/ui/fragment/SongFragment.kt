package com.zakariya.mymusicplayer.ui.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.adapter.SongsAdapter
import com.zakariya.mymusicplayer.repository.SongRepository
import com.zakariya.mymusicplayer.ui.SongViewModel
import com.zakariya.mymusicplayer.ui.SongViewModelFactory
import com.zakariya.mymusicplayer.util.Constants.POSITION_KEY
import com.zakariya.mymusicplayer.util.Constants.PREF_NAME
import com.zakariya.mymusicplayer.util.OnSongClickListener
import kotlinx.android.synthetic.main.fragment_song.*

@Suppress("DEPRECATION")
class SongFragment : Fragment(R.layout.fragment_song) {

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        var currentPlyingSongPosition = -1
    }

    private lateinit var viewModel: SongViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = SongRepository(requireContext())
        val viewModelFactory = SongViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SongViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        rvSongs.layoutManager = LinearLayoutManager(activity as Context)
        val listener = object : OnSongClickListener {
            override fun onSongClickListener(position: Int, songPath: String) {
                Toast.makeText(requireContext(), "clicked", Toast.LENGTH_SHORT).show()
                currentPlyingSongPosition = position

                with(sharedPreferences.edit()) {
                    putInt(POSITION_KEY, currentPlyingSongPosition)
                    apply()
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.playerFragmentContainer, PlayerFragment())
                    .commit()
            }
        }

        viewModel.songLiveData.observe(viewLifecycleOwner, {
            rvSongs.adapter = SongsAdapter(activity as Context, it, listener)
        })
    }
}