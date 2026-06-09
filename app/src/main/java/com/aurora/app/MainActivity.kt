package com.aurora.app

import android.content.Context
import android.content.Intent
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
import com.aurora.app.data.ChatSession
import com.aurora.app.data.ConversationStore
import com.aurora.app.data.UserProfile

class MainActivity : ComponentActivity() {

    // Holds text shared from other apps, consumed by ChatScreen on first composition
    var pendingSharedText: String? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ConversationStore.init(this)
        ApiService.init(this)
        UserProfile.init(this)

        handleShareIntent(intent)

        val prefs = getSharedPreferences("aurora_prefs", Context.MODE_PRIVATE)
        val savedDarkMode = prefs.getBoolean("dark_mode", false)

        setContent {
            var darkMode by remember { mutableStateOf(savedDarkMode) }

            AuroraTheme(darkTheme = darkMode) {
                AuroraNavHost(
                    darkMode = darkMode,
                    onDarkModeChange = {
                        darkMode = it
                        prefs.edit().putBoolean("dark_mode", it).apply()
                    },
                    onConsumeSharedText = {
                        val text = pendingSharedText
                        pendingSharedText = null
                        text
                    },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                pendingSharedText = text
            }
        }
    }

    override fun onStop() {
        super.onStop()
        ChatSession.ensureSaved()
    }
}
