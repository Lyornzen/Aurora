package com.aurora.ai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.ai.theme.AuroraDp
import com.aurora.ai.theme.AuroraLightColors

// ============================================================
// Aurora Card
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuroraCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = AuroraDp.dp24,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            onClick = onClick,
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            content = content,
        )
    }
}

// ============================================================
// Greeting Card (Chat screen top)
// ============================================================

@Composable
fun AuroraGreetingCard(
    userName: String,
    greeting: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AuroraDp.dp180)
            .clip(RoundedCornerShape(AuroraDp.dp28))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        AuroraLightColors.GradientStart,
                        AuroraLightColors.GradientEnd,
                    ),
                ),
            )
            .padding(AuroraDp.dp24),
    ) {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Normal,
            )
            Spacer(modifier = Modifier.height(AuroraDp.dp4))
            Text(
                text = "Hello, $userName",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }

        // Settings icon
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(AuroraDp.dp40)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(AuroraDp.dp22),
            )
        }
    }
}

// ============================================================
// Model Selector Card
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuroraModelSelectorCard(
    modelName: String,
    modelDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(AuroraDp.dp20),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = AuroraDp.dp16, top = 8.dp, end = AuroraDp.dp16, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = modelName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(AuroraDp.dp4))
                Text(
                    text = modelDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.width(AuroraDp.dp12))
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Select model",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(AuroraDp.dp20),
            )
        }
    }
}

// ============================================================
// Progress Bar
// ============================================================

@Composable
fun AuroraProgressBar(
    progress: Float, // 0f to 1f
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = AuroraDp.dp6,
    cornerRadius: Dp = AuroraDp.dp3,
    modifier: Modifier = Modifier,
) {
    val animatedColor by animateColorAsState(progressColor, label = "progressColor")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(cornerRadius))
                .background(animatedColor),
        )
    }
}

// ============================================================
// Status Badge
// ============================================================

@Composable
fun AuroraStatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val animatedColor by animateColorAsState(color, label = "statusColor")
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AuroraDp.dp12))
            .background(animatedColor.copy(alpha = 0.12f))
            .padding(horizontal = AuroraDp.dp10, vertical = AuroraDp.dp4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AuroraDp.dp6),
    ) {
        Icon(
            imageVector = Icons.Filled.Circle,
            contentDescription = null,
            modifier = Modifier.size(AuroraDp.dp8),
            tint = animatedColor,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = animatedColor,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ============================================================
// Aurora TopAppBar
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuroraTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
}

// ============================================================
// Section Header
// ============================================================

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AuroraDp.dp4)
            .padding(vertical = AuroraDp.dp8),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
        )
        action?.invoke()
    }
}
