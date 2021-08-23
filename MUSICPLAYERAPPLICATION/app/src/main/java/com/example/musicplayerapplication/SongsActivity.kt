package com.example.musicplayerapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayerapplication.databinding.ActivitySongsBinding
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class SongsActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySongsBinding
    private lateinit var musicAdapter: MusicAdapter

    companion object{
        lateinit var MusicListMA : ArrayList<Music>
        lateinit var musicListSearch : ArrayList<Music>
        var search : Boolean = false
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs)
            initializeLayout()

            FavouriteActivity.favouriteSongs = ArrayList()
            val editor = getSharedPreferences("Favourites", MODE_PRIVATE)
            val jsonString = editor.getString("FavouriteSongs", null)
            val typeToken = object:TypeToken<ArrayList<Music>>(){}.type
            if (jsonString != null) {
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString,typeToken)
                FavouriteActivity.favouriteSongs.addAll(data)
            }

        PlayListActivity.musicPlaylist = MusicPlaylist()
        val jsonStringPlaylist = editor.getString("MuiscPlaylist", null)
        if (jsonStringPlaylist != null) {
            val dataPlayList: MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist, MusicPlaylist::class.java)
            PlayListActivity.musicPlaylist = dataPlayList
        }

        binding.shuffleBtn.setOnClickListener {
            val intent = Intent(this@SongsActivity , PlayerActivity::class.java)
            intent.putExtra("index" ,0)
            intent.putExtra("class" , "SongsActivity")
            startActivity(intent)
        }
        binding.playlistBtn.setOnClickListener {
            val intent = Intent(this@SongsActivity, PlayListActivity::class.java)
            startActivity(intent)
        }
        binding.favouriteBtn.setOnClickListener {
            val intent = Intent(this@SongsActivity , FavouriteActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requestRunTimePermission() {
        if(ActivityCompat.checkSelfPermission(this , android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this , arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) , 13)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 13){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this , "Permission Granted" , Toast.LENGTH_SHORT).show()
        }else
            ActivityCompat.requestPermissions(this , arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) , 13)

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n")
    private fun initializeLayout(){
        search = false
        requestRunTimePermission()
        setTheme(R.style.coolPinkNav)
        binding = ActivitySongsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MusicListMA = getAllAudio()
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        binding.musicRV.layoutManager = LinearLayoutManager(this)
        musicAdapter = MusicAdapter(this , MusicListMA)
        binding.musicRV.adapter = musicAdapter
        binding.totalSongs.text = "Total Songs : " + musicAdapter.itemCount

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getAllAudio() :ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection =  MediaStore.Audio.Media.IS_MUSIC + "!= 0"  //to check whether data is equal to null or not
        val projection = arrayOf(MediaStore.Audio.Media._ID , MediaStore.Audio.Media.TITLE , MediaStore.Audio.Media.ALBUM , MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION , MediaStore.Audio.Media.DATE_ADDED , MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ALBUM_ID)
        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI , projection, selection, null,
            MediaStore.Audio.Media.DATE_ADDED , null)//latest songs added
        if(cursor != null){  //till the values gets null its keeps on moving
            if(cursor.moveToFirst())
                do{//cursor will get the title of the particular song
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idc = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val albumIdc = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri , albumIdc).toString()
                    val music = Music(id = idc , title =  titleC , album =  albumC , artist = artistC , path = pathC , duration = durationC , artUri = artUriC)
                    val file = File(music.path)
                    if(file.exists()) tempList.add(music)

                } while (cursor.moveToNext()); cursor.close()
        }


        return tempList
    }

    override fun onResume() {
        super.onResume()
        val editor = getSharedPreferences("Favourites", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouriteActivity.favouriteSongs)
        editor.putString("FavouriteSongs", jsonString)
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlayListActivity.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
    }


}