package com.ostech.muse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ostech.muse.databinding.ListItemMusicBinding
import com.ostech.muse.music.Music

class MusicHolder(
    private val binding: ListItemMusicBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(currentMusic: Music) {
        binding.musicFileNameTextView.text = currentMusic.file.name
    }
}

class MusicListAdapter(
    private val musicList: List<Music>
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
    }

    override fun getItemCount() = musicList.size
}