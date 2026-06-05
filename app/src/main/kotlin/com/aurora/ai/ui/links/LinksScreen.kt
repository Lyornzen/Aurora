package com.aurora.ai.ui.links

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.aurora.ai.theme.AuroraDp
import com.aurora.ai.ui.components.AuroraCard
import com.aurora.ai.ui.components.AuroraTopBar

// ============================================================
// Data Models
// ============================================================

data class LinkedDevice(
    val id: String,
    val name: String,
    val isOnline: Boolean,
    val localIp: String,
    val cpuUsage: Int = 45,
    val ramUsage: Int = 62,
    val gpuUsage: Int = 30,
    val networkSpeed: String = "1.2 MB/s",
)

data class RemoteAction(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
)

val sampleDevices = listOf(
    LinkedDevice(
        id = "d1",
        name = "My Desktop",
        isOnline = true,
        localIp = "192.168.1.101",
        cpuUsage = 45,
        ramUsage = 62,
        gpuUsage = 30,
        networkSpeed = "1.2 MB/s",
    ),
    LinkedDevice(
        id = "d2",
        name = "Workstation",
        isOnline = true,
        localIp = "192.168.1.102",
        cpuUsage = 78,
        ramUsage = 85,
        gpuUsage = 92,
        networkSpeed = "8.5 MB/s",
    ),
    LinkedDevice(
        id = "d3",
        name = "Home Server",
        isOnline = false,
        localIp = "192.168.1.200",
    ),
)

val remoteActions = listOf(
    RemoteAction("execute", "Execute Script", Icons.Filled.Terminal, Color(0xFF5B8FF9)),
    RemoteAction("browser", "Open Browser", Icons.Filled.OpenInBrowser, Color(0xFF5AD8A6)),
    RemoteAction("agent", "Run Agent", Icons.Filled.SmartToy, Color(0xFF7C4DFF)),
        RemoteAction("transfer", "File Transfer", Icons.AutoMirrored.Filled.DriveFileMove, Color(0xFFFF9800)),
    RemoteAction("terminal", "Terminal", Icons.Filled.Terminal, Color(0xFF607D8B)),
    RemoteAction("cloud", "Cloud Sync", Icons.Filled.Cloud, Color(0xFF2196F3)),
)

// ============================================================
// Links Screen
// ============================================================

@Composable
fun LinksScreen(
    modifier: Modifier = Modifier,
    onDeviceClick: (LinkedDevice) -> Unit = {},
    onBack: (() -> Unit)? = null,
    selectedDevice: LinkedDevice? = null,
    bottomPadding: Dp = AuroraDp.dp80,
) {
    if (selectedDevice != null) {
        DeviceDetailScreen(
            device = selectedDevice,
            onBack = onBack ?: {},
            bottomPadding = bottomPadding,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = AuroraDp.dp8),
    ) {
        AuroraTopBar(
            title = "Links",
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More")
                }
            },
        )

        Spacer(modifier = Modifier.height(AuroraDp.dp8))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AuroraDp.dp16)
                .padding(bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(AuroraDp.dp12),
        ) {
            emptyList<LinkedDevice>().forEach { device ->
                DeviceCard(
                    device = device,
                    onClick = { onDeviceClick(device) },
                )
            }
        }
    }
}

// ============================================================
// Device Card
// ============================================================

@Composable
fun DeviceCard(
    device: LinkedDevice,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    AuroraCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        cornerRadius = AuroraDp.dp24,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AuroraDp.dp16),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Device icon
            Box(
                modifier = Modifier
                    .size(AuroraDp.dp48)
                    .clip(RoundedCornerShape(AuroraDp.dp14))
                    .background(
                        if (device.isOnline)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.DesktopWindows,
                    contentDescription = null,
                    modifier = Modifier.size(AuroraDp.dp24),
                    tint = if (device.isOnline)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(AuroraDp.dp14))

            // Device info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AuroraDp.dp8),
                ) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // Online indicator dot
                    Box(
                        modifier = Modifier
                            .size(AuroraDp.dp8)
                            .clip(RoundedCornerShape(AuroraDp.dp4))
                            .background(
                                if (device.isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                            ),
                    )
                }

                Spacer(modifier = Modifier.height(AuroraDp.dp2))

                Text(
                    text = if (device.isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (device.isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                )

                Spacer(modifier = Modifier.height(AuroraDp.dp2))

                Text(
                    text = device.localIp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(AuroraDp.dp20),
            )
        }
    }
}

