package io.kess.ecommerce.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.CartItem

class CartItemRepository(private val authRepo: AuthRepository) {
    val fireStore = FirebaseFirestore.getInstance()

    fun getAllCart(onResult: (List<CartItem>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = authRepo.getUserId()
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
            }.addOnFailureListener {e ->
                onFailure(e)
            }
    }

    fun addCart(cartItem: CartItem, onResult: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val cartId = "${cartItem.productId}_${cartItem.variantId}"
        val userId = authRepo.getUserId()
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
//                        onResult("Failed to add product")
                        onFailure(it)
                        Log.d("CART_DEBUG", "Product added Failed")
                    }
            }
        }.addOnFailureListener {
            onFailure(it)
        }
    }

    fun deleteCart(cartId: String, onResult: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = authRepo.getUserId()
        if (userId == null) {
            Log.d("User", "No user")
            return
        }
        fireStore.collection("users").document(userId).collection("cart").document(cartId).delete()
            .addOnSuccessListener {
                onResult("Cart delete successfully")
            }.addOnFailureListener {
                onFailure(it)
            }
    }

    fun increaseQuantity(
        cartId: String,onResult: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {
        val userId = authRepo.getUserId()
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
            ).addOnSuccessListener {
                onResult("Quantity increased")
            }
            .addOnFailureListener {
                onFailure(it)
                Log.e(
                    "FIREBASE_CART",
                    "Failed to increase quantity"
                )
            }
    }

    fun decreaseQuantity(
        cartId: String, onResult: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {
        val userId = authRepo.getUserId()
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
        ).addOnSuccessListener {
            onResult("Cart decreased")
        }.addOnFailureListener {
            onFailure(it)
        }
    }

}