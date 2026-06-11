package io.kess.ecommerce.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.repository.AuthRepository
import io.kess.ecommerce.repository.CartItemRepository

class CartViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val repository = CartItemRepository(authRepo)

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

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
        repository.getAllCart(
            onResult = { data ->
                _cartItems.value = data
            },
            onFailure = {
                _message.value = it.message
            }
        )
    }

    fun addToCart(cartItem: CartItem) {
        if (_isAddingToCart.value == true) return
        _isAddingToCart.value = true

        repository.addCart(cartItem, onResult = { txt ->

            val currentList = _cartItems.value?.toMutableList()
                ?: mutableListOf()
            val cartId = "${cartItem.productId}_${cartItem.variantId}"

            val index = currentList.indexOfFirst {
                it.productId == cartItem.productId &&
                        it.variantId == cartItem.variantId
            }

            if (index != -1) {
                val existing = currentList[index]
                currentList[index] = existing.copy(
                    quantity = existing.quantity + cartItem.quantity
                )
            } else {
                currentList.add(cartItem.copy(id = cartId))
            }

            _cartItems.value = currentList
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

            val current = _cartItems.value
                ?.toMutableList()
                ?: return@deleteCart

            val updateList = current.filter { item ->
                item.id != cartId
            }

            _cartItems.value = updateList
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

            val current = _cartItems.value.orEmpty()

            val updateList = current.map { item ->
                if (item.id == cartId) {
                    item.copy(quantity = item.quantity + 1)
                } else item
            }

            _cartItems.value = updateList
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

            val current = _cartItems.value.orEmpty()

            val updateList = current.map { item ->
                if (item.id == cartId) {
                    item.copy(quantity = item.quantity - 1)
                } else item
            }

            _cartItems.value = updateList
            _message.value = txt
            setLoading(cartId, false)

        }, onFailure = {
            _message.value = it.message
            setLoading(cartId, false)
        })
    }
}