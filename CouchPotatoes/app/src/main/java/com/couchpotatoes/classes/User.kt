package com.couchpotatoes.classes

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val username: String? = null,
    val email: String? = null,
    val role: String? = null,
    val address: String? = null,
    val rating: Double? = null,
    val paymentInfo: String? = null,
    val currentJob: String? = null,
    val currentRequests: List<String>? = null,
    ) {
}