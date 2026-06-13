package com.aurora.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.aurora.app.R
import com.aurora.app.data.ApiService
import com.aurora.app.ui.theme.AppMotion
import com.aurora.app.data.Attachment
import com.aurora.app.data.ChatSession
import com.aurora.app.data.Message
import com.aurora.app.data.Role
import com.aurora.app.data.TokenUsage
import com.aurora.app.ui.components.MarkdownRenderer
import com.aurora.app.ui.components.MessageSearchBar
import com.aurora.app.ui.components.TokenUsageBadge
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

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

/** Build the text content to send to the API, including any attachment. */
private fun buildApiContent(message: Message): String {
    val sb = StringBuilder(message.content)
    message.attachment?.let { att ->
        when {
            att.mimeType.startsWith("image/") ->
                sb.append("\n\n[Image: ${att.name}]\ndata:${att.mimeType};base64,${att.base64Data}")
            att.mimeType.startsWith("text/") || att.mimeType.contains("json") ||
                att.mimeType.contains("xml") || att.mimeType.contains("javascript") -> {
                try {
                    val text = String(android.util.Base64.decode(att.base64Data, android.util.Base64.NO_WRAP), Charsets.UTF_8)
                    sb.append("\n\n--- File: ${att.name} ---\n$text\n---")
                } catch (_: Exception) {
                    sb.append("\n\n[File attached: ${att.name}]")
                }
            }
            else -> sb.append("\n\n[File attached: ${att.name}]")
        }
    }
    return sb.toString()
}

