package com.pinmyhome.app.network.models

data class AuthSession(
    val token: String,
    val user: User
)
