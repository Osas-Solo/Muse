package com.ostech.muse.models.api.response

import com.google.gson.annotations.SerializedName

data class SubscriptionType(
    @SerializedName("subscriptionTypeID")
    val subscriptionTypeID: Int,
    @SerializedName("subscriptionCode")
    val subscriptionCode: String,
    @SerializedName("subscriptionName")
    val subscriptionName: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("numberOfSongs")
    val numberOfSongs: Int
)
