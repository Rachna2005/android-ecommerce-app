package io.kess.ecommerce.model

data class ShopCartGroup(
    val shopId: String,
    val shopName: String,
    val items: List<CartItem>
)