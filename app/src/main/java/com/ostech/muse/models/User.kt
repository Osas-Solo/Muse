package com.ostech.muse.models

import java.util.Date

data class User(
    val userID: Int,
    val emailAddress: String,
    val fullName: String,
    val gender: String,
    val phoneNumber: String,
    val signupDate: Date,
    val currentSubscription: Subscription?
)