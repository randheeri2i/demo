package com.pinmyhome.app.network.repositories

import android.content.Context
import com.pinmyhome.app.network.ApiClient
import com.pinmyhome.app.network.ApiEndpoints
import com.pinmyhome.app.network.ApiResult
import org.json.JSONObject

object BrokerRepository {
    suspend fun createInvite(
        context: Context,
        name: String = "",
        phone: String = "",
        email: String = ""
    ): ApiResult<JSONObject> {
        val body = JSONObject().apply {
            if (name.isNotBlank()) put("invited_name", name)
            if (phone.isNotBlank()) put("invited_phone", phone)
            if (email.isNotBlank()) put("invited_email", email)
        }
        return ApiClient.post(context, ApiEndpoints.Broker.CREATE_INVITE, body)
    }

    suspend fun getInvites(context: Context): ApiResult<JSONObject> =
        ApiClient.get(context, ApiEndpoints.Broker.LIST_INVITES)

    suspend fun validateInvite(token: String): ApiResult<JSONObject> =
        ApiClient.publicGet(ApiEndpoints.Broker.validateInvite(token))

    suspend fun getSubBrokers(context: Context): ApiResult<JSONObject> =
        ApiClient.get(context, ApiEndpoints.Broker.SUB_BROKERS)

    suspend fun getMyBuyers(context: Context): ApiResult<JSONObject> =
        ApiClient.get(context, ApiEndpoints.Broker.MY_BUYERS)
}
