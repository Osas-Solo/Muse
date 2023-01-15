package com.ostech.muse.models.api.response

import com.google.gson.annotations.SerializedName

data class Artist (
    @SerializedName("name")
    val name: String
)