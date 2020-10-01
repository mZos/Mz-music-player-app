package com.zakariya.mymusicplayer.adapter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zakariya.mymusicplayer.R
import com.zakariya.mymusicplayer.model.Songs
import com.zakariya.mymusicplayer.ui.fragment.SongFragment
import com.zakariya.mymusicplayer.util.OnSongClickListener
import kotlinx.android.synthetic.main.single_song_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongsAdapter(
    val context: Context,
    private val songs: ArrayList<Songs>,
    private val listener: OnSongClickListener
) : RecyclerView.Adapter<SongsAdapter.SongsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        return SongsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.single_song_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        val songs = songs[position]
        holder.itemView.txtSongName.text = songs.name

        SongFragment().lifecycleScope.launch(Dispatchers.IO) {
            val imgByte = getSongThumbnail(songs.path)
            withContext(Dispatchers.Main) {
                Glide.with(context).asBitmap().load(imgByte).error(R.drawable.ic_album)
                    .into(holder.itemView.imgThumbnail)
            }
        }

        holder.itemView.txtArtistName.text = songs.artistName

        holder.itemView.setOnClickListener {
            listener.onSongClickListener(position, songs.path)
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    class SongsViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private fun getSongThumbnail(songPath: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(songPath)
        val imgByte = retriever.embeddedPicture
        retriever.release()
        return imgByte
    }
}
