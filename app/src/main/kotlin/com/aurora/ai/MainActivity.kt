package com.aurora.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aurora.ai.theme.AuroraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var themeIndex by rememberSaveable { mutableIntStateOf(0) }
            var languageIndex by rememberSaveable { mutableIntStateOf(0) }

            val resolvedDark = when (themeIndex) {
                0 -> false
                1 -> true
                else -> systemDark
            }

            AuroraTheme(darkTheme = resolvedDark) {
                CompositionLocalProvider(LocalI18n provides stringsFor(languageIndex)) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                        AuroraMainScreen(
                            themeIndex = themeIndex,
                            onThemeChange = { themeIndex = it },
                            languageIndex = languageIndex,
                            onLanguageChange = { languageIndex = it },
                        )
                    }
                }
            }
        }
    }
}
