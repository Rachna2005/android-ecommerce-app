package io.kess.ecommerce.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.CartItem
import io.kess.ecommerce.repository.CartItemRepository

class CartViewModel : ViewModel() {

    private val repository = CartItemRepository()

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message
    fun clearMessage(){
        _message.value = null
    }


    fun loadCart() {
        repository.getAllCart { result ->
            _cartItems.value = result
        }
    }

    fun addToCart(cartItem: CartItem) {
        repository.addCart(cartItem) { result ->
            _message.value = result
        }
        val currentList = _cartItems.value?.toMutableList() ?: mutableListOf()

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
            currentList.add(cartItem)
        }

        _cartItems.value = currentList
    }

    fun deleteCart(cartId: String) {
        repository.deleteCart(cartId)
        val updateList = _cartItems.value?.filter{item ->
           item.id != cartId
        }
        _cartItems.value = updateList!!
    }

    fun increaseQuantity(cartId: String) {
        repository.increaseQuantity(cartId)
        val updateList = _cartItems.value?.map{item ->
            if(item.id == cartId){
                item.copy(quantity = item.quantity + 1)
            }else{
                item
            }
        }
        _cartItems.value = updateList!!

    }

    fun decreaseQuantity(cartId: String, currentQty: Int) {

        if (currentQty <= 1) {
            return
        } else {
            repository.decreaseQuantity(cartId, currentQty)
            val updateList = _cartItems.value?.map { item ->
                if(item.id == cartId){
                    item.copy(quantity = item.quantity -1)
                }else{
                    item
                }
            }
            _cartItems.value = updateList!!
        }

    }
}