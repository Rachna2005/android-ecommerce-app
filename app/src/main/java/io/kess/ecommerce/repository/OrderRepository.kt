package io.kess.ecommerce.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.model.OrderItem
import io.kess.ecommerce.model.cartToOrderItem

class OrderRepository(private val authRepo: AuthRepository) {
    val fireStore = FirebaseFirestore.getInstance()

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

    fun getAllOrder(onResult: (List<Order>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = requireUserId(onFailure) ?: return
        fireStore.collection("orders").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { result ->
                val order = result.documents.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.apply {
                        id = doc.id
                    }
                }
                onResult(order)
            }.addOnFailureListener {
                onFailure(it)
            }
    }

    fun placeOrders(
        orders: List<Pair<Order, List<CartItem>>>,
        onResult: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = requireUserId(onFailure) ?: return

        val batch = fireStore.batch()

        orders.forEach { (order, items) ->

            val orderRef = fireStore.collection("orders").document()

            val previewImages = items
                .take(3)
                .map { it.image }

            val newOrder = order.copy(
                userId = userId,
                previewImages = previewImages
            )

            batch.set(orderRef, newOrder)

            val itemCollection = orderRef.collection("items")

            items.map(::cartToOrderItem).forEach { item ->
                batch.set(itemCollection.document(), item)
            }
        }

        batch.commit()
            .addOnSuccessListener { onResult() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getOrderByShop(
        shopId: String,
        onResult: (List<Order>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fireStore.collection("orders").whereEqualTo("shopId", shopId).get()
            .addOnSuccessListener { data ->
                val orders = data.documents.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.apply {
                        id = doc.id
                    }
                }
                onResult(orders)
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        onResult: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val orderRef = fireStore.collection("orders").document(orderId)

        orderRef.get()
            .addOnSuccessListener { doc ->
                val order = doc.toObject(Order::class.java)

                if (order == null) {
                    onFailure(Exception("Order not found"))
                    return@addOnSuccessListener
                }

                orderRef.update("status", newStatus)
                    .addOnSuccessListener { onResult() }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    fun getOrderDetail(
        orderId: String,
        onResult: (Order) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = requireUserId(onFailure) ?: return
        fireStore.collection("orders")
            .document(orderId)
            .get()
            .addOnSuccessListener { doc ->
                val order = doc.toObject(Order::class.java)?.apply {
                    id = doc.id
                }
                if (order == null) {
                    onFailure(Exception("Order history not found"))
                    return@addOnSuccessListener
                }
                onResult(order)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getOrderItem(
        orderId: String,
        onResult: (List<OrderItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fireStore.collection("orders").document(orderId).collection("items").get()
            .addOnSuccessListener { result ->
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