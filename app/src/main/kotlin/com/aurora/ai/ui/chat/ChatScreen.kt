package com.aurora.ai.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aurora.ai.theme.AuroraDp
import com.aurora.ai.ui.components.AuroraCard
import com.aurora.ai.ui.components.AuroraGreetingCard
import com.aurora.ai.ui.components.AuroraModelSelectorCard
import kotlinx.coroutines.launch

// ============================================================
// Data models
// ============================================================

data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val modelName: String? = null,
    val timestamp: String = "",
)

data class AIModel(
    val name: String,
    val description: String,
    val icon: String = "",
)

val availableModels = listOf(
    AIModel("Gemini 2.5 Pro", "Fast · Vision · 1M Context"),
    AIModel("GPT-5", "Advanced reasoning · Multimodal"),
    AIModel("Gemini 2.5 Flash", "Speed optimized · 1M Context"),
    AIModel("Claude 4", "Long context · Safe & reliable"),
    AIModel("DeepSeek V4", "Open source · Code expert"),
    AIModel("Custom API", "Your own API endpoint"),
)

// Prompt templates — migrated from OpenSeek's built-in prompts
val suggestedActions = listOf(
    "Write Code",
    "Translate Text",
    "Summarize Text",
    "Explain Concept",
    "Code Review",
    "Brainstorm Ideas",
    "Writing Assistant",
)

