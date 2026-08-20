package com.pinmyhome.app.network.repositories

import android.content.Context
import android.net.Uri
import com.pinmyhome.app.network.ApiClient
import com.pinmyhome.app.network.ApiEndpoints
import com.pinmyhome.app.network.ApiResult
import org.json.JSONArray
import org.json.JSONObject

object PropertyRepository {

    suspend fun getAll(
        context: Context,
        societyId: Int? = null,
        status: String? = null
    ): ApiResult<JSONObject> {
        val params = buildMap {
            societyId?.let { put("society_id", it.toString()) }
            status?.let { put("status", it) }
        }
        return ApiClient.get(context, ApiEndpoints.Properties.LIST, params)
    }

    suspend fun create(context: Context, body: JSONObject): ApiResult<JSONObject> =
        ApiClient.post(context, ApiEndpoints.Properties.CREATE, body)

    /**
     * Attach photos to an existing property.
     * - [remoteUrls]: public https URLs sent as JSON (when no local files).
     * - [localUris]: device content/file URIs uploaded as multipart.
     */
    suspend fun addPhotos(
        context: Context,
        propertyId: String,
        remoteUrls: List<String> = emptyList(),
        localUris: List<Uri> = emptyList()
    ): ApiResult<JSONObject> {
        if (localUris.isNotEmpty()) {
            val textFields = if (remoteUrls.isNotEmpty()) {
                mapOf("photo_urls" to JSONArray(remoteUrls).toString())
            } else emptyMap()
            return ApiClient.postMultipart(
                context = context,
                endpoint = ApiEndpoints.Properties.addPhotos(propertyId),
                fileUris = localUris,
                fileField = "photos",
                textFields = textFields
            )
        }
        if (remoteUrls.isEmpty()) return ApiResult.Success(JSONObject())
        val body = JSONObject().apply {
            put("photos", JSONArray().apply { remoteUrls.forEach { put(it) } })
        }
        return ApiClient.post(context, ApiEndpoints.Properties.addPhotos(propertyId), body)
    }

    suspend fun deletePhoto(
        context: Context,
        propertyId: String,
        photoId: String
    ): ApiResult<JSONObject> =
        ApiClient.delete(context, ApiEndpoints.Properties.deletePhoto(propertyId, photoId))
}
