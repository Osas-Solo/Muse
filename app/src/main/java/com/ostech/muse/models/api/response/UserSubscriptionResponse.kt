package com.ostech.muse.models.api.response


import com.google.gson.annotations.SerializedName

data class UserSubscriptionResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("subscription")
    val subscription: Subscription
)