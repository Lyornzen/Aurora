package com.aurora.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.util.concurrent.TimeUnit

data class ApiConfig(
    val id: String,
    val name: String,
    val apiKey: String,
    val baseUrl: String,
    val models: List<String> = emptyList(),
    val enabled: Boolean = true,
    val protocol: String = "openai", // "openai" or "anthropic"
    val disabledModels: List<String> = emptyList(), // hidden in model picker
    val systemPrompt: String = "", // custom system prompt
)

data class ChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val model: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

data class ChatResult(
    val content: String,
    val tokenUsage: TokenUsage? = null,
)

object ApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val configs = mutableListOf<ApiConfig>()

    /** Incremented on every config change — Compose screens observe this to refresh. */
    var configVersion by mutableIntStateOf(0)
        private set

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("aurora_prefs", Context.MODE_PRIVATE)
        val json = prefs?.getString("api_configs", null) ?: return
        if (json.isBlank()) return
        configs.clear()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val modelsArray = obj.optJSONArray("models") ?: JSONArray()
                val models = mutableListOf<String>()
                for (j in 0 until modelsArray.length()) {
                    models.add(modelsArray.getString(j))
                }
                val disabledArray = obj.optJSONArray("disabledModels") ?: JSONArray()
                val disabledModels = mutableListOf<String>()
                for (j in 0 until disabledArray.length()) {
                    disabledModels.add(disabledArray.getString(j))
                }
                val config = ApiConfig(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    apiKey = obj.getString("apiKey"),
                    baseUrl = obj.getString("baseUrl"),
                    models = models,
                    enabled = obj.optBoolean("enabled", true),
                    protocol = obj.optString("protocol", "openai"),
                    disabledModels = disabledModels,
                    systemPrompt = obj.optString("systemPrompt", ""),
                )
                configs.add(config)
            }
        } catch (_: Exception) {
            // Corrupted data, ignore
        }
    }

    private fun saveConfigs() {
        val p = prefs ?: return
        val array = JSONArray()
        for (config in configs) {
            val modelsArray = JSONArray()
            for (model in config.models) {
                modelsArray.put(model)
            }
            val obj = JSONObject().apply {
                put("id", config.id)
                put("name", config.name)
                put("apiKey", config.apiKey)
                put("baseUrl", config.baseUrl)
                put("models", modelsArray)
                put("enabled", config.enabled)
                put("protocol", config.protocol)
                val disabledArray = JSONArray()
                for (m in config.disabledModels) { disabledArray.put(m) }
                put("disabledModels", disabledArray)
                put("systemPrompt", config.systemPrompt)
            }
            array.put(obj)
        }
        p.edit().putString("api_configs", array.toString()).apply()
    }

    fun getConfigs(): List<ApiConfig> = configs.toList()

    fun addConfig(config: ApiConfig) {
        configs.removeAll { it.id == config.id }
        configs.add(config)
        saveConfigs()
        configVersion++
    }

    fun removeConfig(id: String) {
        configs.removeAll { it.id == id }
        saveConfigs()
        configVersion++
    }

    fun getEnabledConfigs(): List<ApiConfig> = configs.filter { it.enabled && it.apiKey.isNotBlank() }

    fun getAllModels(): List<Pair<String, String>> =
        configs.filter { it.enabled }.flatMap { c ->
            c.models.filter { it !in c.disabledModels }.map { it to "${c.name} / $it" }
        }

    fun reorderModels(configId: String, newOrder: List<String>) {
        val idx = configs.indexOfFirst { it.id == configId }
        if (idx >= 0) {
            configs[idx] = configs[idx].copy(models = newOrder)
            saveConfigs()
            configVersion++
        }
    }

    fun toggleModelDisabled(configId: String, modelId: String, disabled: Boolean) {
        val idx = configs.indexOfFirst { it.id == configId }
        if (idx >= 0) {
            val cfg = configs[idx]
            val newDisabled = if (disabled)
                cfg.disabledModels + modelId
            else
                cfg.disabledModels - modelId
            configs[idx] = cfg.copy(disabledModels = newDisabled)
            saveConfigs()
            configVersion++
        }
    }

    suspend fun fetchModels(baseUrl: String, apiKey: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val cleanKey = apiKey.trim()
            val url = "${baseUrl.trim().trimEnd('/')}/models"
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $cleanKey")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(body)
                val data = json.optJSONArray("data") ?: JSONArray()
                val models = mutableListOf<String>()
                for (i in 0 until data.length()) {
                    val model = data.optJSONObject(i)
                    val id = model?.optString("id")
                    if (id != null) {
                        models.add(id)
                    }
                }
                Result.success(models)
            } else {
                Result.failure(Exception("HTTP ${response.code}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun chat(
        config: ApiConfig,
        model: String,
        messages: List<ChatMessage>,
    ): Result<ChatResult> = withContext(Dispatchers.IO) {
        try {
            val isAnthropic = config.protocol == "anthropic"

            // Anthropic Messages API
            if (isAnthropic) {
                val url = "${config.baseUrl.trim().trimEnd('/')}/messages"

                val messagesArray = JSONArray()
                for (msg in messages) {
                    messagesArray.put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }

                // Extract system message if present
                var systemPrompt = config.systemPrompt
                val nonSystemMessages = JSONArray()
                for (i in 0 until messagesArray.length()) {
                    val msg = messagesArray.getJSONObject(i)
                    if (msg.getString("role") == "system") {
                        systemPrompt = msg.getString("content")
                    } else {
                        nonSystemMessages.put(msg)
                    }
                }

                val bodyObj = JSONObject().apply {
                    put("model", model)
                    put("messages", nonSystemMessages)
                    put("max_tokens", 4096)
                    if (systemPrompt.isNotEmpty()) {
                        put("system", systemPrompt)
                    }
                }.toString()

                val request = Request.Builder()
                    .url(url)
                    .addHeader("x-api-key", config.apiKey.trim())
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("Content-Type", "application/json")
                    .post(bodyObj.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    val raw = json.optJSONArray("content")
                        ?.optJSONObject(0)
                        ?.opt("text")
                    val content = if (raw != null && raw != JSONObject.NULL) raw.toString() else "No response"
                    val usage = json.optJSONObject("usage")?.let { u ->
                        TokenUsage(
                            promptTokens = u.optInt("input_tokens", 0),
                            completionTokens = u.optInt("output_tokens", 0),
                            totalTokens = u.optInt("input_tokens", 0) + u.optInt("output_tokens", 0),
                        )
                    }
                    Result.success(ChatResult(content, usage))
                } else {
                    Result.failure(Exception("HTTP ${response.code}: $responseBody"))
                }
            } else {
                // OpenAI-compatible /chat/completions
                val url = "${config.baseUrl.trim().trimEnd('/')}/chat/completions"

                val messagesArray = JSONArray()
                // Prepend system prompt if configured and no system message exists
                val hasSystem = messages.any { it.role == "system" }
                if (config.systemPrompt.isNotEmpty() && !hasSystem) {
                    messagesArray.put(JSONObject().apply {
                        put("role", "system")
                        put("content", config.systemPrompt)
                    })
                }
                for (msg in messages) {
                    messagesArray.put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }

                val body = JSONObject().apply {
                    put("model", model)
                    put("messages", messagesArray)
                    put("temperature", 0.7)
                    put("max_tokens", 4096)
                }.toString()

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer ${config.apiKey.trim()}")
                    .addHeader("Content-Type", "application/json")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    val choices = json.optJSONArray("choices")
                    val raw = choices?.optJSONObject(0)
                        ?.optJSONObject("message")
                        ?.opt("content")
                    val content = if (raw != null && raw != JSONObject.NULL) raw.toString() else "No response"
                    val usage = json.optJSONObject("usage")?.let { u ->
                        TokenUsage(
                            promptTokens = u.optInt("prompt_tokens", 0),
                            completionTokens = u.optInt("completion_tokens", 0),
                            totalTokens = u.optInt("total_tokens", 0),
                        )
                    }
                    Result.success(ChatResult(content, usage))
                } else {
                    Result.failure(Exception("HTTP ${response.code}: $responseBody"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Streaming version of chat — emits text chunks as they arrive via SSE.
     * Returns a Flow<String> where each emission is a text delta.
     */
    fun chatStream(
        config: ApiConfig,
        model: String,
        messages: List<ChatMessage>,
    ): Flow<String> = callbackFlow {
        val job = launch(Dispatchers.IO) {
            try {
                val isAnthropic = config.protocol == "anthropic"

                if (isAnthropic) {
                    // Anthropic streaming
                    val url = "${config.baseUrl.trim().trimEnd('/')}/messages"
                    val messagesArray = JSONArray()
                    for (msg in messages) {
                        messagesArray.put(JSONObject().apply {
                            put("role", msg.role)
                            put("content", msg.content)
                        })
                    }
                    var systemPrompt = config.systemPrompt
                    val nonSystemMessages = JSONArray()
                    for (i in 0 until messagesArray.length()) {
                        val msg = messagesArray.getJSONObject(i)
                        if (msg.getString("role") == "system") {
                            systemPrompt = msg.getString("content")
                        } else {
                            nonSystemMessages.put(msg)
                        }
                    }
                    val bodyObj = JSONObject().apply {
                        put("model", model)
                        put("messages", nonSystemMessages)
                        put("max_tokens", 4096)
                        put("stream", true)
                        if (systemPrompt.isNotEmpty()) put("system", systemPrompt)
                    }.toString()

                    val request = Request.Builder()
                        .url(url)
                        .addHeader("x-api-key", config.apiKey.trim())
                        .addHeader("anthropic-version", "2023-06-01")
                        .addHeader("Content-Type", "application/json")
                        .post(bodyObj.toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = client.newCall(request).execute()
                    val reader = response.body?.byteStream()?.bufferedReader() ?: run {
                        close()
                        return@launch
                    }
                    reader.use { br ->
                        parseAnthropicSSE(br) { chunk -> trySend(chunk) }
                    }
                } else {
                    // OpenAI-compatible streaming
                    val url = "${config.baseUrl.trim().trimEnd('/')}/chat/completions"
                    val messagesArray = JSONArray()
                    val hasSystem = messages.any { it.role == "system" }
                    if (config.systemPrompt.isNotEmpty() && !hasSystem) {
                        messagesArray.put(JSONObject().apply {
                            put("role", "system")
                            put("content", config.systemPrompt)
                        })
                    }
                    for (msg in messages) {
                        messagesArray.put(JSONObject().apply {
                            put("role", msg.role)
                            put("content", msg.content)
                        })
                    }
                    val body = JSONObject().apply {
                        put("model", model)
                        put("messages", messagesArray)
                        put("temperature", 0.7)
                        put("max_tokens", 4096)
                        put("stream", true)
                    }.toString()

                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer ${config.apiKey.trim()}")
                        .addHeader("Content-Type", "application/json")
                        .post(body.toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = client.newCall(request).execute()
                    val reader = response.body?.byteStream()?.bufferedReader() ?: run {
                        close()
                        return@launch
                    }
                    reader.use { br ->
                        parseOpenAISSE(br) { chunk -> trySend(chunk) }
                    }
                }
            } catch (e: Exception) {
                close(e)
            } finally {
                close()
            }
        }
        awaitClose { job.cancel() }
    }

    private fun parseOpenAISSE(reader: BufferedReader, onChunk: (String) -> Unit) {
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val l = line ?: continue
            if (!l.startsWith("data: ")) continue
            val data = l.removePrefix("data: ").trim()
            if (data == "[DONE]") break
            try {
                val json = JSONObject(data)
                val delta = json.optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("delta")
                // optString returns literal "null" for JSON null values — use opt() + toString check
                val raw = delta?.opt("content")
                val content = if (raw != null && raw != JSONObject.NULL) raw.toString() else null
                if (!content.isNullOrEmpty()) {
                    onChunk(content)
                }
            } catch (_: Exception) {
                // Skip malformed JSON lines
            }
        }
    }

    private fun parseAnthropicSSE(reader: BufferedReader, onChunk: (String) -> Unit) {
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val l = line ?: continue
            if (l.startsWith("event: ")) continue // skip event type lines
            if (!l.startsWith("data: ")) continue
            val data = l.removePrefix("data: ").trim()
            try {
                val json = JSONObject(data)
                val type = json.optString("type", "")
                when (type) {
                    "content_block_delta" -> {
                        val raw = json.optJSONObject("delta")?.opt("text")
                        val text = if (raw != null && raw != JSONObject.NULL) raw.toString() else null
                        if (!text.isNullOrEmpty()) {
                            onChunk(text)
                        }
                    }
                    "message_stop", "message_end" -> break
                }
            } catch (_: Exception) {
                // Skip malformed JSON lines
            }
        }
    }
}
