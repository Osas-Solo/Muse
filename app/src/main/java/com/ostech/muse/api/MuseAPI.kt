package com.ostech.muse.api

import com.ostech.muse.models.SubscriptionType
import com.ostech.muse.models.User
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface MuseAPI {
    @GET("user-api/subscription-types")
    suspend fun getSubscriptionTypes(): Array<SubscriptionType>

    @FormUrlEncoded
    @POST("user-api/users")
    suspend fun signupUser(
        @Field("firstName") firstName: String,
        @Field("lastName") lastName: String,
        @Field("gender") gender: Char,
        @Field("emailAddress") emailAddress: String,
        @Field("password") password: String,
        @Field("passwordConfirmer") passwordConfirmer: String,
        @Field("phoneNumber") phoneNumber: String,
    ): Call<User>

    @FormUrlEncoded
    @POST("user-api/users/login")
    suspend fun loginUser(
        @Field("emailAddress") emailAddress: String,
        @Field("password") password: String,
    ): Call<User>


}