package io.kess.ecommerce.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.Order
import io.kess.ecommerce.model.OrderItem
import io.kess.ecommerce.model.ShopCartGroup
import io.kess.ecommerce.repository.AuthRepository
import io.kess.ecommerce.repository.OrderRepository
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.TestVM.Event
import kotlinx.serialization.builtins.UIntArraySerializer

class OrderViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val repository = OrderRepository(authRepo)
    private val _orders = MutableLiveData<UiState<List<Order>>>()
    val orders: LiveData<UiState<List<Order>>> = _orders
    private val _ordersDetail = MutableLiveData<UiState<Order>>()
    val ordersDetail: LiveData<UiState<Order>> = _ordersDetail
    private val _orderItem = MutableLiveData<UiState<List<OrderItem>>>()
    val orderItem: LiveData<UiState<List<OrderItem>>> = _orderItem
    private val _actionState = MutableLiveData<UiState<Unit>>()
    val actionState: LiveData<UiState<Unit>> = _actionState
    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message
    private val _allOrdersCompleted = MutableLiveData<Boolean>()
    val allOrdersCompleted: LiveData<Boolean> = _allOrdersCompleted

    fun clearState() {
        _actionState.value = UiState.Idle
    }

    fun loadOrder() {
        _orders.value = UiState.Loading
        repository.getAllOrder(onResult = { data ->
            _orders.value = UiState.Success(data)
        }, onFailure = {
//            _message.value = it.message
            _orders.value = UiState.Error(it.message.toString())
        })
    }

    fun placeOrder(order: List<Pair<Order, List<CartItem>>>) {
        _actionState.value = UiState.Loading

        repository.placeOrders(
            order,
            onResult = {
                _message.value = Event("Order placed successfully")
                _actionState.value = UiState.Success(Unit)
            },
            onFailure = {
                _actionState.value = UiState.Error(it.message ?: "Unknown error")
            }
        )
    }

    fun getOrderDetail(orderId: String) {
        _ordersDetail.value = UiState.Loading
        repository.getOrderDetail(orderId, onResult = { data ->
            _ordersDetail.value = UiState.Success(data)

        }, onFailure = { _ordersDetail.value = UiState.Error(it.message.toString()) })
    }

    fun getOrderItem(orderId: String) {
        _orderItem.value = UiState.Loading
        repository.getOrderItem(
            orderId,
            onResult = { data -> _orderItem.value = UiState.Success(data) },
            onFailure = { _orderItem.value = UiState.Error(it.message.toString()) })
    }

    fun updateStatus(orderId: String, txt: String) {
        _actionState.value = UiState.Loading
        repository.updateOrderStatus(orderId, txt, onResult = {
            _actionState.value = UiState.Success(
                Unit
            )
        }, onFailure = { _actionState.value = UiState.Error(it.message.toString()) })
    }

    fun getOrderByShop(orderId: String) {
        _orders.value = UiState.Loading
        repository.getOrderByShop(
            orderId,
            onResult = { data -> _orders.value = UiState.Success(data) },
            onFailure = {
                _orders.value =
                    UiState.Error(it.message.toString())
            })
    }
}