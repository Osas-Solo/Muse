package com.ostech.muse.music

import com.google.gson.annotations.SerializedName
import java.io.File
import java.util.*

data class Music(
    val file: File,
    @SerializedName("title")
    val title: String?,
    @SerializedName("artist[0]")
    val artist: String?,
    @SerializedName("album.name")
    val album: String?,
    @SerializedName("genres[0]")
    val genre: String?,
    @SerializedName("release_date")
    val year: Date?,
    @SerializedName("track")
    val trackNumber: Int?,
)
