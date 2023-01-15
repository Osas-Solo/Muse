package com.ostech.muse.models.api.response

import com.google.gson.annotations.SerializedName

data class MusicMetadata (
    @SerializedName("music")
    val music: List<Music>
)
