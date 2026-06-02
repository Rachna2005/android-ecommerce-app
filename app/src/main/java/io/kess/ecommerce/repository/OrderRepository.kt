package io.kess.ecommerce.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.model.OrderItem
import io.kess.ecommerce.model.cartToOrderItem
import io.kess.ecommerce.util.UserSession

class OrderRepository {
    val fireStore = FirebaseFirestore.getInstance()
//    val userId = UserSession.currentUser!!.id
private fun getUserId(): String? {
    return UserSession.currentUser?.id
}
    fun getAllOrder(onResult: (List<Order>) -> Unit){
        val userId = getUserId()
        if(userId == null){
            Log.d("User", "No user")
            return
        }
        fireStore.collection("orders").whereEqualTo("userId", userId).get().addOnSuccessListener {result ->
            val order = result.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }
            onResult(order)
        }.addOnFailureListener {
            Log.e("order_Firebase", "Failed to load orders")
        }
    }

    fun placeOrder(order: Order, items: List<CartItem>, onResult: (String) -> Unit){
        val userId = getUserId()
        if(userId == null){
            Log.d("User", "No user")
            return
        }
        val setOrder = fireStore.collection("orders").document()
        val newOrder = order.copy(
            id = setOrder.id,
            userId = userId
        )
        setOrder.set(newOrder).addOnSuccessListener {
           val batch = fireStore.batch()
            val itemCol = setOrder.collection("items")
            val orderItem = items.map { cartToOrderItem(it) }
            orderItem.forEach { item ->
                val itemRef = itemCol.document()
                batch.set(itemRef, item)
            }
            batch.commit().addOnSuccessListener {
                onResult("Order placed successfully")
            }.addOnFailureListener {
                onResult("Order saved but items failed")
            }
        }.addOnFailureListener {
            onResult("Failed to place order")
        }
    }

    fun getOrderItem(orderId: String, onResult: (List<OrderItem>) -> Unit){
        fireStore.collection("orders").document(orderId).collection("items").get().addOnSuccessListener { result ->
            val items = result.documents.mapNotNull {
                it.toObject(OrderItem::class.java)
            }
            onResult(items)
        }.addOnFailureListener {
            onResult(emptyList())
        }
    }
}