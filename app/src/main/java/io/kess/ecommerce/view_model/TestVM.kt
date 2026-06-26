package io.kess.ecommerce.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.model.OrderItem
import io.kess.ecommerce.model.cartToOrderItem
import io.kess.ecommerce.repository.AuthRepository
import io.kess.ecommerce.repository.OrderRepository
import io.kess.ecommerce.util.UiState

class TestVM {

    class OrderRepository(private val authRepo: AuthRepository) {
        val fireStore = FirebaseFirestore.getInstance()
        fun placeOrder(
            order: Order,
            items: List<CartItem>,
            onResult: () -> Unit, onFailure: (Exception) -> Unit
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
                            onResult()
                        }
                        .addOnFailureListener {
                            onFailure(it)
                        }
                }
                .addOnFailureListener {
                    onFailure(it)
                }
        }
    }

    class Event<out T>(private val content: T) {
        private var hasBeenHandled = false

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }
        fun peekContent(): T = content
    }

    class OrderViewModel : ViewModel() {
        private val authRepo = AuthRepository()
        private val repository = OrderRepository(authRepo)
        private val _orders = MutableLiveData<UiState<List<Order>>>()
        val orders: LiveData<UiState<List<Order>>> = _orders
        private val _actionState =
            MutableLiveData<UiState<String>>()

        val actionState: LiveData<UiState<String>> =
            _actionState
        private val _message = MutableLiveData<Event<String>>()
        val message: LiveData<Event<String>> = _message

        fun placeOrder(order: Order, items: List<CartItem>) {
            _actionState.value = UiState.Loading
            repository.placeOrder(
                order,
                items,
                onResult = {
                    _actionState.value = UiState.Success("")
                    _message.value = Event("Add to Card Success")
                },
                onFailure = {
                    _actionState.value = UiState.Error(it.toString())
                    _message.value = Event("Fail to add to cart")
                })
        }

    }
}