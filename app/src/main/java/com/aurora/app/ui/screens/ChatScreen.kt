package com.aurora.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.aurora.app.R
import com.aurora.app.data.ApiService
import com.aurora.app.data.ConversationStore
import com.aurora.app.data.ConversationHistory
import com.aurora.app.data.ChatSession
import com.aurora.app.data.Message
import com.aurora.app.data.Role
import com.aurora.app.data.UserProfile
import com.aurora.app.ui.components.MarkdownText
import kotlinx.coroutines.launch
import java.util.Calendar

// ─── Data Models ────────────────────────────────────────────────

data class Model(val id: String, val name: String, val tags: List<String>, val provider: String)

// ─── Constants ──────────────────────────────────────────────────

private val DEFAULT_MODELS = listOf(
    Model("gemini-2.5-pro", "Gemini 3 Pro", listOf("128K", "Multimodal"), "Google"),
    Model("gpt-5.4-pro", "GPT-5.4 Pro", listOf("256K", "Vision"), "OpenAI"),
    Model("claude-sonnet", "Claude Sonnet", listOf("200K", "Code"), "Anthropic"),
    Model("llama-3", "Llama 3.1 405B", listOf("131K", "Open"), "Meta"),
)

private val SUGGESTIONS = listOf("Write Code", "Translate", "Summarize PDF", "Create Task", "Explain Concept")

