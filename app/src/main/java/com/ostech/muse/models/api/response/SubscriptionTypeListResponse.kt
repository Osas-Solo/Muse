package com.ostech.muse.models.api.response


import com.google.gson.annotations.SerializedName

data class SubscriptionTypeListResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("subscriptionTypes")
    val subscriptionTypes: List<SubscriptionType>,
    @SerializedName("total")
    val total: Int
)