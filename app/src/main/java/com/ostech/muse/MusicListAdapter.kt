package com.ostech.muse

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ostech.muse.databinding.ListItemMusicBinding
import com.ostech.muse.music.Music
import java.io.File
import java.io.IOException

class MusicHolder(
    private val binding: ListItemMusicBinding
) : RecyclerView.ViewHolder(binding.root) {
    private var musicCheckBox = binding.musicCheckBox
    private var musicPlayButton = binding.musicPlayImageButton
    var musicRemoveButton = binding.musicRemoveImageButton
    private var musicTitleTextView = binding.musicTitleTextView
    private var musicArtistTextView = binding.musicArtistTextView
    private var musicAlbumTextView = binding.musicAlbumTextView
    private var musicGenreTextView = binding.musicGenreTextView
    private var musicTrackNumberTextView = binding.musicTrackNumberTextView
    private var musicYearTextView = binding.musicYearTextView

    private val musicPlayer = MediaPlayer()

    fun bind(currentMusic: Music) {
        musicCheckBox.text = currentMusic.file.name

        musicCheckBox.setOnCheckedChangeListener { _, isChecked ->
            currentMusic.isSelected = isChecked
        }

        musicPlayButton.setOnClickListener {
            playMusic(currentMusic)
        }

        musicRemoveButton.setOnClickListener {

        }

        musicPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
    }

    private fun playMusic(currentMusic: Music) {
        if (musicPlayer.isPlaying) {
            musicPlayer.stop()
        }

        try {
//            val musicFileDescriptor = binding.root.context.assets.openFd(musicFile.path)
//            musicPlayer.setDataSource(musicFileDescriptor.fileDescriptor)
            musicPlayer.setDataSource(currentMusic.uri.toString())
            musicPlayer.prepare()
            musicPlayer.start()
        } catch (e: IOException) {
            Log.e("Music Holder", "playMusic: $e")
        }
    }

    private fun removeMusic(musicFileIndex: Int) {

    }
}

class MusicListAdapter(
    private val musicList: MutableList<Music>
) : RecyclerView.Adapter<MusicHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MusicHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemMusicBinding.inflate(inflater, parent, false)

        return MusicHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicHolder, position: Int) {
        val currentMusic = musicList[position]
        holder.bind(currentMusic)

        holder.musicRemoveButton.setOnClickListener {
            musicList.removeAt(position)
        }
    }

    override fun getItemCount() = musicList.size
}