package com.ostech.muse.music

import android.net.Uri
import java.io.File
import java.util.*

data class Music(
    var isSelected: Boolean = false,
    val uri: Uri,
    val file: File,
    var isRecognitionSuccessful: Boolean = false,
    var title: String?,
    var artists: MutableList<String>?,
    var album: String?,
    var genres: MutableList<String>?,
    var year: Int?,
) {
    companion object {
        fun getTotalSuccessfullyRecognisedFiles(musicList: List<Music>): Int {
            var count = 0

            for (currentMusic in musicList) {
                if (currentMusic.isRecognitionSuccessful && currentMusic.isSelected) {
                    count++
                }
            }

            return count
        }
    }
}