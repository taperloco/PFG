package com.example.recado.data

import android.content.Context

/**
 *  Saves and returns JWT tokens
 */
class TokenManager(private val context: Context) {
    private val PREFS_NAME = "secure_prefs"
    private val TOKEN_KEY = "jwt_token"

    fun saveToken(token: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(TOKEN_KEY, null)
    }
}