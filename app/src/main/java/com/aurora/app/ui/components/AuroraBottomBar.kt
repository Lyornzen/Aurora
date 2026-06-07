package com.aurora.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.DeviceHub
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.aurora.app.R

enum class AuroraTab(
    val label: String,
    @StringRes val labelResId: Int,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
) {
    Chat("Chat", R.string.tab_chat, Icons.Rounded.Forum, Icons.Outlined.Forum),
    Tasks("Tasks", R.string.tab_tasks, Icons.AutoMirrored.Rounded.Assignment, Icons.AutoMirrored.Outlined.Assignment),
    Links("Links", R.string.tab_links, Icons.Rounded.DeviceHub, Icons.Rounded.DeviceHub),
    History("History", R.string.tab_history, Icons.Rounded.History, Icons.Rounded.History),
    Profile("Profile", R.string.tab_profile, Icons.Rounded.Person, Icons.Outlined.Person),
}

@Composable
fun AuroraBottomBar(
    selectedTab: AuroraTab,
    onTabSelected: (AuroraTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val bottomBarBg = colorScheme.surfaceContainerHigh
    val indicatorColor = Color.White

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bottomBarBg,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AuroraTab.entries.forEach { tab ->
                    TabItem(
                        tab = tab,
                        isSelected = tab == selectedTab,
                        indicatorColor = indicatorColor,
                        colorScheme = colorScheme,
                        onClick = { onTabSelected(tab) },
                    )
                }
            }
            // Home indicator bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .width(134.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colorScheme.onSurface.copy(alpha = 0.6f)),
                )
            }
        }
    }
}
@Composable
private fun RowScope.TabItem(
    tab: AuroraTab,
    indicatorColor: Color,
    colorScheme: androidx.compose.material3.ColorScheme,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
        label = "tabColor",
    )
    val icon = if (isSelected) tab.filledIcon else tab.outlinedIcon

    // Fixed-size click area matching the visual indicator, centered within the weight space
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        // Wrapper to constrain clickable area to match visual indicator
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(indicatorColor),
                )
            }
            // Icon + Label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tab.label,
                    tint = if (isSelected) colorScheme.primary else textColor,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = stringResource(tab.labelResId),
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight(600) else FontWeight(500),
                    color = if (isSelected) colorScheme.primary else textColor,
                )
            }
        }
    }
}

