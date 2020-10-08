package com.zakariya.mymusicplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import com.zakariya.mymusicplayer.model.Song
import java.io.IOException

@Suppress("DEPRECATION")
class PlayerHelper {
    companion object PlayBack {

        fun getSongFromSharedStorage(context: Context): ArrayList<Song> {
            val songList: ArrayList<Song> = arrayListOf()
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.ArtistColumns.ARTIST
            )

            val cursor = context.applicationContext.contentResolver.query(
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
                    val song = Song(id, title, path, artistName)
                    songList.add(song)
                }
                cursor.close()
            }
            return songList
        }

        fun initMediaPlayer(mediaPlayer: MediaPlayer?, songPath: String) {
            try {
                mediaPlayer?.setDataSource(songPath)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mediaPlayer?.prepare()
        }
    }
}
