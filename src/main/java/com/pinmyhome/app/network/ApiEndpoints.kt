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
        fun replaceAmenities(id: String) = "/properties/$id/amenities"
        fun getAmenities(id: String) = "/properties/$id/amenities"
    }

    object Reference {
        const val AREAS = "/areas"
        const val SOCIETIES = "/societies"
        const val AMENITIES = "/amenities"
        fun societyConfigurations(societyId: String) = "/societies/$societyId/configurations"
        fun societyPhotos(societyId: String) = "/societies/$societyId/photos"
    }

    object Dashboard {
        const val SUMMARY = "/dashboard/summary"
        const val RECENT_ACTIVITY = "/dashboard/recent-activity"
        const val PIPELINE = "/dashboard/pipeline"
    }

    object Demands {
        const val LIST = "/demands"
        const val CREATE = "/demands"
        fun detail(id: String) = "/demands/$id"
    }

    object Deals {
        const val LIST = "/deals"
        const val CREATE = "/deals"
        fun detail(id: String) = "/deals/$id"
        fun update(id: String) = "/deals/$id"
    }

    object Visits {
        const val LIST = "/visits"
        const val CREATE = "/visits"
        fun update(id: String) = "/visits/$id"
    }

    object Notifications {
        const val LIST = "/notifications"
        const val UNREAD_COUNT = "/notifications/unread-count"
        const val READ_ALL = "/notifications/read-all"
        fun markRead(id: String) = "/notifications/$id/read"
    }

    object Broker {
        const val CREATE_INVITE = "/broker/invites"
        const val LIST_INVITES = "/broker/invites"
        const val SUB_BROKERS = "/broker/sub-brokers"
        const val MY_BUYERS = "/broker/my-buyers"
        fun validateInvite(token: String) = "/broker/invites/$token"
    }

    object Matches {
        const val GENERATE = "/matches/generate"
        const val LIST = "/matches"
    }

    object BuyerManager {
        const val LIST_BUYERS = "/buyer-manager/buyers"
        const val CREATE_BUYER = "/buyer-manager/buyers"
        const val LIST_BROKERS = "/buyer-manager/brokers"
        fun updateBuyer(id: String) = "/buyer-manager/buyers/$id"
    }

    object PropertyManager {
        const val LIST_PROPERTIES = "/property-manager/properties"
        fun updateProperty(id: String) = "/property-manager/properties/$id"
        fun updateStatus(id: String) = "/property-manager/properties/$id/status"
        fun listPhotos(id: String) = "/property-manager/properties/$id/photos"
        fun addPhotos(id: String) = "/property-manager/properties/$id/photos"
        fun deletePhoto(photoId: String) = "/property-manager/photos/$photoId"
    }

    object BrokerManager {
        const val LIST_PROPERTIES = "/broker-manager/properties"
        const val LIST_ASSIGNMENTS = "/broker-manager/assignments"
        fun propertyAssignments(id: String) = "/broker-manager/properties/$id/assignments"
        fun assignBroker(id: String) = "/broker-manager/properties/$id/assignments"
    }
}
