package com.aurora.app.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.aurora.app.R
import com.aurora.app.ui.theme.Warning
import com.aurora.app.ui.theme.WarningContainer
import com.aurora.app.data.ConversationStore
import com.aurora.app.data.ConversationHistory

private data class Conversation(
    val id: String, val title: String, val model: String,
    val modelColor: Color, val lastMessage: String, val timestamp: String,
    val group: String, val messageCount: Int, val pinned: Boolean = false,
)

private val GROUPS = listOf("Today", "Yesterday", "This Week", "Older")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onLoadConversation: (id: String, model: String) -> Unit = { _, _ -> },
) {
    val colorScheme = MaterialTheme.colorScheme
    var search by remember { mutableStateOf("") }

    // Get conversations from store
    // Pinned conversations first, then sorted by time (newest first)
    val storedConversations = ConversationStore.conversations.sortedWith(
        compareByDescending<ConversationHistory> { it.timestamp < 0 } // pinned first
            .thenByDescending { kotlin.math.abs(it.timestamp) }
    )
    val conversations = storedConversations.map { conv ->
        val timeDiff = System.currentTimeMillis() - kotlin.math.abs(conv.timestamp)
        val daysDiff = timeDiff / (1000 * 60 * 60 * 24)
        val group = when {
            daysDiff < 1 -> "Today"
            daysDiff < 2 -> "Yesterday"
            daysDiff < 7 -> "This Week"
            else -> "Older"
        }
        val timestamp = when {
            timeDiff < 60000 -> "Just now"
            timeDiff < 3600000 -> "${timeDiff / 60000}m ago"
            timeDiff < 86400000 -> "${timeDiff / 3600000}h ago"
            else -> "${daysDiff}d ago"
        }
        Conversation(
            id = conv.id,
            title = conv.title,
            model = conv.model,
            modelColor = colorScheme.primary,
            lastMessage = conv.lastMessage,
            timestamp = timestamp,
            group = group,
            messageCount = conv.messageCount,
            pinned = conv.timestamp < 0,
        )
    }

    val filtered = if (search.isBlank()) null
    else conversations.filter {
        it.title.contains(search, ignoreCase = true) ||
        it.lastMessage.contains(search, ignoreCase = true) ||
        it.model.contains(search, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp, vertical = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(stringResource(R.string.history_title), style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            Text("${conversations.size} ${stringResource(R.string.history_title)}",
                style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp))
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text(stringResource(R.string.history_search), color = colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(28.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, null, tint = colorScheme.onSurfaceVariant) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = colorScheme.surfaceVariant,
                    unfocusedContainerColor = colorScheme.surfaceVariant,
                ),
            )
        }
        if (filtered != null) {
            items(filtered) { conv ->
                ConversationCard(conv = conv, colorScheme = colorScheme,
                    onLoadConversation = onLoadConversation)
            }
            if (filtered.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.AutoMirrored.Outlined.Chat, null,
                                tint = colorScheme.outlineVariant, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.history_no_results), fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            GROUPS.forEach { group ->
                val items = conversations.filter { it.group == group }
                if (items.isNotEmpty()) {
                    item {
                        Text(group.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp))
                    }
                    items(items) { conv ->
                        ConversationCard(conv = conv, colorScheme = colorScheme,
                            onLoadConversation = onLoadConversation)
                    }
                }
            }
            if (conversations.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.AutoMirrored.Outlined.Chat, null,
                                tint = colorScheme.outlineVariant, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.history_empty), fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant)
                            Text(stringResource(R.string.history_empty_hint), fontSize = 12.sp,
                                color = colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationCard(
    conv: Conversation,
    colorScheme: androidx.compose.material3.ColorScheme,
    onLoadConversation: (id: String, model: String) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf(conv.title) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            onLoadConversation(conv.id, conv.model)
        },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                .background(conv.modelColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = conv.modelColor,
                    modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(conv.title, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, color = colorScheme.onSurface,
                        modifier = Modifier.weight(1f), maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(conv.timestamp, fontSize = 10.sp,
                            color = colorScheme.onSurfaceVariant)
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Outlined.MoreVert, null,
                                    tint = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(if (conv.pinned) stringResource(R.string.menu_unpin) else stringResource(R.string.menu_pin)) },
                                    onClick = {
                                        ConversationStore.togglePin(conv.id)
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.PushPin, null)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_rename)) },
                                    onClick = {
                                        renameText = conv.title
                                        showRenameDialog = true
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Edit, null)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.btn_delete), color = colorScheme.error) },
                                    onClick = {
                                        showDeleteDialog = true
                                        showMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Delete, null,
                                            tint = colorScheme.error)
                                    },
                                )
                            }
                        }
                    }
                }
                Text(conv.lastMessage, fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(7.dp))
                            .background(conv.modelColor.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.SmartToy, null, tint = conv.modelColor,
                            modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(conv.model, fontSize = 10.sp, fontWeight = FontWeight.Medium,
                            color = conv.modelColor)
                    }
                    Text("${conv.messageCount} messages", fontSize = 10.sp,
                        color = colorScheme.outlineVariant)
                    if (conv.pinned) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                            .background(WarningContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(stringResource(R.string.label_pinned), fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold, color = Warning)
                        }
                    }
                }
            }
        }
    }

    // Rename dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(R.string.rename_title), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameText.isNotBlank()) {
                        ConversationStore.renameConversation(conv.id, renameText.trim())
                    }
                    showRenameDialog = false
                }) { Text(stringResource(R.string.btn_ok), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    ConversationStore.deleteConversation(conv.id)
                    showDeleteDialog = false
                }) {
                    Text(stringResource(R.string.btn_delete), color = colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
        )
    }
}
