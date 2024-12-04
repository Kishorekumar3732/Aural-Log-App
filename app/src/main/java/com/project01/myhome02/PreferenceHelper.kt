package com.project01.myhome02

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "app_prefs"
private const val KEY_IS_DARK_MODE = "is_dark_mode"
private const val KEY_USE_SYSTEM_SETTINGS = "use_system_settings"

interface IPreferencesHelper {
    var isDarkMode: Boolean
    var useSystemSettings: Boolean
}

class PreferencesHelper(context: Context) : IPreferencesHelper {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_IS_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_DARK_MODE, value).apply()

    override var useSystemSettings: Boolean
        get() = prefs.getBoolean(KEY_USE_SYSTEM_SETTINGS, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_SYSTEM_SETTINGS, value).apply()
}

class MockPreferencesHelper : IPreferencesHelper {
    override var isDarkMode: Boolean = false
    override var useSystemSettings: Boolean = true
}
