package com.ostech.muse.models

import com.google.gson.annotations.SerializedName

data class UserSignupErrorResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("error")
    val error: String
)