// ============================================================
// Chat Screen
// ============================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    userName: String = "User",
    bottomPadding: Dp = AuroraDp.dp80,
) {
    var messages by remember { mutableStateOf(emptyList<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf(availableModels[0]) }
    var showModelSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val isEmpty = messages.isEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding + AuroraDp.dp52),
        ) {
            // Scrollable content
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AuroraDp.dp16),
                verticalArrangement = Arrangement.spacedBy(AuroraDp.dp16),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = AuroraDp.dp12,
                    bottom = AuroraDp.dp16,
                ),
            ) {
                // Greeting Card
                item {
                    AuroraGreetingCard(
                        userName = userName,
                        greeting = getTimeBasedGreeting(),
                        onSettingsClick = { },
                    )
                }

                // Model Selector
                item {
                    Spacer(modifier = Modifier.height(AuroraDp.dp8))
                    AuroraModelSelectorCard(
                        modelName = selectedModel.name,
                        modelDescription = selectedModel.description,
                        onClick = { showModelSheet = true },
                    )
                }

                // Suggested Actions (when empty)
                if (isEmpty) {
                    item {
                        Spacer(modifier = Modifier.height(AuroraDp.dp16))
                        Text(
                            text = "Try these",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(AuroraDp.dp12))
                    }

                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AuroraDp.dp8),
                            verticalArrangement = Arrangement.spacedBy(AuroraDp.dp8),
                        ) {
                            suggestedActions.forEach { action ->
                                AssistChip(
                                    onClick = { inputText = action },
                                    label = {
                                        Text(
                                            text = action,
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    },
                                    modifier = Modifier.height(AuroraDp.dp40),
                                    shape = RoundedCornerShape(AuroraDp.dp20),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        labelColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                )
                            }
                        }
                    }
                }

                // Messages
                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(message = message)
                }
            }
        }

        // Input Area - fixed at bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(AuroraDp.dp52)
                .padding(bottom = bottomPadding),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = AuroraDp.dp2,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AuroraDp.dp8, vertical = AuroraDp.dp4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AuroraDp.dp4),
            ) {
                // Attachment button
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(AuroraDp.dp32),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add attachment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AuroraDp.dp18),
                    )
                }

                // Text input
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Ask anything...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    shape = RoundedCornerShape(AuroraDp.dp24),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    maxLines = 1,
                    textStyle = MaterialTheme.typography.bodyMedium,
                )

                // Voice input
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(AuroraDp.dp32),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Voice input",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AuroraDp.dp18),
                    )
                }

                // Image attachment
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(AuroraDp.dp32),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = "Add image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AuroraDp.dp18),
                    )
                }

                // Send button
                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            messages = messages + ChatMessage(
                                id = "msg_${System.currentTimeMillis()}",
                                content = inputText,
                                isUser = true,
                                timestamp = "Just now",
                            )
                            inputText = ""
                            scope.launch {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    },
                    modifier = Modifier.size(AuroraDp.dp40),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(AuroraDp.dp18),
                    )
                }
            }
        }

        // Model Bottom Sheet
        if (showModelSheet) {
            ModalBottomSheet(
                onDismissRequest = { showModelSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AuroraDp.dp24)
                        .padding(bottom = AuroraDp.dp32),
                ) {
                    Text(
                        text = "Select Model",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(AuroraDp.dp20))

                    availableModels.forEach { model ->
                        val isSelected = model.name == selectedModel.name
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AuroraDp.dp4),
                            shape = RoundedCornerShape(AuroraDp.dp16),
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                            onClick = {
                                selectedModel = model
                                showModelSheet = false
                            },
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = AuroraDp.dp16,
                                    vertical = AuroraDp.dp12,
                                ),
                            ) {
                                Text(
                                    text = model.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text = model.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// Chat Message Item
// ============================================================

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    modifier: Modifier = Modifier,
) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val containerColor = if (message.isUser)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceContainerHigh
    val maxWidth: Dp = if (message.isUser) AuroraDp.dp256 else AuroraDp.dp999

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AuroraDp.dp4),
        horizontalAlignment = alignment,
    ) {
        // Model name for assistant messages
        if (!message.isUser && message.modelName != null) {
            Text(
                text = message.modelName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = AuroraDp.dp4, start = AuroraDp.dp4),
            )
        }

        // Message bubble
        Surface(
            modifier = Modifier
                .widthIn(max = maxWidth),
            shape = RoundedCornerShape(
                topStart = AuroraDp.dp24,
                topEnd = AuroraDp.dp24,
                bottomStart = if (message.isUser) AuroraDp.dp24 else AuroraDp.dp4,
                bottomEnd = if (message.isUser) AuroraDp.dp4 else AuroraDp.dp24,
            ),
            color = containerColor,
            tonalElevation = AuroraDp.dp1,
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    horizontal = AuroraDp.dp16,
                    vertical = AuroraDp.dp12,
                ),
            )
        }

        // Action row for assistant messages
        if (!message.isUser) {
            Row(
                modifier = Modifier.padding(top = AuroraDp.dp4, start = AuroraDp.dp4),
                horizontalArrangement = Arrangement.spacedBy(AuroraDp.dp4),
            ) {
                ActionChip(
                    icon = Icons.Filled.ContentCopy,
                    label = "Copy",
                    onClick = { },
                )
                ActionChip(
                    icon = Icons.Filled.Share,
                    label = "Share",
                    onClick = { },
                )
                ActionChip(
                    icon = Icons.Filled.Refresh,
                    label = "Regenerate",
                    onClick = { },
                )
            }
        }
    }
}

@Composable
private fun ActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AuroraDp.dp12),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AuroraDp.dp10,
                vertical = AuroraDp.dp4,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AuroraDp.dp4),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(AuroraDp.dp14),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ============================================================
// Helpers
// ============================================================

private fun getTimeBasedGreeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when (hour) {
        in 0..5 -> "Good Night"
        in 6..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

private fun sampleMessages(): List<ChatMessage> = listOf(
    ChatMessage(
        id = "1",
        content = "Can you explain how quantum computing differs from classical computing?",
        isUser = true,
        timestamp = "2:30 PM",
    ),
    ChatMessage(
        id = "2",
        content = "Great question! Here's a concise comparison:\n\n" +
                "Classical computers use bits (0 or 1) as the smallest unit of data. " +
                "Quantum computers use qubits that can exist in superposition — being both 0 and 1 simultaneously. " +
                "This allows them to process vast amounts of possibilities at once, making them exponentially faster " +
                "for certain problems like factoring large numbers, simulating molecules, and optimization.\n\n" +
                "However, quantum computers are not simply 'faster computers' — they excel at specific problem classes " +
                "and require extremely controlled environments (near absolute zero) to operate.",
        isUser = false,
        modelName = "Gemini 2.5 Pro",
        timestamp = "2:30 PM",
    ),
)
