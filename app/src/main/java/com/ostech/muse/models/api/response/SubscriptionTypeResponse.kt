package com.ostech.muse.models.api.response


import com.google.gson.annotations.SerializedName

data class SubscriptionTypeResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("subscriptionType")
    val subscriptionType: SubscriptionType
)