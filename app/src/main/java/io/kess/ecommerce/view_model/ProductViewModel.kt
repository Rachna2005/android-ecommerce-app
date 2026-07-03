package io.kess.ecommerce.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.model.ProductDetail
import io.kess.ecommerce.model.ProductFilter
import io.kess.ecommerce.model.ProductVariant
import io.kess.ecommerce.repository.ProductRepository
import io.kess.ecommerce.util.UiState

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val _products = MutableLiveData<UiState<List<Product>>>()
    val products: LiveData<UiState<List<Product>>> = _products

    private val _productDetail = MutableLiveData<UiState<ProductDetail>>()
    val productDetail: LiveData<UiState<ProductDetail>> = _productDetail
    private val _variants =
        MutableLiveData<UiState<List<ProductVariant>>>()

    val variants: LiveData<UiState<List<ProductVariant>>> =
        _variants
    private val _variantActionState = MutableLiveData<UiState<String>>()
    val variantActionState: LiveData<UiState<String>> = _variantActionState
    private val _actionState =
        MutableLiveData<UiState<String>>()
    val actionState: LiveData<UiState<String>> =
        _actionState

    private val _isLoadingMore = MutableLiveData(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore
    var isLastPage = false

    //    var isLoading = false
    private var currentFilter = ProductFilter()
    fun getCurrentFilter() = currentFilter

    fun clearState() {
        _actionState.value = UiState.Idle

    }

    fun loadAllProducts(
        isRefresh: Boolean = false,
        limit: Int,
        filter: ProductFilter = ProductFilter()
    ) {
        currentFilter = filter
        if (isRefresh) {
            _products.value = UiState.Loading
            isLastPage = false
//            isLoading = false
        } else {
            _isLoadingMore.value = true
        }
        if (isLastPage) return
//        isLoading = true
        repository.getProduct(
            isRefresh = isRefresh,
            limit = limit,
            filter = filter,
            onResult = { data, lastPage ->
//            isLoading = false
                isLastPage = lastPage
                _isLoadingMore.value = false
                val current = (_products.value as? UiState.Success)?.data ?: emptyList()
                val newList = if (isRefresh) data else current + data
                _products.value = UiState.Success(newList)
            },
            onFailure = { e ->
                _isLoadingMore.value = false
//            isLoading = false
                _products.value = UiState.Error(e.message.toString())
            })
    }

    fun getProductDetail(productId: String) {
        _productDetail.value = UiState.Loading
        repository.getProductDetail(
            productId = productId,
            onResult = { data -> _productDetail.value = UiState.Success(data) },
            onFailure = { e ->
                _productDetail.value = UiState.Error(e.message.toString())
            })
    }

    fun categoryProduct(categoryId: String) {
        _products.value = UiState.Loading
        repository.getProductByCategory(
            categoryId = categoryId,
            onResult = { data -> _products.value = UiState.Success(data) },
            onFailure = { e ->
                Log.d("PRODUCT_BY_CATEGORY", e.message.toString())
            })
    }

    fun observeProductsByShop(
        shopId: String
    ) {
        _products.value = UiState.Loading
        repository.observeProductsByShop(
            shopId = shopId,
            onResult = { products ->
                _products.value =
                    UiState.Success(products)
            },
            onFailure = { e ->
                _products.value =
                    UiState.Error(
                        e.message ?: "Failed to load products"
                    )
            }
        )
    }

    fun addProduct(
        product: Product
    ) {
        _actionState.value = UiState.Loading

        repository.addProduct(
            product = product,
            onResult = { productId ->
                _actionState.value =
                    UiState.Success(productId)
            },
            onFailure = { e ->
                _actionState.value =
                    UiState.Error(
                        e.message ?: "Failed to add product"
                    )
            }
        )
    }

    fun updateProduct(
        productId: String,
        product: Product
    ) {

        _actionState.value = UiState.Loading

        repository.updateProduct(
            productId = productId,
            product = product,
            onResult = { message ->
                _actionState.value =
                    UiState.Success(message)
            },
            onFailure = { e ->
                _actionState.value =
                    UiState.Error(
                        e.message ?: "Failed to update product"
                    )
            }
        )
    }

    fun deleteProduct(
        productId: String
    ) {

        _actionState.value = UiState.Loading

        repository.deleteProduct(
            productId = productId,
            onResult = { message ->
                _actionState.value =
                    UiState.Success(message)
            },
            onFailure = { e ->
                _actionState.value =
                    UiState.Error(
                        e.message ?: "Failed to delete product"
                    )
            }
        )
    }

    fun getVariants(
        productId: String
    ) {

        _variants.value = UiState.Loading

        repository.getVariants(
            productId = productId,
            onResult = { variants ->
                _variants.value =
                    UiState.Success(variants)
            },
            onFailure = { e ->
                _variants.value =
                    UiState.Error(
                        e.message ?: "Failed to load variants"
                    )
            }
        )
    }

    fun addVariant(
        productId: String,
        variant: ProductVariant
    ) {

        _variantActionState.value = UiState.Loading

        repository.addVariant(
            productId = productId,
            variant = variant,
            onResult = { message ->
                _variantActionState.value =
                    UiState.Success(message)
            },
            onFailure = { e ->
                _variantActionState.value =
                    UiState.Error(
                        e.message ?: "Failed to add variant"
                    )
            }
        )
    }

    fun updateVariant(
        productId: String,
        variantId: String,
        variant: ProductVariant
    ) {

        _variantActionState.value = UiState.Loading

        repository.updateVariant(
            productId = productId,
            variantId = variantId,
            variant = variant,
            onResult = { message ->
                _variantActionState.value =
                    UiState.Success(message)
            },
            onFailure = { e ->
                _variantActionState.value =
                    UiState.Error(
                        e.message ?: "Failed to update variant"
                    )
            }
        )
    }

    fun deleteVariant(
        productId: String,
        variantId: String
    ) {

        _variantActionState.value = UiState.Loading

        repository.deleteVariant(
            productId = productId,
            variantId = variantId,
            onResult = { message ->
                _variantActionState.value =
                    UiState.Success(message)
            },
            onFailure = { e ->
                _variantActionState.value =
                    UiState.Error(
                        e.message ?: "Failed to delete variant"
                    )
            }
        )
    }

//    fun clearActionState() {
//        _actionState.value = UiState.Idle
//    }

    override fun onCleared() {
        super.onCleared()
        repository.removeShopProductListener()
    }

}

