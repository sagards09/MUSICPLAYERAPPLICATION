package com.example.musicplayerapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.databinding.adapters.SeekBarBindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayerapplication.R
import com.example.musicplayerapplication.databinding.ActivityPlayerBinding
import com.google.firebase.components.Component
import java.util.concurrent.TimeUnit

class PlayerActivity : AppCompatActivity(), ServiceConnection , MediaPlayer.OnCompletionListener{

    companion object{
        lateinit var musicListPA : ArrayList<Music>
        var songPosition : Int = 0
        var mediaPlayer : MediaPlayer? = null
        var isPlaying : Boolean = false
        var musicService : MusicService? = null
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var isFavourite: Boolean = false
        var fIndex : Int = -1
        var nowPLayingId: String = ""
    }

    private lateinit var binding : ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.deleteplayerBtn.setOnClickListener { finish() }
        initializeLayout()
        binding.playPauseBtnPA.setOnClickListener{
            if(isPlaying) pauseMusic()
            else playMusic()
        }
        binding.previousBtnPA.setOnClickListener{ prevNextSong(increment = false )}
        binding.nextBtnPA.setOnClickListener {prevNextSong(increment = true)  }
        binding.seekbarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer!!.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        binding.repeatBtnPA.setOnClickListener {
            if (!repeat){
                repeat = true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            }else{
                repeat = true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.black))
            }
        }
        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent,"Sharing Music File!!!"))
        }

        binding.favouriteBtnPA.setOnClickListener {
            if (isFavourite){
                isFavourite = false
                binding.favouriteBtnPA.setImageResource(R.drawable.favouirte_empty_icon)
                FavouriteActivity.favouriteSongs.removeAt(fIndex)
            }else {
                isFavourite = true
                binding.favouriteBtnPA.setImageResource(R.drawable.ic_baseline_favorite_24)
                FavouriteActivity.favouriteSongs.add(musicListPA[songPosition])
            }
        }
    }

    private fun setLayout() {
        fIndex = favouriteChecker(musicListPA[songPosition].id)
        Glide.with(this).load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
            .into(binding.songImgPA)
        binding.songNamePA.text = musicListPA[songPosition].title
        if (repeat) binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
        if (isFavourite) binding.favouriteBtnPA.setImageResource(R.drawable.ic_baseline_favorite_24)
        else binding.favouriteBtnPA.setImageResource(R.drawable.favouirte_empty_icon)
    }

    private fun createMediaPlayer(){
        try {
            if(mediaPlayer == null) mediaPlayer = MediaPlayer()
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtnPA.setImageResource(R.drawable.ic_baseline_pause_24)
            binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
            binding.seekbarPA.progress = 0
            binding.seekbarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPLayingId = musicListPA[songPosition].id
        }catch (e: Exception) {
            return
        }
    }

    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index" ,0 )
        when(intent.getStringExtra("class")){

            "MusicAdapterSearch" ->{
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(SongsActivity.musicListSearch)
                setLayout()
            }
            "NowPlaying" ->{
                setLayout()
                binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.seekbarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekbarPA.max = musicService!!.mediaPlayer!!.currentPosition
                if (isPlaying) binding.playPauseBtnPA.setImageResource(R.drawable.ic_baseline_pause_24)
                else binding.playPauseBtnPA.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
            "MusicAdapter" ->{
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(SongsActivity.MusicListMA)
                setLayout()
                createMediaPlayer()
            }
            "SongsActivity" ->{
                val intent = Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA= ArrayList()
                musicListPA.addAll(SongsActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }

    private fun playMusic(){
        binding.playPauseBtnPA.setImageResource(R.drawable.ic_baseline_pause_24)
        isPlaying = true
        mediaPlayer!!.start()
    }

    private fun pauseMusic(){
        binding.playPauseBtnPA.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        isPlaying = false
        mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment: Boolean){
        if(increment){
            setSongPosition(increment = true )
            setLayout()
            createMediaPlayer()
        }else{
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

     override fun onServiceConnected(name: ComponentName?, service: IBinder?){
        val binder = service as MusicService.MyBinder
         musicService = binder.currentService()
         createMediaPlayer()
         musicService!!.seekBarSetup()

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }


    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        }catch (e:Exception){return}
    }
}