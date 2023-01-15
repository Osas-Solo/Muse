package com.ostech.muse.models.api.response

import com.google.gson.annotations.SerializedName

data class Album (
    @SerializedName("name")
    val name: String
)