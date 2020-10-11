package com.zakariya.mymusicplayer.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.adapter.SongsAdapter
import com.zakariya.mymusicplayer.repository.SongRepository
import com.zakariya.mymusicplayer.ui.SongViewModel
import com.zakariya.mymusicplayer.ui.SongViewModelFactory
import com.zakariya.mymusicplayer.util.OnSongClickListener
import kotlinx.android.synthetic.main.fragment_song.*

class SongFragment : Fragment(R.layout.fragment_song) {

    private lateinit var viewModel: SongViewModel
    private lateinit var adapter: SongsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = SongRepository(requireContext())
        val viewModelFactory = SongViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(SongViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        viewModel.forceReload()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLayoutManager()
        initAdapter()

        viewModel.songLiveData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                adapter.updateSongList(it)
            } else {
                adapter.updateSongList(emptyList())
            }
        })
    }

    private fun initAdapter() {
        val listener = object : OnSongClickListener {
            override fun onSongClickListener(position: Int, songPath: String) {
                restartPlayerFragment()
            }
        }
        adapter = SongsAdapter(requireContext(), mutableListOf(), listener)
        rvSongs.adapter = adapter
    }

    private fun initLayoutManager() {
        rvSongs.layoutManager = LinearLayoutManager(activity as Context)
    }

    private fun restartPlayerFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.playerFragmentContainer, PlayerFragment())
            .commit()
    }
}