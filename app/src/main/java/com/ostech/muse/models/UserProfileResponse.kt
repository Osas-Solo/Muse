package com.ostech.muse.models

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("user")
    val user: User,
)