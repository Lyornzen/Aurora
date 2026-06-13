package com.aurora.app.ui.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aurora.app.ui.components.AuroraBottomBar
import com.aurora.app.ui.components.AuroraTab
import com.aurora.app.data.ConversationStore
import com.aurora.app.data.ChatSession
import com.aurora.app.ui.screens.ChatScreen
import com.aurora.app.ui.screens.HistoryScreen
import com.aurora.app.ui.screens.LinksScreen
import com.aurora.app.ui.screens.ProfileScreen
import com.aurora.app.ui.screens.TasksScreen
import com.aurora.app.ui.theme.AppMotion

object AuroraRoutes {
    const val CHAT = "chat"
    const val TASKS = "tasks"
    const val LINKS = "links"
    const val HISTORY = "history"
    const val PROFILE = "profile"
}

/** Tab order index — determines slide direction. */
private val ROUTE_INDEX = mapOf(
    AuroraRoutes.CHAT to 0,
    AuroraRoutes.TASKS to 1,
    AuroraRoutes.LINKS to 2,
    AuroraRoutes.HISTORY to 3,
    AuroraRoutes.PROFILE to 4,
)

private val SlideEasing = CubicBezierEasing(0.25f, 0.10f, 0.25f, 1.0f)
private const val SLIDE_MS = 220
private const val FADE_MS = 80

@Composable
fun AuroraNavHost(
    modifier: Modifier = Modifier,
    darkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {},
    onConsumeSharedText: () -> String? = { null },
) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(AuroraTab.Chat) }
    var scrollToTopTrigger by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.systemBars
            .union(WindowInsets.navigationBars)
            .union(WindowInsets.ime),
        bottomBar = {
            AuroraBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab == AuroraTab.Chat && selectedTab == AuroraTab.Chat) {
                        scrollToTopTrigger++
                    } else {
                        selectedTab = tab
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AuroraRoutes.CHAT,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                val fromIdx = ROUTE_INDEX[initialState.destination.route] ?: 0
                val toIdx = ROUTE_INDEX[targetState.destination.route] ?: 0
                val goingRight = toIdx >= fromIdx
                slideInHorizontally(
                    initialOffsetX = { if (goingRight) it else -it },
                    animationSpec = tween(durationMillis = SLIDE_MS, easing = SlideEasing)
                ) + fadeIn(tween(durationMillis = FADE_MS, easing = LinearEasing), initialAlpha = 0f)
            },
            exitTransition = {
                val fromIdx = ROUTE_INDEX[initialState.destination.route] ?: 0
                val toIdx = ROUTE_INDEX[targetState.destination.route] ?: 0
                val goingRight = toIdx >= fromIdx
                slideOutHorizontally(
                    targetOffsetX = { if (goingRight) -it / 3 else it / 3 },
                    animationSpec = tween(durationMillis = SLIDE_MS, easing = SlideEasing)
                ) + fadeOut(tween(durationMillis = FADE_MS, easing = LinearEasing), targetAlpha = 0f)
            },
            popEnterTransition = {
                val fromIdx = ROUTE_INDEX[initialState.destination.route] ?: 0
                val toIdx = ROUTE_INDEX[targetState.destination.route] ?: 0
                val goingRight = toIdx >= fromIdx
                slideInHorizontally(
                    initialOffsetX = { if (goingRight) it / 3 else -it / 3 },
                    animationSpec = tween(durationMillis = SLIDE_MS, easing = SlideEasing)
                ) + fadeIn(tween(durationMillis = FADE_MS, easing = LinearEasing), initialAlpha = 0f)
            },
            popExitTransition = {
                val fromIdx = ROUTE_INDEX[initialState.destination.route] ?: 0
                val toIdx = ROUTE_INDEX[targetState.destination.route] ?: 0
                val goingRight = toIdx >= fromIdx
                slideOutHorizontally(
                    targetOffsetX = { if (goingRight) -it else it },
                    animationSpec = tween(durationMillis = SLIDE_MS, easing = SlideEasing)
                ) + fadeOut(tween(durationMillis = FADE_MS, easing = LinearEasing), targetAlpha = 0f)
            },
        ) {
            composable(AuroraRoutes.CHAT) {
                ChatScreen(
                    scrollToTopTrigger = scrollToTopTrigger,
                    onConsumeSharedText = onConsumeSharedText,
                )
            }
            composable(AuroraRoutes.TASKS) { TasksScreen() }
            composable(AuroraRoutes.LINKS) { LinksScreen() }
            composable(AuroraRoutes.HISTORY) {
                HistoryScreen(
                    onLoadConversation = { id, model ->
                        val msgs = ConversationStore.loadMessages(id)
                        if (msgs.isNotEmpty()) {
                            ChatSession.loadConversation(id, msgs, model)
                        }
                        selectedTab = AuroraTab.Chat
                        navController.navigate(AuroraRoutes.CHAT) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(AuroraRoutes.PROFILE) {
                ProfileScreen(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange,
                )
            }
        }
    }
}

private val AuroraTab.route: String
    get() = when (this) {
        AuroraTab.Chat -> AuroraRoutes.CHAT
        AuroraTab.Tasks -> AuroraRoutes.TASKS
        AuroraTab.Links -> AuroraRoutes.LINKS
        AuroraTab.History -> AuroraRoutes.HISTORY
        AuroraTab.Profile -> AuroraRoutes.PROFILE
    }
