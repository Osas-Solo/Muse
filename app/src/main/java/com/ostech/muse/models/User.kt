package com.ostech.muse.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class User(
    @SerializedName("userID")
    val userID: Int,
    @SerializedName("emailAddress")
    val emailAddress: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("signupDate")
    val signupDate: Date,
    @SerializedName("currentSubscription")
    val currentSubscription: Subscription?
)