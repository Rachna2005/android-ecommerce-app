package io.kess.ecommerce.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Shop(
    @get:Exclude
    var id: String = "",
    val ownerId: String = "",
    val shopName: String = "",
    val description: String = "",
    val phone: String = "",
    val address: String = "",
    val logoUrl: String = "",
    val isActive: Boolean = true,
    val createAt: Timestamp? = null
)