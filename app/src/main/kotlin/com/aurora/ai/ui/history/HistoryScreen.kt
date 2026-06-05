package com.aurora.ai.ui.history

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.aurora.ai.theme.AuroraDp
import com.aurora.ai.ui.components.AuroraCard
import com.aurora.ai.ui.components.AuroraTopBar
import com.aurora.ai.ui.components.SectionHeader

// ============================================================
// Data Models
// ============================================================

data class HistoryItem(
    val id: String,
    val title: String,
    val type: HistoryType,
    val timestamp: String,
    val preview: String = "",
    val isFavorite: Boolean = false,
)

enum class HistoryType(val label: String, val icon: ImageVector, val color: Color) {
    Chat("Chat", Icons.Filled.ChatBubble, Color(0xFF7C4DFF)),
    Task("Task", Icons.Filled.TaskAlt, Color(0xFF4CAF50)),
    ImageAnalysis("Image", Icons.Filled.Image, Color(0xFFF9A05B)),
}

enum class HistoryGroup(val label: String) {
    Today("Today"),
    Yesterday("Yesterday"),
    ThisWeek("This Week"),
    Earlier("Earlier"),
}

data class HistorySection(
    val group: HistoryGroup,
    val items: List<HistoryItem>,
)

val sampleHistory = listOf(
    HistorySection(
        group = HistoryGroup.Today,
        items = listOf(
            HistoryItem("h1", "Chat with GPT-5 about architecture patterns", HistoryType.Chat, "5 min ago", "Sure, let me break down the clean architecture...", true),
            HistoryItem("h2", "Weekly Report Task", HistoryType.Task, "10 min ago", "Collect data and generate report", false),
            HistoryItem("h3", "Image Analysis", HistoryType.ImageAnalysis, "1 hour ago", "Analyzing the uploaded diagram...", false),
        ),
    ),
    HistorySection(
        group = HistoryGroup.Yesterday,
        items = listOf(
            HistoryItem("h4", "Generate marketing copy", HistoryType.Chat, "Yesterday 3:15 PM", "Here are some variations of the...", false),
            HistoryItem("h5", "Monthly Data Export", HistoryType.Task, "Yesterday 2:00 PM", "Exporting user analytics data...", true),
            HistoryItem("h6", "Debug API errors", HistoryType.Chat, "Yesterday 11:30 AM", "The issue is in the auth middleware...", false),
        ),
    ),
    HistorySection(
        group = HistoryGroup.ThisWeek,
        items = listOf(
            HistoryItem("h7", "Summarize research paper", HistoryType.Chat, "Mon 10:20 AM", "The paper presents a novel approach...", false),
            HistoryItem("h8", "Database Backup", HistoryType.Task, "Mon 8:00 AM", "Backup completed successfully", false),
        ),
    ),
)

// ============================================================
// History Screen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = AuroraDp.dp80,
) {
    var searchQuery by remember { mutableStateOf("") }
    val favorites = emptyList<HistorySection>()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = AuroraDp.dp8),
    ) {
        AuroraTopBar(
            title = "History",
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More")
                }
            },
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuroraDp.dp16, vertical = AuroraDp.dp8),
            placeholder = {
                Text(
                    "Search conversations & tasks...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            shape = RoundedCornerShape(AuroraDp.dp16),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
            singleLine = true,
        )

        // History sections
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AuroraDp.dp16)
                .padding(bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(AuroraDp.dp8),
        ) {
            favorites.forEach { section ->
                item {
                    SectionHeader(title = section.group.label)
                }

                items(section.items, key = { it.id }) { item ->
                    HistoryItemCard(item = item)
                }

                item {
                    Spacer(modifier = Modifier.height(AuroraDp.dp4))
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: HistoryItem,
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
            // Type icon
            Icon(
                imageVector = item.type.icon,
                contentDescription = null,
                tint = item.type.color,
                modifier = Modifier.size(AuroraDp.dp20),
            )

            Spacer(modifier = Modifier.width(AuroraDp.dp12))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(AuroraDp.dp2))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AuroraDp.dp8),
                ) {
                    Text(
                        text = item.type.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = item.type.color,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = item.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.preview.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AuroraDp.dp4))
                    Text(
                        text = item.preview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Actions
            IconButton(onClick = { }, modifier = Modifier.size(AuroraDp.dp32)) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    modifier = Modifier.size(AuroraDp.dp18),
                    tint = if (item.isFavorite) Color(0xFFEF5350)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { }, modifier = Modifier.size(AuroraDp.dp32)) {
                Icon(
                    imageVector = Icons.Filled.FileDownload,
                    contentDescription = "Export",
                    modifier = Modifier.size(AuroraDp.dp18),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { }, modifier = Modifier.size(AuroraDp.dp32)) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(AuroraDp.dp18),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
