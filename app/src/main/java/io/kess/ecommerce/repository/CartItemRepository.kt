package io.kess.ecommerce.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import io.kess.ecommerce.model.CartItem
import kotlin.collections.remove

class CartItemRepository(private val authRepo: AuthRepository) {
    val fireStore = FirebaseFirestore.getInstance()
    private var cartListener: ListenerRegistration? = null

    private fun requireUserId(
        onFailure: (Exception) -> Unit
    ): String? {
        val userId = authRepo.getUserId()
        if (userId == null) {
            onFailure(Exception("User not logged in"))
            return null
        }
        return userId
    }

    fun getAllCart(onResult: (List<CartItem>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = requireUserId(onFailure) ?: return
        cartListener?.remove()
        cartListener =
            fireStore.collection("users")
                .document(userId)
                .collection("cart")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onFailure(error)
                        return@addSnapshotListener
                    }
                    val carts =
                        snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(CartItem::class.java)?.apply {
                                id = doc.id
                            }
                        } ?: emptyList()
                    onResult(carts)
                }
    }

    fun removeCartItems(
        cartItem: List<CartItem>,
        onResult: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = requireUserId(onFailure) ?: return
        val batch = fireStore.batch()
        cartItem.forEach { item ->
            val cartDoc =
                fireStore.collection("users").document(userId).collection("cart").document(item.id)
            batch.delete(cartDoc)
        }
        batch.commit().addOnSuccessListener {
            onResult()
        }.addOnFailureListener {
            onFailure(it)
        }
    }

    fun addCart(cartItem: CartItem, onResult: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val cartId = "${cartItem.productId}_${cartItem.variantId}"
        val userId = requireUserId(onFailure) ?: return
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
        val userId = requireUserId(onFailure) ?: return
        fireStore.collection("users").document(userId).collection("cart").document(cartId).delete()
            .addOnSuccessListener {
                onResult("Cart delete successfully")
            }.addOnFailureListener {
                onFailure(it)
            }
    }

    fun increaseQuantity(
        cartId: String, onResult: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {
        val userId = requireUserId(onFailure) ?: return
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
        val userId = requireUserId(onFailure) ?: return
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

    fun removeCartListener() {
        cartListener?.remove()
        cartListener = null
    }

}