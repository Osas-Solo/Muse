package com.ostech.muse.models

data class SubscriptionType(
    val subscriptionTypeID: Int,
    val subscriptionCode: String,
    val subscriptionName: String,
    val price: Double,
    val numberOfSongs: Int
)
