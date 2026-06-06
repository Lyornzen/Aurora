package com.aurora.app.data

import android.content.Context
import android.content.SharedPreferences

object UserProfile {
    private const val PREFS_NAME = "user_profile"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_FIRST_LAUNCH = "first_launch"

    private var prefs: SharedPreferences? = null
    var nickname: String = ""
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        nickname = prefs?.getString(KEY_NICKNAME, "") ?: ""
    }

    fun isFirstLaunch(): Boolean {
        return prefs?.getBoolean(KEY_FIRST_LAUNCH, true) ?: true
    }

    fun setNickname(name: String) {
        nickname = name.trim()
        prefs?.edit()?.putString(KEY_NICKNAME, nickname)?.apply()
        prefs?.edit()?.putBoolean(KEY_FIRST_LAUNCH, false)?.apply()
    }
}
