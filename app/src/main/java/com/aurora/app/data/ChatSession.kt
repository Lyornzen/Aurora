package com.aurora.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Global singleton that survives Composable lifecycle changes,
 * so conversations persist when switching tabs.
 */
object ChatSession {
    val messages = mutableStateListOf<Message>()
    var currentConversationId by mutableStateOf<String?>(null)
    var selectedModelName by mutableStateOf("")

    fun loadConversation(id: String, msgs: List<Message>, model: String) {
        // Save current before loading another
        if (messages.isNotEmpty() && currentConversationId != id) {
            ensureSaved()
        }
        messages.clear()
        messages.addAll(msgs)
        currentConversationId = id
        selectedModelName = model
    }

    fun ensureSaved() {
        if (messages.isEmpty()) return
        val id = currentConversationId ?: run {
            // Auto-create conversation entry for new chats
            val userMsgs = messages.filter { it.role == Role.User }
            val title = userMsgs.firstOrNull()?.content?.take(50) ?: "New Chat"
            val lastMsg = messages.lastOrNull()?.content?.take(100) ?: ""
            val convId = System.nanoTime().toString()
            ConversationStore.addConversation(ConversationHistory(
                id = convId,
                title = title,
                model = selectedModelName.ifEmpty { "Aurora" },
                lastMessage = lastMsg,
                messageCount = messages.size,
            ))
            ConversationStore.saveMessages(convId, messages.toList())
            currentConversationId = convId
            return
        }
        val lastMsg = messages.lastOrNull()?.content?.take(100) ?: ""
        val userMsgs = messages.filter { it.role == Role.User }
        val title = userMsgs.firstOrNull()?.content?.take(50) ?: "New Chat"
        ConversationStore.updateConversation(
            id = id,
            title = title,
            lastMessage = lastMsg,
            messageCount = messages.size,
        )
        ConversationStore.saveMessages(id, messages.toList())
    }

    fun startNew() {
        ensureSaved()
        clear()
    }

    fun clear() {
        messages.clear()
        currentConversationId = null
        selectedModelName = ""
    }
}
