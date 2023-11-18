package com.couchpotatoes.classes

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable


@IgnoreExtraProperties
data class Job constructor(
    var uid: String? = null,
    var requesterName: String? = null,
    var requesterEmail: String? = null,
    // only stores one item at a time for now
    // can update to be a map that contains multiple items
    var item: String? = null,
    var price: String? = null,
    var store: String? = null,
    var deliveryAddress: String? = null,
    var expirationTime: Long? = null,
    var status: String? = null
) : Serializable {
}