// ─── ChatScreen ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    scrollToTopTrigger: Int = 0,
    onConsumeSharedText: () -> String? = { null },
) {
    val messages = ChatSession.messages
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val colorScheme = MaterialTheme.colorScheme

    // File picker
    var pendingAttachment by remember { mutableStateOf<Attachment?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val resolver = context.contentResolver
                val mimeType = resolver.getType(it) ?: "application/octet-stream"
                val fileName = it.lastPathSegment ?: "file"
                val bytes = resolver.openInputStream(it)?.use { stream -> stream.readBytes() }
                if (bytes != null && bytes.size <= 10 * 1024 * 1024) {
                    val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    pendingAttachment = Attachment(fileName, mimeType, base64)
                }
            } catch (_: Exception) {}
        }
    }

    // Build model list
    val configVersion = ApiService.configVersion
    val configuredModels = remember(configVersion) {
        val configs = ApiService.getEnabledConfigs()
        val models = mutableListOf<Model>()
        configs.forEach { config ->
            config.models.filter { it !in config.disabledModels }.forEach { modelId ->
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
    var selectedModel by remember { mutableStateOf(allModels.firstOrNull() ?: DEFAULT_MODELS[0]) }
    var showModelSheet by remember { mutableStateOf(false) }

    // Search state
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchMatches by remember { mutableStateOf(listOf<Int>()) }
    var currentMatchIndex by remember { mutableIntStateOf(0) }

    // Consume shared text from other apps
    LaunchedEffect(Unit) {
        val shared = onConsumeSharedText()
        if (!shared.isNullOrBlank()) {
            input = shared
        }
    }

    // Search logic
    LaunchedEffect(searchQuery, messages.size) {
        if (searchQuery.isBlank()) {
            searchMatches = emptyList()
            currentMatchIndex = 0
        } else {
            val q = searchQuery.lowercase()
            searchMatches = messages.indices.filter { i ->
                messages[i].content.lowercase().contains(q)
            }
            currentMatchIndex = if (searchMatches.isNotEmpty()) 0 else -1
        }
    }

    LaunchedEffect(configVersion) {
        if (allModels.none { it.id == selectedModel.id }) {
            selectedModel = allModels.firstOrNull() ?: DEFAULT_MODELS[0]
        }
    }
    val listState = rememberLazyListState()

    val isNotAtBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisible != null && lastVisible.index < messages.size - 1
        }
    }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> stringResource(R.string.chat_good_morning)
        hour < 17 -> stringResource(R.string.chat_good_afternoon)
        else -> stringResource(R.string.chat_good_evening)
    }

    // Auto-scroll
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            kotlinx.coroutines.delay(50)
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }
    val lastMessageContent = messages.lastOrNull()?.content ?: ""
    LaunchedEffect(lastMessageContent.length) {
        if (loading && lastMessageContent.isNotEmpty()) {
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }
    LaunchedEffect(loading) {
        if (loading) {
            kotlinx.coroutines.delay(100)
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }
    LaunchedEffect(loading) {
        if (!loading && messages.isNotEmpty()) {
            kotlinx.coroutines.delay(600)
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }
    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) listState.scrollToItem(0)
    }

    var activeJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            activeJob?.cancel()
            ChatSession.ensureSaved()
        }
    }

    val noApiMessage = stringResource(R.string.chat_error_no_api)

    // Edit message state
    var editingMessageId by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }

    val send = { text: String?, attachment: Attachment? ->
        val content = (text ?: input).trim()
        if (content.isNotEmpty() || attachment != null) {
            input = ""
            pendingAttachment = null
            keyboardController?.hide()
            focusManager.clearFocus()
            messages.add(Message(
                id = UUID.randomUUID().toString(),
                role = Role.User,
                content = content,
                ts = "now",
                attachment = attachment,
            ))
            activeJob?.cancel()
            loading = true

            activeJob = scope.launch {
                val enabledConfigs = ApiService.getEnabledConfigs()
                if (enabledConfigs.isEmpty()) {
                    messages.add(Message(
                        id = UUID.randomUUID().toString(),
                        role = Role.Assistant,
                        content = noApiMessage,
                        model = selectedModel.name,
                        ts = "now",
                    ))
                    loading = false
                    activeJob = null
                } else {
                    val config = enabledConfigs.find { it.models.contains(selectedModel.id) }
                        ?: enabledConfigs.first()
                    val chatMessages = messages.map { msg ->
                        com.aurora.app.data.ChatMessage(
                            id = msg.id,
                            role = if (msg.role == Role.User) "user" else "assistant",
                            content = buildApiContent(msg),
                        )
                    }

                    val assistantMsgId = UUID.randomUUID().toString()
                    messages.add(Message(
                        id = assistantMsgId,
                        role = Role.Assistant,
                        content = "",
                        model = selectedModel.name,
                        ts = "now",
                    ))

                    val sb = StringBuilder()
                    try {
                        ApiService.chatStream(config, selectedModel.id, chatMessages).collect { chunk ->
                            sb.append(chunk)
                            val idx = messages.indexOfLast { it.id == assistantMsgId }
                            if (idx >= 0) {
                                messages[idx] = messages[idx].copy(content = sb.toString())
                            }
                        }
                    } catch (e: Exception) {
                        val idx = messages.indexOfLast { it.id == assistantMsgId }
                        if (idx >= 0) {
                            messages[idx] = messages[idx].copy(
                                content = if (sb.isEmpty()) "Error: ${e.message}" else sb.toString()
                            )
                        }
                    }
                    loading = false
                    activeJob = null
                }
            }
        }
    }

    // Resend from a specific message (edit & resend)
    val resendFrom = { msgId: String, newContent: String ->
        val idx = messages.indexOfFirst { it.id == msgId }
        if (idx >= 0) {
            // Update the message content
            messages[idx] = messages[idx].copy(content = newContent)
            // Remove all messages after this one
            while (messages.size > idx + 1) {
                messages.removeAt(messages.size - 1)
            }
            // Re-send
            activeJob?.cancel()
            loading = true

            activeJob = scope.launch {
                val enabledConfigs = ApiService.getEnabledConfigs()
                if (enabledConfigs.isEmpty()) {
                    messages.add(Message(
                        id = UUID.randomUUID().toString(),
                        role = Role.Assistant,
                        content = noApiMessage,
                        model = selectedModel.name,
                        ts = "now",
                    ))
                    loading = false
                    activeJob = null
                    return@launch
                }
                val config = enabledConfigs.find { it.models.contains(selectedModel.id) }
                    ?: enabledConfigs.first()
                val chatMessages = messages.map { msg ->
                    com.aurora.app.data.ChatMessage(
                        id = msg.id,
                        role = if (msg.role == Role.User) "user" else "assistant",
                        content = buildApiContent(msg),
                    )
                }
                val assistantMsgId = UUID.randomUUID().toString()
                messages.add(Message(
                    id = assistantMsgId,
                    role = Role.Assistant,
                    content = "",
                    model = selectedModel.name,
                    ts = "now",
                ))
                val sb = StringBuilder()
                try {
                    ApiService.chatStream(config, selectedModel.id, chatMessages).collect { chunk ->
                        sb.append(chunk)
                        val i = messages.indexOfLast { it.id == assistantMsgId }
                        if (i >= 0) messages[i] = messages[i].copy(content = sb.toString())
                    }
                } catch (e: Exception) {
                    val i = messages.indexOfLast { it.id == assistantMsgId }
                    if (i >= 0) messages[i] = messages[i].copy(
                        content = if (sb.isEmpty()) "Error: ${e.message}" else sb.toString()
                    )
                }
                loading = false
                activeJob = null
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar
            // Search bar — graphicsLayer fade+slide for 120fps
            val searchAlpha by animateFloatAsState(
                targetValue = if (showSearch) 1f else 0f,
                animationSpec = AppMotion.springDefault(),
                label = "searchAlpha",
            )
            val searchOffsetY by animateFloatAsState(
                targetValue = if (showSearch) 0f else 1f,
                animationSpec = AppMotion.springDefault(),
                label = "searchOffsetY",
            )
            // Always compose but skip drawing when invisible — avoids relayout
            Box(modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = searchAlpha
                    translationY = searchOffsetY * (-40f)
                }
            ) {
                if (searchAlpha > 0.01f) {
                    MessageSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        matchCount = searchMatches.size,
                        currentMatch = if (currentMatchIndex >= 0) currentMatchIndex + 1 else 0,
                        onNextMatch = {
                            if (searchMatches.isNotEmpty()) {
                                currentMatchIndex = (currentMatchIndex + 1) % searchMatches.size
                                scope.launch { listState.animateScrollToItem(searchMatches[currentMatchIndex]) }
                            }
                        },
                        onPreviousMatch = {
                            if (searchMatches.isNotEmpty()) {
                                currentMatchIndex = (currentMatchIndex - 1 + searchMatches.size) % searchMatches.size
                                scope.launch { listState.animateScrollToItem(searchMatches[currentMatchIndex]) }
                            }
                        },
                        onDismiss = { showSearch = false; searchQuery = "" },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp, vertical = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = { showSearch = !showSearch }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Search, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                item {
                    HeroCard(greeting = greeting, modelName = selectedModel.name, colorScheme = colorScheme)
                }
                item {
                    ModelSelectorCard(model = selectedModel, colorScheme = colorScheme, onClick = { showModelSheet = true })
                }
                items(messages, key = { it.id }) { msg ->
                    val isLastMsg = msg.id == messages.lastOrNull()?.id
                    val isHighlighted = searchQuery.isNotBlank() && msg.content.lowercase().contains(searchQuery.lowercase())
                    val msgIndex = messages.indexOf(msg)

                    MessageCard(
                        message = msg,
                        colorScheme = colorScheme,
                        isStreaming = isLastMsg && loading,
                        isHighlighted = isHighlighted,
                        isEditing = editingMessageId == msg.id,
                        editingText = if (editingMessageId == msg.id) editingText else "",
                        onEditStart = {
                            editingMessageId = msg.id
                            editingText = msg.content
                        },
                        onEditConfirm = {
                            resendFrom(msg.id, editingText)
                            editingMessageId = null
                        },
                        onEditCancel = { editingMessageId = null },
                        onEditChange = { editingText = it },
                        onBranch = {
                            ChatSession.branchAt(msgIndex)
                        },
                        onRetry = {
                            val lastUser = messages.lastOrNull { it.role == Role.User }
                            if (lastUser != null) send(lastUser.content, null)
                        },
                    )
                }
                if (loading) {
                    item { LoadingDots(modelName = selectedModel.name, colorScheme = colorScheme) }
                }
                if (messages.size <= 1 && !loading) {
                    item { SuggestionRow(suggestions = SUGGESTIONS, colorScheme = colorScheme, onSelect = { send(it, null) }) }
                }
            }

            // Pending attachment preview
            if (pendingAttachment != null) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "📎 ${pendingAttachment!!.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(onClick = { pendingAttachment = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.Close, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Branch indicator
            if (ChatSession.currentBranchId != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp)).background(colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Rounded.CallSplit, null, tint = colorScheme.tertiary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.branch_active), fontSize = 11.sp, color = colorScheme.tertiary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        ChatSession.restoreBranch(ChatSession.currentBranchId!!)
                    }) {
                        Text(stringResource(R.string.branch_restore), fontSize = 11.sp)
                    }
                }
            }

            InputBar(
                input = input,
                onInputChange = { input = it },
                onSend = { send(null, pendingAttachment) },
                onNewChat = { ChatSession.startNew(); input = "" },
                onFile = { filePickerLauncher.launch("*/*") },
                enabled = input.isNotBlank() || pendingAttachment != null,
                hasMessages = messages.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // FAB — graphicsLayer-based for 120fps (no recomposition, no relayout)
        val fabVisible = isNotAtBottom && messages.size > 2
        val fabScale by animateFloatAsState(
            targetValue = if (fabVisible) 1f else 0f,
            animationSpec = AppMotion.springStiff(),
            label = "fabScale",
        )
        val fabAlpha by animateFloatAsState(
            targetValue = if (fabVisible) 1f else 0f,
            animationSpec = AppMotion.springStiff(),
            label = "fabAlpha",
        )
        if (fabAlpha > 0.01f) {
            FloatingActionButton(
                onClick = { scope.launch { listState.scrollToItem(messages.size - 1) } },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 80.dp)
                    .graphicsLayer {
                        scaleX = fabScale
                        scaleY = fabScale
                        alpha = fabAlpha
                    },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Icon(Icons.Rounded.KeyboardArrowDown, "Scroll to bottom")
            }
        }
    }

    // Model sheet
    if (showModelSheet) {
        ModalBottomSheet(
            onDismissRequest = { showModelSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colorScheme.surface,
        ) {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                item {
                    Text(stringResource(R.string.chat_choose_model), style = MaterialTheme.typography.titleLarge,
                        color = colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.chat_select_model_hint),
                        style = MaterialTheme.typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                }
                items(allModels, key = { it.id }) { m ->
                    val isSelected = m.id == selectedModel.id
                    val isConfigured = configuredModels.any { it.id == m.id }
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) colorScheme.primaryContainer else Color.Transparent)
                            .clickable { selectedModel = m; showModelSheet = false }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (isConfigured) colorScheme.primaryContainer else colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.SmartToy, null,
                                tint = if (isConfigured) colorScheme.primary else colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(buildAnnotatedString {
                                append(m.name)
                                if (!isConfigured) { append(" "); withStyle(SpanStyle(fontSize = 9.sp, color = colorScheme.error)) { append("(no key)") } }
                            }, style = MaterialTheme.typography.titleMedium,
                                color = if (isConfigured) colorScheme.primary else colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                m.tags.forEach { tag ->
                                    Box(modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(colorScheme.surfaceVariant).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                        Text(tag, fontSize = 9.sp, color = colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(m.provider, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                            }
                        }
                        if (isSelected && isConfigured) {
                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(colorScheme.primary), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

}

// ─── Sub-components ─────────────────────────────────────────────

@Composable
private fun HeroCard(greeting: String, modelName: String, colorScheme: androidx.compose.material3.ColorScheme) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(greeting, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.primary)
                Spacer(Modifier.height(2.dp))
                Text("Aurora AI", style = MaterialTheme.typography.headlineLarge, color = colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(colorScheme.primary.copy(alpha = 0.2f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.SmartToy, null, tint = colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(modelName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onPrimaryContainer)
                    }
                }
            }
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(colorScheme.primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = colorScheme.primary, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
private fun ModelSelectorCard(model: Model, colorScheme: androidx.compose.material3.ColorScheme, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.SmartToy, null, tint = colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(model.name, style = MaterialTheme.typography.titleMedium, color = colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    model.tags.forEach { tag ->
                        Box(modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(colorScheme.surfaceVariant).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(tag, fontSize = 9.sp, color = colorScheme.onSurfaceVariant)
                        }
                    }
                    Box(modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(colorScheme.primaryContainer).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(model.provider, fontSize = 9.sp, color = colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(10.dp)).background(colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.KeyboardArrowDown, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageCard(
    message: Message,
    colorScheme: androidx.compose.material3.ColorScheme,
    isStreaming: Boolean = false,
    isHighlighted: Boolean = false,
    isEditing: Boolean = false,
    editingText: String = "",
    onEditStart: () -> Unit = {},
    onEditConfirm: () -> Unit = {},
    onEditCancel: () -> Unit = {},
    onEditChange: (String) -> Unit = {},
    onBranch: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showContextMenu by remember { mutableStateOf(false) }

    val highlightBg = if (isHighlighted) colorScheme.tertiaryContainer.copy(alpha = 0.3f) else Color.Transparent

    if (message.role == Role.User) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(modifier = Modifier.widthIn(max = 280.dp).clip(RoundedCornerShape(24.dp))
                .background(colorScheme.surfaceVariant).padding(horizontal = 16.dp, vertical = 12.dp)
                .combinedClickable(onClick = {}, onLongClick = { showContextMenu = true })) {
                Column(horizontalAlignment = Alignment.End) {
                    if (isEditing) {
                        OutlinedTextField(value = editingText, onValueChange = onEditChange, modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.primary, fontWeight = FontWeight.SemiBold),
                            shape = RoundedCornerShape(12.dp))
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onEditCancel) { Text(stringResource(R.string.btn_cancel), fontSize = 12.sp) }
                            TextButton(onClick = onEditConfirm) { Text(stringResource(R.string.btn_ok), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        }
                    } else {
                        Text(message.content, style = MaterialTheme.typography.bodyLarge, color = colorScheme.primary, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
                        if (message.attachment != null) {
                            Spacer(Modifier.height(4.dp))
                            Text("📎 ${message.attachment.name}", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(message.ts, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                    }
                }
                DropdownMenu(expanded = showContextMenu, onDismissRequest = { showContextMenu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.menu_edit)) }, onClick = { showContextMenu = false; onEditStart() },
                        leadingIcon = { Icon(Icons.Outlined.Edit, null) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.label_copy)) }, onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Aurora", message.content))
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        showContextMenu = false
                    }, leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.menu_branch)) }, onClick = { showContextMenu = false; onBranch() },
                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.CallSplit, null) })
                }
            }
        }
    } else {
        Card(modifier = Modifier.fillMaxWidth().background(highlightBg), shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AutoAwesome, null, tint = colorScheme.primary, modifier = Modifier.size(12.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(message.model ?: "Aurora AI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.primary)
                    Spacer(Modifier.weight(1f))
                    if (message.tokenUsage != null) {
                        TokenUsageBadge(tokenUsage = message.tokenUsage)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(message.ts, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(10.dp))
                MarkdownRenderer(content = message.content, isStreaming = isStreaming, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Row {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Aurora Message", message.content))
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.ContentCopy, stringResource(R.string.label_copy), tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = {
                        val sendIntent = Intent().apply { action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, message.content); type = "text/plain" }
                        context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Share, stringResource(R.string.label_share), tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onRetry, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Refresh, stringResource(R.string.label_retry), tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingDots(modelName: String, colorScheme: androidx.compose.material3.ColorScheme) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    // Wave animation: dots bounce up and down with staggered phase
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ), label = "wave"
    )
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = colorScheme.primary, modifier = Modifier.size(12.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(modelName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.primary)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { i ->
                    // Each dot has a different phase offset for the wave
                    val phaseOffset = i * 0.33f
                    val wave = kotlin.math.sin((shift + phaseOffset) * kotlin.math.PI).toFloat()
                    val yOffset = wave * 6f // amplitude in dp
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape)
                        .graphicsLayer { translationY = yOffset.dp.toPx() }
                        .background(colorScheme.primary.copy(alpha = 0.4f + (kotlin.math.abs(wave) * 0.6f))))
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(suggestions: List<String>, colorScheme: androidx.compose.material3.ColorScheme, onSelect: (String) -> Unit) {
    Column {
        Text(stringResource(R.string.chat_try_asking), fontSize = 12.sp, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            suggestions.forEach { s ->
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(colorScheme.primaryContainer)
                    .clickable { onSelect(s) }.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(s, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.primary)
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
    onFile: () -> Unit,
    enabled: Boolean,
    hasMessages: Boolean,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(modifier = modifier.background(colorScheme.background).padding(horizontal = 16.dp).padding(bottom = 2.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onNewChat, modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = colorScheme.surfaceVariant, contentColor = colorScheme.onSurfaceVariant)) {
            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(6.dp))
        Row(modifier = Modifier.weight(1f).clip(RoundedCornerShape(32.dp)).background(colorScheme.surfaceVariant).padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onFile, modifier = Modifier.size(30.dp)) {
                Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(colorScheme.surface).padding(5.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.AttachFile, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
            OutlinedTextField(value = input, onValueChange = onInputChange,
                placeholder = { Text(stringResource(R.string.chat_placeholder), color = colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                modifier = Modifier.weight(1f).heightIn(max = 120.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, cursorColor = colorScheme.primary),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colorScheme.onSurface, fontWeight = FontWeight.Medium),
                maxLines = 5)
        }
        Spacer(Modifier.width(6.dp))
        if (enabled) {
            IconButton(onClick = onSend, modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = colorScheme.primary, contentColor = Color.White)) {
                Icon(Icons.AutoMirrored.Rounded.Send, null, modifier = Modifier.size(18.dp))
            }
        } else {
            IconButton(onClick = {}, enabled = false, modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = colorScheme.surfaceVariant, contentColor = colorScheme.onSurfaceVariant)) {
                Icon(Icons.AutoMirrored.Rounded.Send, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
