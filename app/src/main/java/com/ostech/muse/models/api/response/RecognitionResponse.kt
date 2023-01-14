package com.ostech.muse.models.api.response

import com.google.gson.annotations.SerializedName

data class RecognitionResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("result_type")
    val result_type: String?,
    @SerializedName("metadata")
    val metadata: String?,
    @SerializedName("cost_time")
    val cost_time: String?,
)
