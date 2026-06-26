package io.kess.ecommerce.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Order(
    @get:Exclude
    var id: String = "",
    val userId: String = "",
    val shopId: String = "",
    val shopName: String = "",
    val totalPrice: Double = 0.0,
    val totalQuantity: Int = 0,
    val status: String = "PENDING",
    var previewImages: List<String> = emptyList(),
    val address: String = "",
    val phoneNumber: String = "",
    val paymentMethod: String = "",
    val createdAt: Timestamp? = null
)