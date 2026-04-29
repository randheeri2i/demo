package com.pinmyhome.app.network.repositories

import android.content.Context
import com.pinmyhome.app.network.ApiClient
import com.pinmyhome.app.network.ApiEndpoints
import com.pinmyhome.app.network.ApiResult
import org.json.JSONArray
import org.json.JSONObject

object DemandsRepository {
    suspend fun getAll(context: Context): ApiResult<JSONObject> =
        ApiClient.get(context, ApiEndpoints.Demands.LIST)

    suspend fun getById(context: Context, id: String): ApiResult<JSONObject> =
        ApiClient.get(context, ApiEndpoints.Demands.detail(id))

    suspend fun create(
        context: Context,
        buyerName: String,
        buyerPhone: String,
        flatType: String,
        budgetMin: Long,
        budgetMax: Long,
        areaId: Int? = null,
        isExclusive: Boolean = false,
        preferredSocietyIds: List<Int> = emptyList()
    ): ApiResult<JSONObject> {
        val body = JSONObject().apply {
            put("buyer_name", buyerName)
            put("buyer_phone", buyerPhone)
            put("flat_type", flatType)
            put("budget_min", budgetMin)
            put("budget_max", budgetMax)
            put("is_exclusive", isExclusive)
            areaId?.let { put("area_id", it) }
            if (preferredSocietyIds.isNotEmpty()) {
                put(
                    "preferred_society_ids",
                    JSONArray().apply { preferredSocietyIds.forEach { put(it) } }
                )
            }
        }
        return ApiClient.post(context, ApiEndpoints.Demands.CREATE, body)
    }
}
