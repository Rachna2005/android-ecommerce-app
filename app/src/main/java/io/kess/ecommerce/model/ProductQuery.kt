package io.kess.ecommerce.model

import io.kess.ecommerce.repository.ProductDisplayType

data class ProductQuery(
    val displayType: ProductDisplayType = ProductDisplayType.ALL,
    val categoryId: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val shopId: String? = null
)