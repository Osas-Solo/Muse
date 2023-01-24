package com.ostech.muse

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ostech.muse.databinding.ListItemMusicBinding
import com.ostech.muse.music.Music
import java.io.IOException

class MusicHolder(
    private val binding: ListItemMusicBinding
) : RecyclerView.ViewHolder(binding.root) {
    var musicCheckBox = binding.musicCheckBox
    var musicPlayButton = binding.musicPlayImageButton
    var musicRemoveButton = binding.musicRemoveImageButton
    var musicTitleTextView = binding.musicTitleTextView
    var musicArtistTextView = binding.musicArtistTextView
    var musicAlbumTextView = binding.musicAlbumTextView
    var musicGenreTextView = binding.musicGenreTextView
    var musicYearTextView = binding.musicYearTextView

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
            stopMusic()
        } else {
            try {
                musicPlayer.setDataSource(this.binding.root.context, currentMusic.uri)
                musicPlayer.prepare()
                musicPlayer.start()
                Log.i("Music Holder", "playMusic: Now playing: ${currentMusic.file.name}")
            } catch (e: IOException) {
                Log.e("Music Holder", "playMusic: $e")
            }
        }
    }

    fun stopMusic() {
        musicPlayer.stop()
        musicPlayer.reset()
        Log.i("Music Holder", "stopMusic: Stopping music")
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
            removeMusic(position)
            holder.stopMusic()
        }
    }

    private fun removeMusic(musicFileIndex: Int) {
        musicList.removeAt(musicFileIndex)
        notifyDataSetChanged()
    }

    override fun getItemCount() = musicList.size
}