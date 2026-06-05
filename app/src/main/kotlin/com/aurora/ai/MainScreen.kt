package com.aurora.ai

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aurora.ai.navigation.AuroraBottomNavBar
import com.aurora.ai.theme.AuroraDp
import com.aurora.ai.ui.chat.ChatScreen
import com.aurora.ai.ui.history.HistoryScreen
import com.aurora.ai.ui.links.LinkedDevice
import com.aurora.ai.ui.links.LinksScreen
import com.aurora.ai.ui.profile.ProfileScreen
import com.aurora.ai.ui.tasks.AuroraTask
import com.aurora.ai.ui.tasks.TaskDetailScreen
import com.aurora.ai.ui.tasks.TasksScreen
import kotlinx.coroutines.launch

@Composable
fun AuroraMainScreen(
    themeIndex: Int = 0,
    onThemeChange: (Int) -> Unit = {},
    languageIndex: Int = 0,
    onLanguageChange: (Int) -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    var selectedTask by remember { mutableStateOf<AuroraTask?>(null) }
    var selectedDevice by remember { mutableStateOf<LinkedDevice?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            AuroraBottomNavBar(
                currentIndex = pagerState.currentPage,
                onItemSelected = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                visible = true,
            )
        },
    ) { innerPadding ->
        val task = selectedTask
        if (task != null) {
            TaskDetailScreen(task = task, onBack = { selectedTask = null }, modifier = Modifier.fillMaxSize())
        } else {
            val device = selectedDevice
            if (device != null) {
                LinksScreen(selectedDevice = device, onDeviceClick = { selectedDevice = it }, onBack = { selectedDevice = null },
                    modifier = Modifier.fillMaxSize(), bottomPadding = AuroraDp.dp80)
            } else {
                Box(Modifier.fillMaxSize().padding(innerPadding)) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), userScrollEnabled = true) { page ->
                        when (page) {
                            0 -> ChatScreen(bottomPadding = AuroraDp.dp0)
                            1 -> TasksScreen(onTaskClick = { t -> selectedTask = t }, bottomPadding = AuroraDp.dp0)
                            2 -> LinksScreen(onDeviceClick = { d -> selectedDevice = d }, bottomPadding = AuroraDp.dp0)
                            3 -> HistoryScreen(bottomPadding = AuroraDp.dp0)
                            4 -> ProfileScreen(
                                bottomPadding = AuroraDp.dp0,
                                themeIndex = themeIndex,
                                onThemeChange = onThemeChange,
                                languageIndex = languageIndex,
                                onLanguageChange = onLanguageChange,
                            )
                        }
                    }
                }
            }
        }
    }
}
