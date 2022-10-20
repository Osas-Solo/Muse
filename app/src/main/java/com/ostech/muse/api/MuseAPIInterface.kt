package com.ostech.muse.api

import com.ostech.muse.models.Subscription
import com.ostech.muse.models.SubscriptionType
import com.ostech.muse.models.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MuseAPIInterface {
    @GET("user-api/subscription-types")
    suspend fun getSubscriptionTypes(): Call<Array<SubscriptionType>>

    @GET("user-api/subscription-types/{id}")
    suspend fun getSubscriptionTypeByID(@Path(value = "id") id: Int): Call<SubscriptionType>

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
    ): Response<String>

    @FormUrlEncoded
    @POST("user-api/users/login")
    suspend fun loginUser(
        @Field("emailAddress") emailAddress: String,
        @Field("password") password: String,
    ): Call<User>

    @GET("user-api/users/{userID}/subscriptions")
    suspend fun getSubscriptions(@Path(value = "userID") userID: Int): Call<Array<Subscription>>

    @GET("user-api/users/{userID}/subscriptions/{subscriptionID}")
    suspend fun getSubscriptionByID(
        @Path(value = "userID") userID: Int,
        @Path(value = "subscriptionID") subscriptionID: Int
    ): Call<Subscription>
}