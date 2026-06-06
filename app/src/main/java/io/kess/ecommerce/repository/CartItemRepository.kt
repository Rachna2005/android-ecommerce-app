package io.kess.ecommerce.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.util.UserSession

class CartItemRepository {
    val fireStore = FirebaseFirestore.getInstance()

    //        val userId = UserSession.currentUser!!.id
    private fun getUserId(): String? {
        return UserSession.currentUser?.id
    }

    fun getAllCart(onResult: (List<CartItem>) -> Unit) {
        val userId = getUserId()
        if (userId == null) {
            Log.d("User", "No user")
            return
        }
        fireStore.collection("users").document(userId).collection("cart").get()
            .addOnSuccessListener { result ->
                val cartList = result.documents.mapNotNull { doc ->
                    doc.toObject(CartItem::class.java)?.apply {
                        id = doc.id
                    }
                }
                onResult(cartList)
            }.addOnCanceledListener {
                onResult(emptyList())
            }
    }

    fun addCart(cartItem: CartItem, onResult: (String) -> Unit) {
        val cartId = "${cartItem.productId}_${cartItem.variantId}"
        val userId = getUserId()
        if (userId == null) {
            Log.d("User", "No user")
            return
        }

        val findCart =
            fireStore.collection("users").document(userId).collection("cart").document(cartId)
        findCart.get().addOnSuccessListener { doc ->
            val currentQty = doc.getLong("quantity")?.toInt() ?: 0
            if (doc.exists()) {
                findCart.update("quantity", currentQty + cartItem.quantity)
                onResult("Product cart have been updated")
            } else {
                findCart.set(cartItem.copy(id = cartId))
                    .addOnSuccessListener {
                        onResult("Product added to cart")
                        Log.d("CART_DEBUG", "Product added")
                    }
                    .addOnFailureListener {
                        onResult("Failed to add product")
                        Log.d("CART_DEBUG", "Product added Failed")
                    }
            }
        }.addOnFailureListener {
            onResult("Something went wrong")
        }
    }

    fun deleteCart(cartId: String) {
        val userId = getUserId()
        if (userId == null) {
            Log.d("User", "No user")
            return
        }
        fireStore.collection("users").document(userId).collection("cart").document(cartId).delete()
            .addOnFailureListener {
                Log.d("Delete_Cart_Firebase", "Failed")
            }
    }

    fun increaseQuantity(
        cartId: String
    ) {
        val userId = getUserId()
        if (userId == null) {
            Log.d("User", "No user")
            return
        }

        fireStore
            .collection("users")
            .document(userId)
            .collection("cart")
            .document(cartId)
            .update(
                "quantity",
                FieldValue.increment(1)
            )
            .addOnFailureListener {

                Log.e(
                    "FIREBASE_CART",
                    "Failed to increase quantity"
                )
            }
    }

    fun decreaseQuantity(
        cartId: String,
        currentQuantity: Int
    ) {
        val userId = getUserId()
        if (userId == null) {
            Log.d("User", "No user")
            return
        }
        val cartRef =
            fireStore
                .collection("users")
                .document(userId)
                .collection("cart")
                .document(cartId)

        cartRef.update(
            "quantity",
            FieldValue.increment(-1)
        )
    }

}