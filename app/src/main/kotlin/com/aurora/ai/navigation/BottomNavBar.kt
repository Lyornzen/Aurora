package com.aurora.ai.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.ai.LocalI18n
import com.aurora.ai.theme.AuroraDp
import com.aurora.ai.theme.AuroraMotion

/**
 * Aurora AI Bottom Navigation Bar
 * Material Design 3 style with dynamic color,
 * soft shadows, and smooth transitions.
 * Height: 80dp; Background: SurfaceContainer.
 */
enum class AuroraNavItem(
    val labelKey: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Chat("navChat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    Tasks("navTasks", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt),
    Links("navLinks", Icons.Filled.Link, Icons.Outlined.Link),
    History("navHistory", Icons.Filled.History, Icons.Outlined.History),
    Profile("navProfile", Icons.Filled.Person, Icons.Outlined.Person),
}

object AuroraNavBarDefaults {
    val Height = AuroraDp.dp80
    val IconSize = AuroraDp.dp24
    val LabelFontSize = 12.sp
    val IconLabelSpacing = AuroraDp.dp4
    val CornerRadius = AuroraDp.dp24
    val HorizontalPadding = AuroraDp.dp8
    val TopPadding = AuroraDp.dp8
}

@Composable
fun AuroraBottomNavBar(
    currentIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    val i18n = LocalI18n.current
    val labels = listOf(i18n.navChat, i18n.navTasks, i18n.navLinks, i18n.navHistory, i18n.navProfile)
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(AuroraNavBarDefaults.Height),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = AuroraDp.dp2,
            shadowElevation = AuroraDp.dp4,
            shape = RoundedCornerShape(
                topStart = AuroraNavBarDefaults.CornerRadius,
                topEnd = AuroraNavBarDefaults.CornerRadius,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = AuroraNavBarDefaults.HorizontalPadding)
                    .padding(top = AuroraNavBarDefaults.TopPadding)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AuroraNavItem.entries.forEachIndexed { index, item ->
                    val selected = currentIndex == index
                    NavBarItemContent(item, labels[index], selected) { onItemSelected(index) }
                }
            }
        }
    }
}

@Composable
private fun NavBarItemContent(
    item: AuroraNavItem,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val iconColor by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, label = "iconColor")
    val textColor by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, label = "textColor")
    Column(
        modifier = Modifier.selectable(selected = selected, onClick = onClick, role = Role.Tab).padding(horizontal = AuroraDp.dp4),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (selected) Box(Modifier.size(width = AuroraDp.dp44, height = AuroraDp.dp32).clip(RoundedCornerShape(AuroraDp.dp16)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)))
            Icon(if (selected) item.selectedIcon else item.unselectedIcon, label, Modifier.size(AuroraNavBarDefaults.IconSize), tint = iconColor)
        }
        Spacer(Modifier.height(AuroraNavBarDefaults.IconLabelSpacing))
        Text(label, color = textColor, fontSize = AuroraNavBarDefaults.LabelFontSize, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}
