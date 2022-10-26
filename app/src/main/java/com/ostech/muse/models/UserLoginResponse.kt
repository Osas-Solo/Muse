package com.ostech.muse.models

import com.google.gson.annotations.SerializedName

data class UserLoginResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("user")
    val user: User,
    @SerializedName("sessionToken")
    val sessionToken: String
)