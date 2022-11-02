package com.ostech.muse.music

import android.net.Uri
import com.google.gson.annotations.SerializedName
import java.io.File
import java.util.*

data class Music(
    var isSelected: Boolean = false,
    val uri: Uri,
    val file: File,
    @SerializedName("title")
    var title: String?,
    @SerializedName("artist[0]")
    var artist: String?,
    @SerializedName("album.name")
    var album: String?,
    @SerializedName("genres[0]")
    var genre: String?,
    @SerializedName("release_date")
    var year: Date?,
    @SerializedName("track")
    var trackNumber: Int?,
)
