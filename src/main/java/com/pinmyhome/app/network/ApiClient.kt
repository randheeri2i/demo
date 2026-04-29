package com.pinmyhome.app.network

import android.content.Context
import com.pinmyhome.app.ui.auth.getAuthToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ApiConfig {
    const val BASE_URL = "https://c4eb30ec-60b2-422d-a195-7eaf4cadfed4-00-3aehvqsytvlv9.janeway.replit.dev/api"
    const val TIMEOUT_MS = 15_000
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = -1) : ApiResult<Nothing>()
}

object ApiClient {

    suspend fun publicPost(endpoint: String, body: JSONObject): ApiResult<JSONObject> =
        request("POST", endpoint, body, token = null)

    suspend fun publicGet(
        endpoint: String,
        params: Map<String, String> = emptyMap()
    ): ApiResult<JSONObject> =
        request("GET", endpoint, null, token = null, queryParams = params)

    suspend fun get(
        context: Context,
        endpoint: String,
        params: Map<String, String> = emptyMap()
    ): ApiResult<JSONObject> =
        request("GET", endpoint, null, getAuthToken(context), queryParams = params)

    suspend fun post(context: Context, endpoint: String, body: JSONObject): ApiResult<JSONObject> =
        request("POST", endpoint, body, getAuthToken(context))

    suspend fun patch(context: Context, endpoint: String, body: JSONObject): ApiResult<JSONObject> =
        request("PATCH", endpoint, body, getAuthToken(context))

    suspend fun put(context: Context, endpoint: String, body: JSONObject): ApiResult<JSONObject> =
        request("PUT", endpoint, body, getAuthToken(context))

    suspend fun delete(context: Context, endpoint: String): ApiResult<JSONObject> =
        request("DELETE", endpoint, null, getAuthToken(context))

    private suspend fun request(
        method: String,
        endpoint: String,
        body: JSONObject?,
        token: String?,
        queryParams: Map<String, String> = emptyMap()
    ): ApiResult<JSONObject> = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            conn = (URL(buildUrl(endpoint, queryParams)).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = ApiConfig.TIMEOUT_MS
                readTimeout = ApiConfig.TIMEOUT_MS
                if (body != null && method != "GET") {
                    doOutput = true
                    outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                }
            }

            val code = conn.responseCode
            val text = if (code in 200..299) {
                conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
            } else {
                conn.errorStream?.bufferedReader(Charsets.UTF_8)?.readText() ?: "{}"
            }

            if (code in 200..299) {
                ApiResult.Success(normalise(text))
            } else {
                val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
                val msg = json.optString("error").ifBlank { json.optString("message") }
                    .ifBlank { "Request failed (HTTP $code)" }
                ApiResult.Error(msg, code)
            }
        } catch (e: java.net.SocketTimeoutException) {
            ApiResult.Error("Connection timed out. Please check your internet.")
        } catch (e: java.net.UnknownHostException) {
            ApiResult.Error("No internet connection.")
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message ?: e.javaClass.simpleName}")
        } finally {
            conn?.disconnect()
        }
    }

    fun normalise(text: String): JSONObject {
        val trimmed = text.trim()
        return when {
            trimmed.startsWith("{") -> runCatching { JSONObject(trimmed) }.getOrElse { JSONObject() }
            trimmed.startsWith("[") -> {
                val arr = runCatching { JSONArray(trimmed) }.getOrElse { JSONArray() }
                JSONObject().apply { put("data", arr) }
            }
            else -> JSONObject()
        }
    }

    private fun buildUrl(endpoint: String, params: Map<String, String>): String {
        if (params.isEmpty()) return "${ApiConfig.BASE_URL}$endpoint"
        val query = params.entries.joinToString("&") {
            "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
        return "${ApiConfig.BASE_URL}$endpoint?$query"
    }
}
