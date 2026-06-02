package io.kess.ecommerce.model

fun cartToOrderItem(cartItem: CartItem): OrderItem {
    return OrderItem(
        productId = cartItem.productId, variantId = cartItem.variantId,
        name = cartItem.name,
        quantity = cartItem.quantity,
        image = cartItem.image,
        selectorColor = cartItem.selectorColor,
        selectSize = cartItem.selectSize,
        price = cartItem.price
    )
}