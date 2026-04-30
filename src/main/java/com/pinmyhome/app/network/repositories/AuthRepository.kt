package com.pinmyhome.app.network.repositories

import android.content.Context
import com.pinmyhome.app.network.ApiClient
import com.pinmyhome.app.network.ApiEndpoints
import com.pinmyhome.app.network.ApiResult
import com.pinmyhome.app.network.models.AuthSession
import com.pinmyhome.app.network.models.User
import com.pinmyhome.app.ui.auth.clearAuthSession
import com.pinmyhome.app.ui.auth.saveAuthSession
import org.json.JSONObject

object AuthRepository {

    suspend fun login(
        context: Context,
        phone: String,
        password: String
    ): ApiResult<AuthSession> {
        val body = JSONObject().apply {
            put("phone", phone)
            put("password", password)
        }

        return when (val result = ApiClient.publicPost(ApiEndpoints.Auth.LOGIN, body)) {
            is ApiResult.Success -> {
                val json = result.data
                val token = json.optString("token")
                val user = User.fromJson(json.optJSONObject("user") ?: JSONObject())

                saveAuthSession(
                    context = context,
                    token = token,
                    phone = user.phone,
                    name = user.name,
                    role = user.role,
                    userId = user.id.toString()
                )

                ApiResult.Success(AuthSession(token = token, user = user))
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun register(
        context: Context,
        name: String,
        phone: String,
        email: String,
        password: String,
        panNumber: String = "",
        inviteToken: String = ""
    ): ApiResult<AuthSession> {
        val body = JSONObject().apply {
            put("name", name)
            put("phone", phone)
            put("email", email)
            put("password", password)
            if (panNumber.isNotBlank()) put("pan_number", panNumber)
            if (inviteToken.isNotBlank()) put("invite_token", inviteToken)
        }

        return when (val result = ApiClient.publicPost(ApiEndpoints.Auth.SIGNUP, body)) {
            is ApiResult.Success -> {
                val json = result.data
                val token = json.optString("token")
                val user = User.fromJson(json.optJSONObject("user") ?: JSONObject())

                saveAuthSession(
                    context = context,
                    token = token,
                    phone = user.phone,
                    name = user.name,
                    role = user.role,
                    userId = user.id.toString()
                )

                ApiResult.Success(AuthSession(token = token, user = user))
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun getMe(context: Context): ApiResult<User> {
        return when (val result = ApiClient.get(context, ApiEndpoints.Auth.ME)) {
            is ApiResult.Success -> {
                val payload = result.data
                val userJson = payload.optJSONObject("user")
                    ?: payload.optJSONObject("data")
                    ?: payload
                ApiResult.Success(User.fromJson(userJson))
            }
            is ApiResult.Error -> result
        }
    }

    suspend fun logout(context: Context) {
        runCatching { ApiClient.post(context, ApiEndpoints.Auth.LOGOUT, JSONObject()) }
        clearAuthSession(context)
    }
}
