package com.ostech.muse.models

import java.util.Date

data class Subscription(
    val subscriptionID: Int,
    val transactionReference: String,
    val subscriptionType: SubscriptionType,
    val numberOfRecognisedSongs: Int,
    val numberOfSongsLeft: Int,
    val subscriptionDate: Date,
    val pricePaid: Double
)
