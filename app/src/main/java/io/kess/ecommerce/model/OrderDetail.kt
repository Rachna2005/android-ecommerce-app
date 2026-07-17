package io.kess.ecommerce.model

data class OrderDetail(
    val order: Order = Order(),
    val orderItem: List<OrderItem> = emptyList()

)