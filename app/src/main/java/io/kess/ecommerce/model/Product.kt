package io.kess.ecommerce.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Product(
    @get:Exclude
    var id: String = "",
    val shopId: String = "",
    val image: String = "",
    val name: String = "",
    val categoryId: String = "",
    val price: Double = 0.0,
    val discountPercentage: Double? = null,
    val description: String = "",
    val status: String = "ACTIVE",
    val totalStock: Int = 0,
    val createdAt: Timestamp? = null
)

