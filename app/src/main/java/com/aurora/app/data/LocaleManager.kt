package com.aurora.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleManager {
    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "app_language"
    
    private var prefs: SharedPreferences? = null
    private var currentLocale: Locale = Locale.ENGLISH
    
    val supportedLanguages = listOf(
        "en" to "English",
        "zh" to "中文",
    )
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val langTag = prefs?.getString(KEY_LANGUAGE, "en") ?: "en"
        currentLocale = Locale.forLanguageTag(langTag)
        applyLocale()
    }

    private fun applyLocale() {
        val localeList = LocaleListCompat.create(currentLocale)
        AppCompatDelegate.setApplicationLocales(localeList)
    }
    
    fun getLanguageTag(): String = currentLocale.toLanguageTag()
    fun getLanguageName(): String {
        return supportedLanguages.find { it.first == currentLocale.language }?.second ?: "English"
    }
    
    fun setLanguage(context: Context, languageTag: String): Boolean {
        val newLocale = Locale.forLanguageTag(languageTag)
        if (newLocale == currentLocale) return false
        currentLocale = newLocale
        prefs?.edit()?.putString(KEY_LANGUAGE, languageTag)?.apply()
        applyLocale()
        return true
    }
}
