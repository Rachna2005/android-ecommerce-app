package io.kess.ecommerce.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Review(
    @get:Exclude
    var id: String = "",
    val userId: String = "",
    val username: String = "",
    val rating: Int = 0,
    val review: String = "",
    val createdAt: Timestamp? = null
)