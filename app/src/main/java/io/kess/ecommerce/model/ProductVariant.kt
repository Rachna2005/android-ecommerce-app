package io.kess.ecommerce.model

import com.google.firebase.firestore.Exclude

data class ProductVariant(
    @get:Exclude
    var id: String = "",
    val color: String = "",
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val size: String = "",
    val stock: Int = 0
)