package com.zakariya.mymusicplayer.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.adapter.SongsAdapter
import com.zakariya.mymusicplayer.repository.SongRepository
import com.zakariya.mymusicplayer.ui.SongViewModel
import com.zakariya.mymusicplayer.ui.SongViewModelFactory
import com.zakariya.mymusicplayer.views.ScrollingViewOnApplyWindowInsetsListener
import kotlinx.android.synthetic.main.fragment_song.*
import me.zhanghai.android.fastscroll.FastScroller
import me.zhanghai.android.fastscroll.FastScrollerBuilder


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

        val fastScroller = createFastScroller(rvSongs)

        rvSongs.setOnApplyWindowInsetsListener(
            ScrollingViewOnApplyWindowInsetsListener(rvSongs, fastScroller)
        )

        viewModel.songLiveData.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                adapter.updateSongList(it)
            } else {
                adapter.updateSongList(emptyList())
            }
        })
    }

    private fun createFastScroller(recyclerView: RecyclerView): FastScroller? {
        return FastScrollerBuilder(recyclerView).useMd2Style().build()
    }

    private fun initAdapter() {
        adapter = SongsAdapter(requireContext(), mutableListOf())
        rvSongs.adapter = adapter
    }

    private fun initLayoutManager() {
        rvSongs.layoutManager = LinearLayoutManager(activity as Context)
    }
}