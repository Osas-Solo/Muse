package com.ostech.muse.models.api.response

import com.google.gson.annotations.SerializedName

data class RecognitionResponseStatus(
    @SerializedName("version")
    val version: String?,
    @SerializedName("msg")
    val msg: String?,
    @SerializedName("code")
    val code: Int?
)