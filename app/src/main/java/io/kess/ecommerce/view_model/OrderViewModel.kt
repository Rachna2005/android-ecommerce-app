package io.kess.ecommerce.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.model.OrderItem
import io.kess.ecommerce.repository.OrderRepository

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()
    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _orderItem = MutableLiveData<List<OrderItem>>()
    val orderItem = _orderItem

    fun loadOrder() {
        repository.getAllOrder(onResult = { data ->
            _orders.value = data
        })
    }

    fun placeOrder(order: Order, items: List<CartItem>) {
        repository.placeOrder(order, items, onResult = { data -> _message.value = data })
    }

    fun getOrderItem(orderId: String) {
        repository.getOrderItem(orderId, onResult = { data -> _orderItem.value = data })
    }
}