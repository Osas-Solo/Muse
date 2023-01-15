package com.ostech.muse.music

import android.net.Uri
import java.io.File
import java.util.*

data class Music(
    var isSelected: Boolean = false,
    val uri: Uri,
    val file: File,
    var title: String?,
    var artists: MutableList<String>?,
    var album: String?,
    var genres: MutableList<String>?,
    var year: Int?,
)