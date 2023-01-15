package com.ostech.muse.music

import android.net.Uri
import com.google.gson.annotations.SerializedName
import java.io.File
import java.util.*

data class Music(
    var isSelected: Boolean = false,
    val uri: Uri,
    val file: File,
    var title: String?,
    var artist: String?,
    var album: String?,
    var genre: String?,
    var year: Date?,
)