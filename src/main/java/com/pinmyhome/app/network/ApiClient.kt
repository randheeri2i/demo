package com.pinmyhome.app.network

import android.content.Context
import android.net.Uri
import com.pinmyhome.app.ui.auth.getAuthToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

object ApiConfig {
    const val BASE_URL = "https://c4eb30ec-60b2-422d-a195-7eaf4cadfed4-00-3aehvqsytvlv9.janeway.replit.dev/api"
    const val TIMEOUT_MS = 30_000
}

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = -1) : ApiResult<Nothing>()
}

object ApiClient {

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

    /**
     * Multipart upload for property photos.
     * Sends each local file under the field name [fileField] (default "photos").
     * Optional remote URL strings can be sent as repeated "photo_urls" text parts.
     */
    suspend fun postMultipart(
        context: Context,
        endpoint: String,
        fileUris: List<Uri>,
        fileField: String = "photos",
        textFields: Map<String, String> = emptyMap()
    ): ApiResult<JSONObject> = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val boundary = "----PinMyHomeBoundary${UUID.randomUUID()}"
            val token = getAuthToken(context)
            conn = (URL("${ApiConfig.BASE_URL}$endpoint").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = ApiConfig.TIMEOUT_MS
                readTimeout = ApiConfig.TIMEOUT_MS
            }

            DataOutputStream(conn.outputStream).use { out ->
                textFields.forEach { (key, value) ->
                    out.writeBytes("--$boundary\r\n")
                    out.writeBytes("Content-Disposition: form-data; name=\"$key\"\r\n\r\n")
                    out.writeBytes("$value\r\n")
                }

                fileUris.forEachIndexed { index, uri ->
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val fileName = guessFileName(uri, index, mime)
                    out.writeBytes("--$boundary\r\n")
                    out.writeBytes(
                        "Content-Disposition: form-data; name=\"$fileField\"; filename=\"$fileName\"\r\n"
                    )
                    out.writeBytes("Content-Type: $mime\r\n\r\n")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        input.copyTo(out)
                    } ?: return@withContext ApiResult.Error("Could not read selected photo")
                    out.writeBytes("\r\n")
                }

                out.writeBytes("--$boundary--\r\n")
                out.flush()
            }

            readResponse(conn)
        } catch (e: java.net.SocketTimeoutException) {
            ApiResult.Error("Connection timed out. Please check your internet.")
        } catch (e: java.net.UnknownHostException) {
            ApiResult.Error("No internet connection.")
        } catch (e: Exception) {
            ApiResult.Error("Upload failed: ${e.message ?: e.javaClass.simpleName}")
        } finally {
            conn?.disconnect()
        }
    }

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
            readResponse(conn)
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

    private fun readResponse(conn: HttpURLConnection): ApiResult<JSONObject> {
        val code = conn.responseCode
        val text = if (code in 200..299) {
            conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
        } else {
            conn.errorStream?.bufferedReader(Charsets.UTF_8)?.readText() ?: "{}"
        }
        return if (code in 200..299) {
            ApiResult.Success(normalise(text))
        } else {
            val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
            val msg = json.optString("error").ifBlank { json.optString("message") }
                .ifBlank { "Request failed (HTTP $code)" }
            ApiResult.Error(msg, code)
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

    private fun guessFileName(uri: Uri, index: Int, mime: String): String {
        val ext = when {
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            mime.contains("heic") -> "heic"
            else -> "jpg"
        }
        val last = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.contains('.') }
        return last ?: "photo_${index + 1}.$ext"
    }
}
