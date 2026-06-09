package com.aurora.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

/**
 * Global singleton that survives Composable lifecycle changes,
 * so conversations persist when switching tabs.
 * Supports conversation branching — user can fork from any message.
 */
object ChatSession {
    val messages = mutableStateListOf<Message>()
    var currentConversationId by mutableStateOf<String?>(null)
    var selectedModelName by mutableStateOf("")

    // Branch history: maps message ID → snapshot of messages at that point
    private val branches = mutableMapOf<String, List<Message>>()
    var currentBranchId by mutableStateOf<String?>(null)
        private set

    fun loadConversation(id: String, msgs: List<Message>, model: String) {
        if (messages.isNotEmpty() && currentConversationId != id) {
            ensureSaved()
        }
        messages.clear()
        messages.addAll(msgs)
        currentConversationId = id
        selectedModelName = model
        branches.clear()
        currentBranchId = null
    }

    fun ensureSaved() {
        if (messages.isEmpty()) return
        val id = currentConversationId ?: run {
            val userMsgs = messages.filter { it.role == Role.User }
            val title = userMsgs.firstOrNull()?.content?.take(50) ?: "New Chat"
            val lastMsg = messages.lastOrNull()?.content?.take(100) ?: ""
            val convId = UUID.randomUUID().toString()
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

    /**
     * Create a branch at the given message index.
     * Saves current state and truncates messages to the branch point.
     * Returns the branch ID for later restoration.
     */
    fun branchAt(messageIndex: Int): String {
        val branchId = UUID.randomUUID().toString()
        // Save current full message list
        branches[branchId] = messages.toList()
        // Truncate to branch point (keep messages 0..messageIndex)
        val keep = messages.subList(0, messageIndex + 1).toList()
        messages.clear()
        messages.addAll(keep)
        currentBranchId = branchId
        return branchId
    }

    /**
     * Restore a previous branch.
     */
    fun restoreBranch(branchId: String) {
        val snapshot = branches[branchId] ?: return
        messages.clear()
        messages.addAll(snapshot)
        currentBranchId = null
        branches.remove(branchId)
    }

    /**
     * Get available branches.
     */
    fun getBranches(): Map<String, List<Message>> = branches.toMap()

    fun startNew() {
        ensureSaved()
        clear()
    }

    fun clear() {
        messages.clear()
        currentConversationId = null
        selectedModelName = ""
        branches.clear()
        currentBranchId = null
    }
}
