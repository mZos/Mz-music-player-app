package com.zakariya.mymusicplayer.ui.activity

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.model.Songs

class PlayerActivity : AppCompatActivity() {

    val TAG = "PlayerActivityTest"

    private var listOfSongs: ArrayList<Songs> = arrayListOf()
    private var position: Int = -1
//
//    companion object {
//        var mediaPlayer: MediaPlayer? = MediaPlayer()
//        var currentPlayingPosition: Int? = -1
//        var isPlaying = false
//    }

    private lateinit var uri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

//        if (intent != null) {
//            position = intent.getIntExtra("position", -1)
//            mediaPlayer?.reset()
//        }
//
//        currentPlayingPosition = position
//        getSongs()
//
//        mediaPlayer?.setDataSource(this, Uri.parse(listOfSongs[position].path))
//        mediaPlayer?.prepare()
//        mediaPlayer?.start()
//        imgPlay.visibility = View.GONE
//
//        imgPlay.setOnClickListener {
//            mediaPlayer?.start()
//            isPlaying = true
//            it.visibility = View.GONE
//            imgPause.visibility = View.VISIBLE
//        }
//
//        imgPause.setOnClickListener {
//            mediaPlayer?.pause()
//            isPlaying = false
//            it.visibility = View.GONE
//            imgPlay.visibility = View.VISIBLE
//        }

    }

    private fun getSongs() {
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST
        )

        val cursor = applicationContext.contentResolver.query(
            uri,
            projection,
            null,
            null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val title = cursor.getString(0)
                val id = cursor.getString(1)
                val path = cursor.getString(2)
                val artistName = cursor.getString(3)
                val song = Songs(id, title, path, artistName)
                listOfSongs.add(song)
            }
            cursor.close()
        }

    }
}