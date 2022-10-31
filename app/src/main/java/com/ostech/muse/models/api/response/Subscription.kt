package com.ostech.muse.models.api.response

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Subscription(
    @SerializedName("subscriptionID")
    val subscriptionID: Int,
    @SerializedName("transactionReference")
    val transactionReference: String,
    @SerializedName("subscriptionType")
    val subscriptionType: String,
    @SerializedName("numberOfRecognisedSongs")
    val numberOfRecognisedSongs: Int,
    @SerializedName("numberOfSongsLeft")
    val numberOfSongsLeft: Int,
    @SerializedName("subscriptionDate")
    val subscriptionDate: Date,
    @SerializedName("pricePaid")
    val pricePaid: Double
)
