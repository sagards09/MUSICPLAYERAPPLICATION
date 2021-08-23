package com.example.musicplayerapplication

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Button
import androidx.core.util.TimeUtils.formatDuration

class MusicService : Service() {

    private var myBinder = MyBinder()
    var mediaPlayer:MediaPlayer? = null
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(intent: Intent?): IBinder{
        mediaSession = MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }

    inner class MyBinder:Binder(){
        fun currentService(): MusicService{
            return this@MusicService
        }
    }

    fun createMediaPlayer(){
        try{
            if (PlayerActivity.musicService!!.mediaPlayer == null) PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.playPauseBtnPA.setImageResource(R.drawable.ic_baseline_pause_24)
            PlayerActivity.binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarEnd.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekbarPA.progress = 0
            PlayerActivity.binding.seekbarPA.max = mediaPlayer!!.duration
            PlayerActivity.nowPLayingId = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
        }catch (e:Exception){return}
    }

    fun seekBarSetup(){
        runnable = Runnable {
            PlayerActivity.binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekbarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }
}