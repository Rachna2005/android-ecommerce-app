package io.kess.ecommerce.model

data class ProductFilter(
    val categoryId: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val shopId: String? = null
)