package com.aurora.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.app.data.ApiService
import com.aurora.app.data.ChatMessage
import com.aurora.app.data.ChatSession
import com.aurora.app.data.Message
import com.aurora.app.data.Role
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

data class ModelInfo(
    val id: String,
    val name: String,
    val tags: List<String>,
    val provider: String,
)

class ChatViewModel : ViewModel() {

    val messages: MutableList<Message> = ChatSession.messages

    var loading by mutableStateOf(false)
        private set

    var selectedModel by mutableStateOf(ModelInfo("", "", emptyList(), ""))
        private set

    var activeJob by mutableStateOf<Job?>(null)
        private set

    private val defaultModels = listOf(
        ModelInfo("gemini-2.5-pro", "Gemini 3 Pro", listOf("128K", "Multimodal"), "Google"),
        ModelInfo("gpt-5.4-pro", "GPT-5.4 Pro", listOf("256K", "Vision"), "OpenAI"),
        ModelInfo("claude-sonnet", "Claude Sonnet", listOf("200K", "Code"), "Anthropic"),
        ModelInfo("llama-3", "Llama 3.1 405B", listOf("131K", "Open"), "Meta"),
    )

    val configuredModels: List<ModelInfo>
        get() {
            val configs = ApiService.getEnabledConfigs()
            return configs.flatMap { config ->
                config.models.filter { it !in config.disabledModels }.map { modelId ->
                    ModelInfo(modelId, modelId, emptyList(), config.name)
                }
            }
        }

    val allModels: List<ModelInfo>
        get() {
            val configured = configuredModels
            return configured + defaultModels.filter { def ->
                configured.none { it.id == def.id }
            }
        }

    fun initialize() {
        val models = allModels
        if (selectedModel.id.isEmpty() || models.none { it.id == selectedModel.id }) {
            selectedModel = models.firstOrNull() ?: defaultModels[0]
        }
    }

    fun selectModel(model: ModelInfo) {
        selectedModel = model
    }

    fun sendMessage(
        content: String,
        noApiMessage: String,
        onStreamUpdate: () -> Unit = {},
    ) {
        if (content.isBlank()) return

        messages.add(Message(UUID.randomUUID().toString(), Role.User, content.trim(), ts = "now"))
        activeJob?.cancel()
        loading = true

        activeJob = viewModelScope.launch {
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
            val chatMessages = messages.map {
                ChatMessage(
                    id = it.id,
                    role = if (it.role == Role.User) "user" else "assistant",
                    content = it.content,
                )
            }

            // Add placeholder for streaming
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
                    onStreamUpdate()
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

    fun cancelRequest() {
        activeJob?.cancel()
        loading = false
        activeJob = null
    }

    fun newChat() {
        ChatSession.startNew()
    }

    fun loadConversation(id: String, msgs: List<Message>, model: String) {
        ChatSession.loadConversation(id, msgs, model)
    }

    fun retryLastUserMessage(noApiMessage: String, onStreamUpdate: () -> Unit = {}) {
        val lastUser = messages.lastOrNull { it.role == Role.User }
        if (lastUser != null) {
            sendMessage(lastUser.content, noApiMessage, onStreamUpdate)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ChatSession.ensureSaved()
    }
}