// ============================================================
// Device Detail Screen
// ============================================================

@Composable
fun DeviceDetailScreen(
    device: LinkedDevice,
    onBack: () -> Unit,
    bottomPadding: Dp = AuroraDp.dp80,
) {
    Scaffold(
        topBar = {
            AuroraTopBar(
                title = device.name,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = AuroraDp.dp16)
                .padding(bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(AuroraDp.dp16),
        ) {
            // Connection status
            AuroraCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = AuroraDp.dp24,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AuroraDp.dp16),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(AuroraDp.dp56)
                            .clip(RoundedCornerShape(AuroraDp.dp16))
                            .background(
                                if (device.isOnline)
                                    Color(0xFF4CAF50).copy(alpha = 0.12f)
                                else
                                    Color(0xFF9E9E9E).copy(alpha = 0.12f),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DesktopWindows,
                            contentDescription = null,
                            tint = if (device.isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                            modifier = Modifier.size(AuroraDp.dp28),
                        )
                    }
                    Spacer(modifier = Modifier.width(AuroraDp.dp14))
                    Column {
                        Text(
                            text = if (device.isOnline) "Connected" else "Disconnected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = device.localIp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // System resources
            Text(
                text = "System Resources",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            ResourceCard(
                label = "CPU",
                usage = device.cpuUsage,
                color = Color(0xFF5B8FF9),
                icon = Icons.Filled.Computer,
            )
            ResourceCard(
                label = "RAM",
                usage = device.ramUsage,
                color = Color(0xFF5AD8A6),
                icon = Icons.Filled.Computer,
            )
            ResourceCard(
                label = "GPU",
                usage = device.gpuUsage,
                color = Color(0xFFF9A05B),
                icon = Icons.Filled.Computer,
            )
            ResourceCard(
                label = "Network",
                usage = 0,
                detail = device.networkSpeed,
                color = Color(0xFF7C4DFF),
                icon = Icons.Filled.Link,
            )

            Spacer(modifier = Modifier.height(AuroraDp.dp8))

            // Remote Actions
            Text(
                text = "Remote Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(AuroraDp.dp12),
                verticalArrangement = Arrangement.spacedBy(AuroraDp.dp12),
            ) {
                items(remoteActions) { action ->
                    RemoteActionCard(action = action)
                }
            }
        }
    }
}

@Composable
private fun ResourceCard(
    label: String,
    usage: Int,
    color: Color,
    icon: ImageVector,
    detail: String? = null,
) {
    AuroraCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = AuroraDp.dp20,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AuroraDp.dp14),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(AuroraDp.dp20),
            )
            Spacer(modifier = Modifier.width(AuroraDp.dp12))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = detail ?: "${usage}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(AuroraDp.dp6))
                LinearProgressIndicator(
                    progress = { usage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AuroraDp.dp4)
                        .clip(RoundedCornerShape(AuroraDp.dp2)),
                    color = color,
                    trackColor = color.copy(alpha = 0.12f),
                )
            }
        }
    }
}

@Composable
private fun RemoteActionCard(
    action: RemoteAction,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AuroraDp.dp20)),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = { },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AuroraDp.dp16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(AuroraDp.dp44)
                    .clip(RoundedCornerShape(AuroraDp.dp14))
                    .background(action.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = action.color,
                    modifier = Modifier.size(AuroraDp.dp22),
                )
            }
            Spacer(modifier = Modifier.height(AuroraDp.dp8))
            Text(
                text = action.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
