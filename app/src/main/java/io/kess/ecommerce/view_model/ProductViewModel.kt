package io.kess.ecommerce.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.model.ProductDetail
import io.kess.ecommerce.repository.ProductRepository
import io.kess.ecommerce.util.UiState

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val _products = MutableLiveData<UiState<List<Product>>>()
    val products: LiveData<UiState<List<Product>>> = _products

    private val _productDetail = MutableLiveData<UiState<ProductDetail>>()
    val productDetail: LiveData<UiState<ProductDetail>> = _productDetail

    fun loadAllProducts() {
        _products.value = UiState.Loading
        repository.getProduct(onResult = { data ->
            _products.value = UiState.Success(data)
        }, onFailure = { e ->
            Log.d("GET_ALL_PRODUCT", e.message.toString())
            _products.value = UiState.Error(e.message.toString())
        })
    }

    fun getProductDetail(productId: String) {
        _productDetail.value = UiState.Loading
        repository.getProductDetail(
            productId = productId,
            onResult = { data -> _productDetail.value = UiState.Success(data) },
            onFailure = { e ->
                Log.d("Get_All_Product", e.message.toString())
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
}

