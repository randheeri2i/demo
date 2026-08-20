package com.pinmyhome.app.network

/**
 * Single source of truth for every API endpoint in the app.
 * Base URL lives in ApiConfig — only paths go here.
 */
object ApiEndpoints {

    object Auth {
        const val SIGNUP = "/auth/signup"
        const val LOGIN = "/auth/login"
        const val ME = "/auth/me"
        const val LOGOUT = "/auth/logout"
    }

    object Properties {
        const val LIST = "/properties"
        const val CREATE = "/properties"
        const val CHECK_FLAT = "/properties/check-flat"
        fun detail(id: String) = "/properties/$id"
        fun update(id: String) = "/properties/$id"
        fun updateStatus(id: String) = "/properties/$id/status"
        fun addPhotos(id: String) = "/properties/$id/photos"
        fun deletePhoto(id: String, photoId: String) = "/properties/$id/photos/$photoId"
        fun setPrimaryPhoto(id: String, photoId: String) = "/properties/$id/photos/$photoId/primary"
    }

    object Reference {
        const val AREAS = "/areas"
        const val SOCIETIES = "/societies"
        const val AMENITIES = "/amenities"
        fun societyConfigurations(societyId: String) = "/societies/$societyId/configurations"
        fun societyPhotos(societyId: String) = "/societies/$societyId/photos"
    }
}
