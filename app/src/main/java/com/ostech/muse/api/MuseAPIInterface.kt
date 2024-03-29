package com.ostech.muse.api

import com.ostech.muse.models.api.response.RecognitionResponse
import com.ostech.muse.models.api.response.SubscriptionTypeListResponse
import com.ostech.muse.models.api.response.SubscriptionTypeResponse
import com.ostech.muse.models.api.response.UserLoginResponse
import com.ostech.muse.models.api.response.UserProfileResponse
import com.ostech.muse.models.api.response.UserSignupResponse
import com.ostech.muse.models.api.response.UserSubscriptionListResponse
import com.ostech.muse.models.api.response.UserSubscriptionResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface MuseAPIInterface {
    @GET("user-api/subscription-types")
    suspend fun getSubscriptionTypes(): Response<SubscriptionTypeListResponse>

    @GET("user-api/subscription-types/{id}")
    suspend fun getSubscriptionTypeByID(@Path(value = "id") id: Int): Response<SubscriptionTypeResponse>

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
    ): Response<UserSignupResponse>

    @FormUrlEncoded
    @POST("user-api/users/login")
    suspend fun loginUser(
        @Field("emailAddress") emailAddress: String,
        @Field("password") password: String,
    ): Response<UserLoginResponse>

    @GET("user-api/users/{userID}")
    suspend fun getUserProfile(@Path(value = "userID") userID: Int): Response<UserProfileResponse>

    @GET("user-api/users/{userID}/subscriptions")
    suspend fun getSubscriptions(@Path(value = "userID") userID: Int): Response<UserSubscriptionListResponse>

    @GET("user-api/users/{userID}/subscriptions/{subscriptionID}")
    suspend fun getSubscriptionByID(
        @Path(value = "userID") userID: Int,
        @Path(value = "subscriptionID") subscriptionID: Int
    ): Response<UserSubscriptionResponse>

    @FormUrlEncoded
    @POST("user-api/users/{userID}/subscriptions/{subscriptionID}")
    suspend fun updateSubscription(
        @Path(value = "userID") userID: Int,
        @Path(value = "subscriptionID") subscriptionID: Int,
        @Field("numberOfNewlyRecognisedSongs") numberOfNewlyRecognisedSongs: Int,
    ): Response<UserSubscriptionResponse>

    @FormUrlEncoded
    @POST("user-api/users/{userID}/subscriptions")
    suspend fun paySubscription(
        @Path(value = "userID") userID: Int,
        @Field("transactionReference") transactionReference: String,
        @Field("subscriptionType") subscriptionTypeID: Int,
        @Field("amountPaid") amountPaid: Double,
    ): Response<UserSubscriptionResponse>

    @Multipart
    @POST("recogniser/recognise")
    suspend fun recogniseSong (
        @Part musicFile: MultipartBody.Part
    ): Response<RecognitionResponse>
}