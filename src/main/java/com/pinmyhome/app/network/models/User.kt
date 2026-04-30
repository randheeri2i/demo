package com.pinmyhome.app.network.models

import org.json.JSONObject

data class BrokerInfo(
    val id: Int = 0,
    val kycStatus: String = "",
    val isIndependent: Boolean = true,
    val parentBrokerId: Int? = null,
    val panNumber: String = "",
    val accountType: String = ""
) {
    companion object {
        fun fromJson(json: JSONObject): BrokerInfo {
            val isIndependent = json.optBoolean("is_independent", true)
            val rawAccountType = json.optString("account_type")
            val accountType = rawAccountType.ifBlank {
                if (isIndependent) "Independent Broker" else "Sub Broker"
            }
            return BrokerInfo(
                id = json.optInt("id", 0),
                kycStatus = json.optString("kyc_status"),
                isIndependent = isIndependent,
                parentBrokerId = json.optInt("parent_broker_id").takeIf { it != 0 },
                panNumber = json.optString("pan_number"),
                accountType = accountType
            )
        }
    }
}

data class User(
    val id: Int = 0,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: String = "",
    val isActive: Boolean = true,
    val adminApproved: String? = null,
    val panNumber: String = "",
    val createdAt: String = "",
    val broker: BrokerInfo? = null
) {
    companion object {
        fun fromJson(json: JSONObject): User {
            val brokerJson = json.optJSONObject("broker")
            val broker = brokerJson?.let { BrokerInfo.fromJson(it) }
            return User(
                id = json.optInt("id", 0),
                name = json.optString("name"),
                phone = json.optString("phone"),
                email = json.optString("email"),
                role = json.optString("role"),
                isActive = json.optBoolean("is_active", true),
                adminApproved = json.opt("admin_approved")?.toString(),
                panNumber = json.optString("pan_number").ifBlank { broker?.panNumber.orEmpty() },
                createdAt = json.optString("created_at"),
                broker = broker
            )
        }
    }
}
