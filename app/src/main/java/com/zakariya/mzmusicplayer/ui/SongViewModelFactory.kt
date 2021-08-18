package com.zakariya.mzmusicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zakariya.mzmusicplayer.repository.SongRepository

@Suppress("UNCHECKED_CAST")
class SongViewModelFactory(private val repository: SongRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SongViewModel(repository) as T
    }
}