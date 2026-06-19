package io.kess.ecommerce.model

data class ProductDetail(
    val product: Product = Product(),
    val variant: List<ProductVariant> = emptyList()

)

