package io.kess.ecommerce.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.model.OrderItem
import io.kess.ecommerce.model.cartToOrderItem

class OrderRepository(private val authRepo: AuthRepository) {
    val fireStore = FirebaseFirestore.getInstance()
    fun getAllOrder( onResult: (List<Order>) -> Unit, onFailure: (Exception) -> Unit){
        val userId = authRepo.getUserId()
        if(userId == null){
            Log.d("User", "No user")
            return
        }
        fireStore.collection("orders").whereEqualTo("userId", userId).get().addOnSuccessListener {result ->
            val order = result.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.apply {
                    id = doc.id
                }
            }
            onResult(order)
        }.addOnFailureListener {
            Log.e("order_Firebase", "Failed to load orders")
            onFailure(it)
        }
    }

    fun placeOrder(
        order: Order,
        items: List<CartItem>,
        onResult: (String) -> Unit,onFailure: (Exception) -> Unit
    ) {

        val userId = authRepo.getUserId()
        if (userId == null) {
            Log.d("User", "No user")
            return
        }

        val setOrder = fireStore.collection("orders").document()

        val orderItems = items.map { cartToOrderItem(it) }

        val previewImages = items
            .take(3)
            .map { it.image }

        val newOrder = order.copy(
            userId = userId,
            previewImages = previewImages
        )

        setOrder.set(newOrder)
            .addOnSuccessListener {

                val batch = fireStore.batch()
                val itemCol = setOrder.collection("items")

                orderItems.forEach { item ->
                    val itemRef = itemCol.document()
                    batch.set(itemRef, item)
                }

                batch.commit()
                    .addOnSuccessListener {
                        onResult("Order placed successfully")
                    }
                    .addOnFailureListener {
                        onResult("Order saved but items failed")
                    }

            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun getOrderDetail(
        orderId: String,
        onResult: (Order) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = authRepo.getUserId()
        if (userId == null) {
            Log.d("User", "No user")
            return
        }

        fireStore.collection("orders")
            .document(orderId)
            .get()
            .addOnSuccessListener { doc ->
                val order = doc.toObject(Order::class.java)?.apply {
                    id = doc.id
                }
                if(order == null){
                    onFailure(Exception("Order history not found"))
                    return@addOnSuccessListener
                }
                onResult(order)
            }
            .addOnFailureListener { e ->
                Log.e("order_Firebase", "Failed to load order detail", e)
                onFailure(e)
            }
    }

    fun getOrderItem(orderId: String, onResult: (List<OrderItem>) -> Unit, onFailure: (Exception) -> Unit){
        fireStore.collection("orders").document(orderId).collection("items").get().addOnSuccessListener { result ->
            val items = result.documents.mapNotNull {
                it.toObject(OrderItem::class.java)?.apply {
                    id = it.id
                }
            }
            onResult(items)
        }.addOnFailureListener {
            onFailure(it)
        }
    }
}