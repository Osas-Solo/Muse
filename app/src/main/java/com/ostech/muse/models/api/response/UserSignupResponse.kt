package com.ostech.muse.models.api.response

import com.google.gson.annotations.SerializedName

data class UserSignupResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("user")
    val user: User
)
