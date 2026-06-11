package io.kess.ecommerce.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.model.OrderItem
import io.kess.ecommerce.repository.AuthRepository
import io.kess.ecommerce.repository.OrderRepository
import io.kess.ecommerce.util.UiState
import kotlinx.serialization.builtins.UIntArraySerializer

class OrderViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val repository = OrderRepository(authRepo)
    private val _orders = MutableLiveData<UiState<List<Order>>>()
    val orders: LiveData<UiState<List<Order>>> = _orders
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message
    private val _ordersDetail = MutableLiveData<UiState<Order>>()
    val ordersDetail: LiveData<UiState<Order>> = _ordersDetail
    private val _isOrder = MutableLiveData(false)
    val isOrder: LiveData<Boolean> = _isOrder
    private val _orderItem = MutableLiveData<List<OrderItem>>()
    val orderItem = _orderItem

    fun loadOrder() {
        _orders.value = UiState.Loading
        repository.getAllOrder(onResult = { data ->
            _orders.value = UiState.Success(data)
        }, onFailure = {
            _message.value = it.message
            _orders.value = UiState.Error(it.message.toString())
        })
    }

    fun placeOrder(order: Order, items: List<CartItem>) {
        if (_isOrder.value == true) return
        _isOrder.value = true
        repository.placeOrder(
            order,
            items,
            onResult = { data ->
                _message.value = data
                _isOrder.value = false
            },
            onFailure = {
                _message.value = it.message
                _isOrder.value = false
            })
    }

    fun getOrderDetail(orderId: String) {
        _ordersDetail.value = UiState.Loading
        repository.getOrderDetail(orderId, onResult = { data ->
            _ordersDetail.value = UiState.Success(data)

        }, onFailure = { _ordersDetail.value = UiState.Error(it.message.toString()) })
    }

    fun getOrderItem(orderId: String) {
        repository.getOrderItem(
            orderId,
            onResult = { data -> _orderItem.value = data },
            onFailure = { _message.value = it.message })
    }
}