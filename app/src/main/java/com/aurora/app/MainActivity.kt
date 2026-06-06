package com.aurora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.aurora.app.ui.navigation.AuroraNavHost
import com.aurora.app.ui.theme.AuroraTheme
import com.aurora.app.data.ApiService
import com.aurora.app.data.ConversationStore
import com.aurora.app.data.UserProfile
import com.aurora.app.ui.theme.Primary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ConversationStore.init(this)
        ApiService.init(this)
        UserProfile.init(this)
        setContent {
            var darkMode by remember { mutableStateOf(false) }

            AuroraTheme(
                darkTheme = darkMode,
                accentColor = Primary,
            ) {
                AuroraNavHost(
                    darkMode = darkMode,
                    onDarkModeChange = { darkMode = it },
                )
            }
        }
    }
}
