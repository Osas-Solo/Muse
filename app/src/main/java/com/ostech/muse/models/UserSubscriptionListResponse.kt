package com.ostech.muse.models


import com.google.gson.annotations.SerializedName

data class UserSubscriptionListResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("subscriptions")
    val subscriptions: List<Subscription>,
    @SerializedName("total")
    val total: Int
)