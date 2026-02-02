package com.example.bdo

import android.content.Context
import android.content.SharedPreferences

/**
 * Session Manager
 * Handles user session data storage and retrieval
 */
object SessionManager {
    
    private const val PREF_NAME = "BDO_SESSION"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMAIL = "email"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_PHONE = "phone"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Save user session after successful login
     */
    fun saveUserSession(context: Context, user: User) {
        val editor = getPreferences(context).edit()
        editor.putInt(KEY_USER_ID, user.user_id)
        editor.putString(KEY_EMAIL, user.email)
        editor.putString(KEY_FULL_NAME, user.full_name)
        editor.putString(KEY_PHONE, user.phone)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }
    
    /**
     * Get current user ID
     */
    fun getUserId(context: Context): Int {
        return getPreferences(context).getInt(KEY_USER_ID, 0)
    }
    
    /**
     * Get current user email
     */
    fun getUserEmail(context: Context): String? {
        return getPreferences(context).getString(KEY_EMAIL, null)
    }
    
    /**
     * Get current user full name
     */
    fun getUserFullName(context: Context): String? {
        return getPreferences(context).getString(KEY_FULL_NAME, null)
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Clear user session (logout)
     */
    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}
