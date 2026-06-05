package com.aurora.ai.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * API Service — fetch models, test connections.
 * References OpenSeek's ApiService.fetchModels() pattern.
 */
object ApiService {

    data class FetchResult(
        val models: List<String> = emptyList(),
        val error: String? = null,
        val success: Boolean = false,
    )

    /**
     * Fetch available models from an OpenAI-compatible /models endpoint.
     */
    suspend fun fetchModels(baseUrl: String, apiKey: String): FetchResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("${baseUrl.trimEnd('/')}/models")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 15_000
                readTimeout = 15_000
            }

            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().readText()
                val json = org.json.JSONObject(body)
                val data = json.optJSONArray("data") ?: JSONArray()
                val models = mutableListOf<String>()
                for (i in 0 until data.length()) {
                    val model = data.getJSONObject(i).optString("id")
                    if (model.isNotBlank()) models.add(model)
                }
                FetchResult(models = models, success = true)
            } else {
                val errBody = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP ${conn.responseCode}"
                FetchResult(error = errBody.take(200))
            }
        } catch (e: Exception) {
            FetchResult(error = e.message ?: "Network error")
        }
    }

    /**
     * Quick test: check if the API endpoint is reachable with the given key.
     */
    suspend fun testConnection(baseUrl: String, apiKey: String): Boolean {
        val result = fetchModels(baseUrl, apiKey)
        return result.success || result.error?.contains("404") == true // Some APIs return 404 on /models but work for chat
    }
}
