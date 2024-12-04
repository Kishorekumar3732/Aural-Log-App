package com.project01.myhome02

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "aural_log_prefs"
        private const val KEY_USER = "key_user"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // Save user details and login status
    fun saveUserSession(user: User) {
        val editor = prefs.edit()
        val userJson = Gson().toJson(user)
        editor.putString(KEY_USER, userJson)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    // Retrieve saved user details
    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return Gson().fromJson(userJson, User::class.java)
    }

    // Check if the user is logged in
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Clear user session
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
