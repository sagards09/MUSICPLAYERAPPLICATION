package com.example.musicplayerapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayerapplication.databinding.MusicViewBinding


class MusicAdapter(private var context : Context, private var musicList : ArrayList<Music>, private val playlistDetails: Boolean = false, private val selectionActivity: Boolean = false) : RecyclerView.Adapter<MusicAdapter.MyHolder>() {

    class MyHolder(binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context) , parent , false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = musicList[position].duration.toString()
        Glide.with(context).
        load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
            .into(holder.image)
        holder.root.setOnClickListener{
            when{
                playlistDetails ->{
                    holder.root.setOnClickListener{
                        sendIntent(ref = "PlaylistDetailsAdapter",pos = position)
                    }
                }
                selectionActivity ->{
                    holder.root.setOnClickListener {
                        if (addSong(musicList[position]))
                            holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.cool_pink))
                        else
                            holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
                    }
                }
                else ->{
                    holder.root.setOnClickListener {
                        when{
                            SongsActivity.search -> sendIntent(ref = "MusicAdapterSearch",pos = position)
                            musicList[position].id == PlayerActivity.nowPLayingId ->
                                sendIntent(ref = "NowPlaying",pos = PlayerActivity.songPosition)
                            else->sendIntent(ref = "MusicAdapter",pos = position)
                        }
                    }
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    private fun sendIntent(ref : String,pos:Int){
        val intent = Intent(context,PlayerActivity::class.java)
        intent.putExtra("index",pos)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)
    }

    private fun addSong(song : Music): Boolean{
        PlayListActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.forEachIndexed{index, music ->
            if (song.id == music.id){
                PlayListActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.removeAt(index)
                return false
            }
        }
        PlayListActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.add(song)
        return true
    }

    fun refreshPlaylists() {
        musicList = ArrayList()
        musicList = PlayListActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist
        notifyDataSetChanged()
    }

    fun updateMusiclist(searchList : ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }
}