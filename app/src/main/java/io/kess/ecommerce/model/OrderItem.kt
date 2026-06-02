package io.kess.ecommerce.model

import com.google.firebase.firestore.Exclude

data class OrderItem(
    @get:Exclude
    var id: String = "",
    val productId: String = "",
    val variantId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val image: String = "",
    val selectorColor: String = "",
    val selectSize: String = "",
    val price: Double = 0.0,
)