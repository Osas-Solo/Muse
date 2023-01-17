package com.ostech.muse.models.api.response

import com.google.gson.annotations.SerializedName

data class RecognitionResponse(
    @SerializedName("status")
    val status: RecognitionResponseStatus?,
    @SerializedName("result_type")
    val resultType: Any?,
    @SerializedName("metadata")
    val metadata: MusicMetadata?,
    @SerializedName("cost_time")
    val costTime: Any?,
)
