package io.kess.ecommerce.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.model.ShopCartGroup
import io.kess.ecommerce.repository.AuthRepository
import io.kess.ecommerce.repository.CartItemRepository
import io.kess.ecommerce.util.UiState


class CartViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val repository = CartItemRepository(authRepo)
    private val _cartItems = MutableLiveData<UiState<List<CartItem>>>()
    val cartItems: LiveData<UiState<List<CartItem>>> = _cartItems
    private val _cartGroup = MutableLiveData<List<ShopCartGroup>>()
    val cartGroup: LiveData<List<ShopCartGroup>> = _cartGroup
    private val _loadingItems = MutableLiveData<Set<String>>(emptySet())
    val loadingItems: LiveData<Set<String>> = _loadingItems
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message
    private val _isAddingToCart = MutableLiveData(false)
    val isAddingToCart: LiveData<Boolean> = _isAddingToCart

    fun clearMessage() {
        _message.value = null
    }

    private fun setLoading(cartId: String, isLoading: Boolean) {
        val current = _loadingItems.value ?: emptySet()

        _loadingItems.value = if (isLoading) {
            current + cartId
        } else {
            current - cartId
        }
    }

    fun loadCart() {
        _cartItems.value = UiState.Loading
        repository.getAllCart(
            onResult = { data ->
                _cartItems.value = UiState.Success(data)
                buildCartUi(data)
            },
            onFailure = {
                _message.value = it.message
                _cartItems.value = UiState.Error(it.message.toString())
            }
        )
    }

    private fun buildCartUi(carts: List<CartItem>) {
        val grouped = carts.groupBy { it.shopId }
        val result = grouped.map { (shopId, items) ->
            ShopCartGroup(
                shopId = shopId,
                shopName = items.firstOrNull()?.shopName ?: "Unknown Shop",
                items = items
            )
        }
        _cartGroup.value = result
    }
    fun addToCart(cartItem: CartItem) {
        if (_isAddingToCart.value == true) return
        _isAddingToCart.value = true

        repository.addCart(cartItem, onResult = { txt ->
            _message.value = txt
            _isAddingToCart.value = false

        }, onFailure = {
            _message.value = it.message
            _isAddingToCart.value = false
        })
    }

    fun deleteCart(cartId: String) {
        setLoading(cartId, true)

        repository.deleteCart(cartId, onResult = { message ->
            _message.value = message
            setLoading(cartId, false)

        }, onFailure = { e ->
            _message.value = e.message
            setLoading(cartId, false)
        })
    }

    fun increaseQuantity(cartId: String) {
        setLoading(cartId, true)

        repository.increaseQuantity(cartId, onResult = { txt ->
            _message.value = txt
            setLoading(cartId, false)

        }, onFailure = {
            _message.value = it.message
            setLoading(cartId, false)
        })
    }

    fun decreaseQuantity(cartId: String, currentQty: Int) {
        setLoading(cartId, true)

        if (currentQty <= 1) {
            setLoading(cartId, false)
            return
        }

        repository.decreaseQuantity(cartId, onResult = { txt ->

            _message.value = txt
            setLoading(cartId, false)

        }, onFailure = {
            _message.value = it.message
            setLoading(cartId, false)
        })
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeCartListener()
    }
}