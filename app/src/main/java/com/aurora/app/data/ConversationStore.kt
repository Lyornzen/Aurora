package com.aurora.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject

enum class Role { User, Assistant }

data class TokenUsage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalTokens: Int = 0,
    val estimatedCost: Double = 0.0,
)

data class Attachment(
    val name: String,
    val mimeType: String,
    val base64Data: String,
)

data class Message(
    val id: String,
    val role: Role,
    val content: String,
    val model: String? = null,
    val ts: String,
    val tokenUsage: TokenUsage? = null,
    val attachment: Attachment? = null,
)

data class ConversationHistory(
    val id: String,
    val title: String,
    val model: String,
    val lastMessage: String,
    val messageCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
)

object ConversationStore {
    private const val PREFS_NAME = "conversations_prefs"
    private const val KEY_CONVERSATIONS = "conversations_history"
    private const val KEY_MESSAGES_PREFIX = "messages_"

    private var prefs: SharedPreferences? = null
    private val _conversations = mutableStateListOf<ConversationHistory>()
    val conversations: List<ConversationHistory> get() = _conversations

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadConversations()
    }

    private fun loadConversations() {
        val json = prefs?.getString(KEY_CONVERSATIONS, null) ?: return
        _conversations.clear()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                _conversations.add(
                    ConversationHistory(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        model = obj.getString("model"),
                        lastMessage = obj.optString("lastMessage", ""),
                        messageCount = obj.optInt("messageCount", 0),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    )
                )
            }
        } catch (_: Exception) {
            // Corrupted data; reset
        }
    }

    private fun saveConversations() {
        if (prefs == null) return
        val arr = JSONArray()
        for (conv in _conversations) {
            arr.put(
                JSONObject().apply {
                    put("id", conv.id)
                    put("title", conv.title)
                    put("model", conv.model)
                    put("lastMessage", conv.lastMessage)
                    put("messageCount", conv.messageCount)
                    put("timestamp", conv.timestamp)
                }
            )
        }
        prefs?.edit()?.putString(KEY_CONVERSATIONS, arr.toString())?.apply()
    }

    fun addConversation(conversation: ConversationHistory) {
        _conversations.add(0, conversation)
        saveConversations()
    }

    fun updateConversation(id: String, title: String, lastMessage: String, messageCount: Int) {
        val idx = _conversations.indexOfFirst { it.id == id }
        if (idx >= 0) {
            _conversations[idx] = _conversations[idx].copy(
                title = title,
                lastMessage = lastMessage,
                messageCount = messageCount,
                timestamp = System.currentTimeMillis(),
            )
            saveConversations()
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        val idx = _conversations.indexOfFirst { it.id == id }
        if (idx >= 0) {
            _conversations[idx] = _conversations[idx].copy(title = newTitle)
            saveConversations()
        }
    }

    fun deleteConversation(id: String) {
        _conversations.removeAll { it.id == id }
        prefs?.edit()?.remove(KEY_MESSAGES_PREFIX + id)?.apply()
        saveConversations()
    }

    fun togglePin(id: String) {
        val idx = _conversations.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val conv = _conversations[idx]
            _conversations[idx] = conv.copy(
                timestamp = if (conv.timestamp < 0) kotlin.math.abs(conv.timestamp)
                else -(kotlin.math.abs(conv.timestamp) + 1)
            )
            saveConversations()
        }
    }

    fun isPinned(id: String): Boolean {
        return _conversations.find { it.id == id }?.let { it.timestamp < 0 } ?: false
    }

    fun saveMessages(conversationId: String, messages: List<Message>) {
        if (prefs == null) return
        val arr = JSONArray()
        for (msg in messages) {
            arr.put(
                JSONObject().apply {
                    put("id", msg.id)
                    put("role", msg.role.name)
                    put("content", msg.content)
                    msg.model?.let { put("model", it) }
                    put("ts", msg.ts)
                    msg.tokenUsage?.let { tu ->
                        put("tokenUsage", JSONObject().apply {
                            put("promptTokens", tu.promptTokens)
                            put("completionTokens", tu.completionTokens)
                            put("totalTokens", tu.totalTokens)
                            put("estimatedCost", tu.estimatedCost)
                        })
                    }
                    msg.attachment?.let { att ->
                        put("attachment", JSONObject().apply {
                            put("name", att.name)
                            put("mimeType", att.mimeType)
                            put("base64Data", att.base64Data)
                        })
                    }
                }
            )
        }
        prefs?.edit()?.putString(KEY_MESSAGES_PREFIX + conversationId, arr.toString())?.apply()
    }

    fun loadMessages(conversationId: String): List<Message> {
        val json = prefs?.getString(KEY_MESSAGES_PREFIX + conversationId, null) ?: return emptyList()
        val result = mutableListOf<Message>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                var tokenUsage: TokenUsage? = null
                obj.optJSONObject("tokenUsage")?.let { tu ->
                    tokenUsage = TokenUsage(
                        promptTokens = tu.optInt("promptTokens", 0),
                        completionTokens = tu.optInt("completionTokens", 0),
                        totalTokens = tu.optInt("totalTokens", 0),
                        estimatedCost = tu.optDouble("estimatedCost", 0.0),
                    )
                }
                var attachment: Attachment? = null
                obj.optJSONObject("attachment")?.let { att ->
                    attachment = Attachment(
                        name = att.optString("name", ""),
                        mimeType = att.optString("mimeType", ""),
                        base64Data = att.optString("base64Data", ""),
                    )
                }
                result.add(
                    Message(
                        id = obj.getString("id"),
                        role = Role.valueOf(obj.getString("role")),
                        content = obj.getString("content"),
                        model = obj.optString("model").ifEmpty { null },
                        ts = obj.getString("ts"),
                        tokenUsage = tokenUsage,
                        attachment = attachment,
                    )
                )
            }
        } catch (_: Exception) {
            // Corrupted data
        }
        return result
    }

    fun clear() {
        _conversations.clear()
        saveConversations()
    }

    /**
     * Export a conversation as a formatted Markdown string.
     */
    fun exportConversation(conversationId: String): String {
        val conv = _conversations.find { it.id == conversationId } ?: return ""
        val msgs = loadMessages(conversationId)
        val sb = StringBuilder()
        sb.appendLine("# ${conv.title}")
        sb.appendLine()
        sb.appendLine("**Model:** ${conv.model}  ")
        sb.appendLine("**Messages:** ${conv.messageCount}  ")
        sb.appendLine("**Date:** ${java.util.Date(conv.timestamp)}")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()
        for (msg in msgs) {
            val role = if (msg.role == Role.User) "**You**" else "**${msg.model ?: "Aurora"}**"
            sb.appendLine("$role:")
            sb.appendLine()
            sb.appendLine(msg.content)
            sb.appendLine()
        }
        return sb.toString()
    }

    /**
     * Export a conversation as JSON.
     */
    fun exportConversationJson(conversationId: String): String {
        val conv = _conversations.find { it.id == conversationId } ?: return "{}"
        val msgs = loadMessages(conversationId)
        val json = JSONObject().apply {
            put("id", conv.id)
            put("title", conv.title)
            put("model", conv.model)
            put("messageCount", conv.messageCount)
            put("timestamp", conv.timestamp)
            put("messages", JSONArray().apply {
                msgs.forEach { msg ->
                    put(JSONObject().apply {
                        put("id", msg.id)
                        put("role", msg.role.name)
                        put("content", msg.content)
                        msg.model?.let { put("model", it) }
                        put("ts", msg.ts)
                    })
                }
            })
        }
        return json.toString(2)
    }
}
