package com.aurora.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.automirrored.outlined.Launch
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Tablet
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurora.app.ui.theme.Success
import com.aurora.app.ui.theme.SuccessContainer

// ─── Data Models ────────────────────────────────────────────────

private enum class DeviceType { Laptop, Desktop, Tablet }
private data class Device(
    val id: String, val name: String, val type: DeviceType,
    val ip: String, val latency: Int?, val lastSync: String,
    val connected: Boolean, val os: String, val version: String,
)
private data class QuickAction(val icon: ImageVector, val label: String, val bg: Color, val fg: Color)

// TODO: Replace with actual device discovery data source
private val DEVICES = emptyList<Device>()

// ─── LinksScreen ────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinksScreen(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    var syncing by remember { mutableStateOf(false) }
    val connectedCount = DEVICES.count { it.connected }
    val infiniteTransition = rememberInfiniteTransition(label = "syncSpin")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation",
    )

    val quickActions = listOf(
        QuickAction(Icons.Outlined.Terminal, "Open Terminal", colorScheme.primaryContainer, colorScheme.primary),
        QuickAction(Icons.Outlined.FolderOpen, "Browse Files", colorScheme.primaryContainer, colorScheme.primary),
        QuickAction(Icons.Outlined.CloudSync, "Force Sync", colorScheme.secondaryContainer, colorScheme.secondary),
        QuickAction(Icons.AutoMirrored.Outlined.Launch, "Remote View", colorScheme.primaryContainer, colorScheme.primary),
    )

    LazyColumn(
        modifier = modifier.fillMaxSize().background(colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp, vertical = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Links", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text("$connectedCount of ${DEVICES.size} devices connected",
                        style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(
                        onClick = { syncing = true },
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colorScheme.surfaceVariant),
                    ) {
                        Icon(Icons.Outlined.Refresh, "Sync",
                            tint = colorScheme.primary, modifier = Modifier.size(20.dp)
                                .then(if (syncing) Modifier.rotate(rotation) else Modifier))
                    }
                    // TODO: Implement sync/refresh
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colorScheme.primary),
                    ) {
                        Icon(Icons.Outlined.Add, "Add Device", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        // Connection status hero
        item {
            val isConnected = connectedCount > 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(36.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) colorScheme.primaryContainer else colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                            .background(if (isConnected) colorScheme.primary else colorScheme.primary),
                            contentAlignment = Alignment.Center) {
                            Icon(
                                if (isConnected) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                                null, tint = Color.White, modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isConnected) "Network Active" else "No Connection",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isConnected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                if (isConnected) "$connectedCount device${if (connectedCount > 1) "s" else ""} on local network"
                                else "Check your network settings",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isConnected) colorScheme.primary else colorScheme.primary,
                            )
                        }
                        if (syncing) {
                            LinearProgressIndicator(
                                modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(999.dp)),
                                color = colorScheme.primary, trackColor = MaterialTheme.colorScheme.primaryContainer,
                            )
                        }
                    }
                    if (isConnected) {
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("4ms" to "Latency", "Just now" to "Last sync", "LAN" to "Network").forEach { (valStr, label) ->
                                Box(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.2f)).padding(8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(valStr, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Text(label, fontSize = 10.sp, color = colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // Quick Actions
        item {
            Column {
                Text("QUICK ACTIONS", fontSize = 10.sp, fontWeight = FontWeight.W600,
                    color = colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickActions.forEach { action ->
                        // TODO: Implement quick action handlers
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {},
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                    .background(action.bg), contentAlignment = Alignment.Center) {
                                    Icon(action.icon, null, tint = action.fg, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(action.label, fontSize = 13.sp, fontWeight = FontWeight.W500,
                                    color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
        // Devices header
        item {
            Text("DEVICES", fontSize = 10.sp, fontWeight = FontWeight.W600,
                color = colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp))
        }
        // Device list
        items(DEVICES) { device -> DeviceCard(device = device) }
        // Add device CTA
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
                    .border(2.dp, colorScheme.outlineVariant, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                // TODO: Implement device addition (QR code scan / IP input)
                Row(
                    modifier = Modifier.clickable {}.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.QrCode, null, tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Add New Device", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.W600,
                            color = colorScheme.onSurfaceVariant)
                        Text("Scan QR code or enter IP address",
                            fontSize = 11.sp, color = colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(device: Device) {
    val colorScheme = MaterialTheme.colorScheme
    val deviceIcon = when (device.type) {
        DeviceType.Laptop -> Icons.Rounded.Laptop
        DeviceType.Desktop -> Icons.Rounded.Computer
        DeviceType.Tablet -> Icons.Rounded.Tablet
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = if (device.connected) 1f else 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                .background(if (device.connected) colorScheme.primaryContainer else colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center) {
                Icon(deviceIcon, null,
                    tint = if (device.connected) colorScheme.primary else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(device.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.W600,
                        modifier = Modifier.weight(1f))
                    // TODO: Implement device context menu
                    IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.MoreVert, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
                Text("${device.ip} · ${device.os}", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                        .background(if (device.connected) SuccessContainer else colorScheme.errorContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(if (device.connected) "Connected" else "Offline",
                            fontSize = 10.sp, fontWeight = FontWeight.W600,
                            color = if (device.connected) Success else colorScheme.error)
                    }
                    Text(device.version, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
