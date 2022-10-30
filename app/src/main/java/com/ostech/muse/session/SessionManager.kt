package com.ostech.muse.session

import android.content.Context
import android.content.SharedPreferences
import com.ostech.muse.R

class SessionManager (context: Context) {
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val SUBSCRIPTION_PLAN_ID = "subscription_plan_id"
        const val SUBSCRIPTION_AMOUNT = "subscription_amount"
    }

    fun saveAuthToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return sharedPreferences.getString(USER_TOKEN, null)
    }

    fun saveUserID(userID: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(USER_ID, userID)
        editor.apply()
    }

    fun fetchUserID(): Int {
        return sharedPreferences.getInt(USER_ID, 0)
    }

    fun saveSubscriptionPlanID(subscriptionPlanID: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(SUBSCRIPTION_PLAN_ID, subscriptionPlanID)
        editor.apply()
    }

    fun fetchSubscriptionPlanID(): Int {
        return sharedPreferences.getInt(SUBSCRIPTION_PLAN_ID, 0)
    }

    fun saveSubscriptionPrice(price: Double) {
        val editor = sharedPreferences.edit()
        editor.putFloat(SUBSCRIPTION_AMOUNT, price.toFloat())
        editor.apply()
    }

    fun fetchSubscriptionPrice(): Double {
        return sharedPreferences.getFloat(SUBSCRIPTION_AMOUNT, 0F).toDouble()
    }
}