package io.kess.ecommerce.view_model
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.Shop
import io.kess.ecommerce.repository.ShopRepository
import io.kess.ecommerce.util.UiState
class ShopViewModel : ViewModel() {

    private val repository = ShopRepository()

    private val _shopState = MutableLiveData<UiState<Shop>>()
    val shopState: LiveData<UiState<Shop>> = _shopState

    private val _actionState = MutableLiveData<UiState<String>>()
    val actionState: LiveData<UiState<String>> = _actionState

    fun createShop(shop: Shop) {

        _shopState.value = UiState.Loading

        repository.createShop(
            shop = shop,

            onSuccess = { createdShop ->
                _shopState.value = UiState.Success(createdShop)
            },

            onFailure = { e ->
                _shopState.value =
                    UiState.Error(e.message ?: "Failed to create shop")
            }
        )
    }

    fun getShopByOwner(ownerId: String) {

        _shopState.value = UiState.Loading

        repository.getShopByOwner(
            ownerId = ownerId,
            onSuccess = { shop ->
                _shopState.value = UiState.Success(shop)
            },

            onFailure = { e ->
                _shopState.value =
                    UiState.Error(e.message ?: "Failed to load shop")
            }
        )
    }
    fun getCurrentShop(): Shop? {
        return (_shopState.value as? UiState.Success)?.data
    }

    fun updateShop(
        shopId: String,
        shopName: String? = null,
        description: String? = null,
        phone: String? = null,
        address: String? = null,
        logoUrl: String? = null
    ) {

        _actionState.value = UiState.Loading

        repository.updateShop(
            shopId = shopId,
            shopName = shopName,
            description = description,
            phone = phone,
            address = address,
            logoUrl = logoUrl,

            onSuccess = { message ->

                val currentState = _shopState.value

                if (currentState is UiState.Success) {

                    val currentShop = currentState.data

                    val updatedShop = currentShop.copy(
                        shopName = shopName ?: currentShop.shopName,
                        description = description ?: currentShop.description,
                        phone = phone ?: currentShop.phone,
                        address = address ?: currentShop.address,
                        logoUrl = logoUrl ?: currentShop.logoUrl
                    )

                    updatedShop.id = currentShop.id

                    _shopState.value = UiState.Success(updatedShop)
                }

                _actionState.value = UiState.Success(message)
            },

            onFailure = { e ->
                _actionState.value =
                    UiState.Error(e.message ?: "Update failed")
            }
        )
    }

    fun disableShop(shopId: String) {

        _actionState.value = UiState.Loading

        repository.disableShop(
            shopId = shopId,

            onSuccess = { message ->
                _actionState.value = UiState.Success(message)
            },

            onFailure = { e ->
                _actionState.value =
                    UiState.Error(e.message ?: "Failed to disable shop")
            }
        )
    }
}