package com.ostech.muse.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MuseAPIBuilder {
    private val client = OkHttpClient.Builder().build()
    private const val MUSE_API_URL = "https://muse-api-osas-solo.herokuapp.com/"

    val museAPIService = Retrofit.Builder()
        .baseUrl(MUSE_API_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
        .create(MuseAPIInterface::class.java)
}