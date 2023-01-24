package com.ostech.muse.music

import android.net.Uri
import java.io.File

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
    fun isSuccessfullyRecognized(): Boolean {
        return isRecognitionSuccessful && isSelected
    }

    companion object {
        fun List<Music>.getTotalSuccessfullyRecognisedFiles(): Int {
            var count = 0

            for (currentMusic in this) {
                if (currentMusic.isRecognitionSuccessful && currentMusic.isSelected) {
                    count++
                }
            }

            return count
        }
    }
}