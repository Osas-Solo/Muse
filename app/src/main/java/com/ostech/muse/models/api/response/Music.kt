package com.ostech.muse.models.api.response

import com.google.gson.annotations.SerializedName
import java.util.*

data class Music(
    @SerializedName("album")
    val albumName: Album?,
    @SerializedName("title")
    val trackTitle: String?,
    @SerializedName("artists")
    val artists: List<Artist>?,
    @SerializedName("genres")
    val genres: List<Genre>?,
    @SerializedName("release_date")
    val releaseDate: Date?
)
