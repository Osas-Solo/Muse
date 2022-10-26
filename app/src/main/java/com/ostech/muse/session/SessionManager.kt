package com.ostech.muse.session

import android.content.Context
import android.content.SharedPreferences
import com.ostech.muse.R

class SessionManager (context: Context) {
    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
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
}