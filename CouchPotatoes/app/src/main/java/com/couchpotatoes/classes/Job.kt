package com.couchpotatoes.classes

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Job(
    val requesterName: String? = null,
    val requesterEmail: String? = null,
    // only stores one item at a time for now
    // can update to be a map that contains multiple items
    val item: String? = null,
    val price: String? = null,
    val store: String? = null,
    val deliveryAddress: String? = null,
    val status: String? = null
) {
}