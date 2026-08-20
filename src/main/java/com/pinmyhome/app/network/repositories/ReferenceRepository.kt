package com.pinmyhome.app.network.repositories

import android.content.Context
import com.pinmyhome.app.network.ApiClient
import com.pinmyhome.app.network.ApiEndpoints
import com.pinmyhome.app.network.ApiResult
import org.json.JSONObject

object ReferenceRepository {

    suspend fun getAreas(context: Context): ApiResult<JSONObject> =
        ApiClient.get(context, ApiEndpoints.Reference.AREAS)

    suspend fun getSocieties(context: Context, areaId: Int): ApiResult<JSONObject> =
        ApiClient.get(
            context,
            ApiEndpoints.Reference.SOCIETIES,
            mapOf("area_id" to areaId.toString())
        )

    suspend fun getSocietyConfigurations(
        context: Context,
        societyId: String
    ): ApiResult<JSONObject> =
        ApiClient.get(context, ApiEndpoints.Reference.societyConfigurations(societyId))
}
