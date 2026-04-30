package com.pinmyhome.app.ui.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object AuthKeys {
    val TOKEN = stringPreferencesKey("auth_token")
    val PHONE = stringPreferencesKey("phone")
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_ROLE = stringPreferencesKey("user_role")
    val USER_ID = stringPreferencesKey("user_id")
}

suspend fun saveAuthSession(
    context: Context,
    token: String,
    phone: String,
    name: String = "",
    role: String = "",
    userId: String = ""
) {
    context.dataStore.edit { prefs ->
        prefs[AuthKeys.TOKEN] = token
        prefs[AuthKeys.PHONE] = phone
        prefs[AuthKeys.USER_NAME] = name
        prefs[AuthKeys.USER_ROLE] = role
        prefs[AuthKeys.USER_ID] = userId
    }
}

suspend fun getAuthToken(context: Context): String? =
    context.dataStore.data.first()[AuthKeys.TOKEN]

suspend fun getSavedPhone(context: Context): String? =
    context.dataStore.data.first()[AuthKeys.PHONE]

suspend fun getSavedUserName(context: Context): String? =
    context.dataStore.data.first()[AuthKeys.USER_NAME]

suspend fun getSavedUserRole(context: Context): String? =
    context.dataStore.data.first()[AuthKeys.USER_ROLE]

suspend fun getSavedUserId(context: Context): String? =
    context.dataStore.data.first()[AuthKeys.USER_ID]

suspend fun clearAuthSession(context: Context) {
    context.dataStore.edit { it.clear() }
}
