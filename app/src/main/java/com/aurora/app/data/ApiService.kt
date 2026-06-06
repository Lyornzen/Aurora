package com.aurora.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ApiConfig(
    val id: String,
    val name: String,
    val apiKey: String,
    val baseUrl: String,
    val models: List<String> = emptyList(),
    val enabled: Boolean = true,
)

data class ChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val model: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)

object ApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val configs = mutableListOf<ApiConfig>()

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
                val config = ApiConfig(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    apiKey = obj.getString("apiKey"),
                    baseUrl = obj.getString("baseUrl"),
                    models = models,
                    enabled = obj.optBoolean("enabled", true),
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
    }

    fun removeConfig(id: String) {
        configs.removeAll { it.id == id }
        saveConfigs()
    }

    fun getEnabledConfigs(): List<ApiConfig> = configs.filter { it.enabled && it.apiKey.isNotBlank() }

    fun getAllModels(): List<Pair<String, String>> =
        configs.filter { it.enabled }.flatMap { c -> c.models.map { it to "${c.name} / $it" } }

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
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "${config.baseUrl.trim().trimEnd('/')}/chat/completions"

            val messagesArray = JSONArray()
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
                val content = choices?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content") ?: "No response"
                Result.success(content)
            } else {
                Result.failure(Exception("HTTP ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