// ─── ChatScreen ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(modifier: Modifier = Modifier) {
    val messages = ChatSession.messages
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    // Build model list: configured models first, then defaults — recompute on config change
    var configVersion by remember { mutableStateOf(0) }
    val configuredModels = remember(configVersion) {
        val configs = ApiService.getEnabledConfigs()
        val models = mutableListOf<Model>()
        configs.forEach { config ->
            config.models.forEach { modelId ->
                models.add(Model(modelId, modelId, emptyList(), config.name))
            }
        }
        models
    }
    val allModels = remember(configVersion) {
        configuredModels + DEFAULT_MODELS.filter { def ->
            configuredModels.none { it.id == def.id }
        }
    }

    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var selectedModel by remember(configVersion) {
        mutableStateOf(allModels.firstOrNull() ?: DEFAULT_MODELS[0])
    }
    var showModelSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> stringResource(R.string.chat_good_morning)
        hour < 17 -> stringResource(R.string.chat_good_afternoon)
        else -> stringResource(R.string.chat_good_evening)
    }

    // Auto-scroll to bottom when messages change
    LaunchedEffect(messages.size, loading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Save conversation when navigating away
    DisposableEffect(Unit) {
        onDispose { ChatSession.ensureSaved() }
    }

    // Periodically check for config changes (API keys added/removed)
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2000)
            val currentCount = ApiService.getEnabledConfigs().size
            if (currentCount > 0 && configuredModels.size != ApiService.getAllModels().size) {
                configVersion++
            }
        }
    }

    val noApiMessage = stringResource(R.string.chat_error_no_api)

    // First-launch dialog
    var showWelcomeDialog by remember { mutableStateOf(UserProfile.isFirstLaunch()) }
    var nicknameInput by remember { mutableStateOf("") }
    val displayName = UserProfile.nickname.ifEmpty { "Alex" }

    val send = { text: String? ->
        val content = (text ?: input).trim()
        if (content.isNotEmpty()) {
            input = ""
            messages.add(Message(System.nanoTime().toString(), Role.User, content, ts = "now"))
            loading = true

            scope.launch {
                val enabledConfigs = ApiService.getEnabledConfigs()
                if (enabledConfigs.isEmpty()) {
                    messages.add(Message(
                        id = System.nanoTime().toString(),
                        role = Role.Assistant,
                        content = noApiMessage,
                        model = selectedModel.name,
                        ts = "now",
                    ))
                    loading = false
                } else {
                    val config = enabledConfigs.find { it.models.contains(selectedModel.id) }
                        ?: enabledConfigs.first()
                    val chatMessages = messages.map {
                        com.aurora.app.data.ChatMessage(
                            id = it.id,
                            role = if (it.role == Role.User) "user" else "assistant",
                            content = it.content,
                        )
                    }
                    val result = ApiService.chat(config, selectedModel.id, chatMessages)
                    result.fold(
                        onSuccess = { response ->
                            messages.add(Message(
                                id = System.nanoTime().toString(),
                                role = Role.Assistant,
                                content = response,
                                model = selectedModel.name,
                                ts = "now",
                            ))
                        },
                        onFailure = { error ->
                            messages.add(Message(
                                id = System.nanoTime().toString(),
                                role = Role.Assistant,
                                content = "Error: ${error.message}",
                                model = selectedModel.name,
                                ts = "now",
                            ))
                        }
                    )
                    loading = false
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp, vertical = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    HeroCard(greeting = greeting, displayName = displayName, modelName = selectedModel.name, colorScheme = colorScheme)
                }
                item {
                    ModelSelectorCard(
                        model = selectedModel,
                        colorScheme = colorScheme,
                        onClick = { showModelSheet = true },
                    )
                }
                items(messages, key = { it.id }) { msg ->
                    MessageCard(message = msg, colorScheme = colorScheme)
                }
                if (loading) {
                    item { LoadingDots(modelName = selectedModel.name, colorScheme = colorScheme) }
                }
                if (messages.size <= 1 && !loading) {
                    item {
                        SuggestionRow(
                            suggestions = SUGGESTIONS,
                            colorScheme = colorScheme,
                            onSelect = { send(it) },
                        )
                    }
                }
            }

            InputBar(
                input = input,
                onInputChange = { input = it },
                onSend = { send(null) },
                onNewChat = {
                    ChatSession.startNew()
                    input = ""
                },
                enabled = input.isNotBlank(),
                hasMessages = messages.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showModelSheet) {
        ModalBottomSheet(
            onDismissRequest = { showModelSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(stringResource(R.string.chat_choose_model), style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.chat_select_model_hint),
                    style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                allModels.forEach { m ->
                    val isSelected = m.id == selectedModel.id
                    val isConfigured = configuredModels.any { it.id == m.id }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) colorScheme.primaryContainer else Color.Transparent)
                            .clickable {
                                if (isConfigured) {
                                    selectedModel = m
                                    showModelSheet = false
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                                .background(if (isConfigured) colorScheme.secondaryContainer else colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.SmartToy, null,
                                tint = if (isConfigured) colorScheme.secondary else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(m.name, style = MaterialTheme.typography.titleMedium,
                                color = if (isConfigured) colorScheme.primary else colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (m.tags.isNotEmpty()) {
                                    m.tags.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(colorScheme.surfaceVariant)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(tag, fontSize = 9.sp, color = colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                Text(m.provider, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                            }
                        }
                        if (isSelected && isConfigured) {
                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(colorScheme.primary),
                                contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ── Welcome Dialog (first launch) ──
    if (showWelcomeDialog) {
        AlertDialog(
            onDismissRequest = {
                if (nicknameInput.isNotBlank()) {
                    UserProfile.setNickname(nicknameInput)
                }
                showWelcomeDialog = false
            },
            title = {
                Text("Welcome to Aurora", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Enter your nickname to get started:",
                        color = colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nicknameInput,
                        onValueChange = { nicknameInput = it },
                        placeholder = { Text("Your name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nicknameInput.isNotBlank()) {
                        UserProfile.setNickname(nicknameInput)
                    }
                    showWelcomeDialog = false
                }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
        )
    }
}

// ─── Sub-components ─────────────────────────────────────────────

@Composable
private fun HeroCard(greeting: String, displayName: String, modelName: String, colorScheme: androidx.compose.material3.ColorScheme) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("$greeting, $displayName", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary)
                Spacer(Modifier.height(2.dp))
                Text("Aurora AI", style = MaterialTheme.typography.headlineLarge,
                    color = colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    .background(colorScheme.primary.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.SmartToy, null, tint = colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(modelName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onPrimaryContainer)
                    }
                }
            }
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp))
                .background(colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = colorScheme.primary, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun ModelSelectorCard(model: Model, colorScheme: androidx.compose.material3.ColorScheme, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                .background(colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.SmartToy, null, tint = colorScheme.secondary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(model.name, style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    model.tags.forEach { tag ->
                        Box(modifier = Modifier.clip(RoundedCornerShape(5.dp))
                            .background(colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(tag, fontSize = 9.sp, color = colorScheme.onSurfaceVariant)
                        }
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(5.dp))
                        .background(colorScheme.primaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(model.provider, fontSize = 9.sp, color = colorScheme.primary,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(10.dp))
                .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.KeyboardArrowDown, null, tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun MessageCard(message: Message, colorScheme: androidx.compose.material3.ColorScheme) {
    val context = LocalContext.current
    val view = LocalView.current

    if (message.role == Role.User) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier.widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(message.content, style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.primary, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(message.ts, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(22.dp).clip(RoundedCornerShape(6.dp))
                        .background(colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AutoAwesome, null, tint = colorScheme.primary, modifier = Modifier.size(12.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(message.model ?: "Aurora AI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = colorScheme.primary)
                    Spacer(Modifier.weight(1f))
                    Text(message.ts, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(10.dp))
                MarkdownText(
                    text = message.content,
                    textColor = colorScheme.onSurface,
                    codeBg = colorScheme.primary,
                    fontSize = 14f,
                    lineHeight = 22f,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                // Action icons with real functionality
                Row {
                    // Copy
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Aurora Message", message.content))
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, stringResource(R.string.label_copy), tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp))
                    }
                    // Share
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, message.content)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Outlined.Share, stringResource(R.string.label_share), tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp))
                    }
                    // More
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Outlined.MoreHoriz, stringResource(R.string.label_more), tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingDots(modelName: String, colorScheme: androidx.compose.material3.ColorScheme) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(22.dp).clip(RoundedCornerShape(6.dp))
                    .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = colorScheme.primary, modifier = Modifier.size(12.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(modelName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.primary)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(3) { i ->
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = i * 200),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "dot$i",
                    )
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape)
                        .background(colorScheme.primary.copy(alpha = alpha)))
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(suggestions: List<String>, colorScheme: androidx.compose.material3.ColorScheme, onSelect: (String) -> Unit) {
    Column {
        Text(stringResource(R.string.chat_try_asking), fontSize = 12.sp, color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            suggestions.forEach { s ->
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .background(colorScheme.secondaryContainer)
                        .clickable { onSelect(s) }.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(s, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
private fun InputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onNewChat: () -> Unit,
    enabled: Boolean,
    hasMessages: Boolean,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .background(colorScheme.background)
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
            IconButton(
            onClick = onNewChat,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = colorScheme.surfaceVariant,
                contentColor = colorScheme.onSurfaceVariant,
            ),
        ) {
            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(6.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(colorScheme.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.surface)
                    .padding(5.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.AttachFile, null, tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp))
                }
            }
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = { Text(stringResource(R.string.chat_placeholder), color = colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = colorScheme.primary,
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
            IconButton(onClick = {}, modifier = Modifier.size(30.dp)) {
                Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(10.dp))
                    .background(colorScheme.secondaryContainer)
                    .padding(5.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Mic, null, tint = colorScheme.secondary, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.width(6.dp))
        IconButton(
            onClick = onSend,
            enabled = enabled,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (enabled) colorScheme.primary else colorScheme.surfaceVariant,
                contentColor = if (enabled) Color.White else colorScheme.onSurfaceVariant,
            ),
        ) {
            Icon(Icons.AutoMirrored.Rounded.Send, null, modifier = Modifier.size(18.dp))
        }
    }
}
