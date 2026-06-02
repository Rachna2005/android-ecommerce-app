package io.kess.ecommerce.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.model.ProductDetail
import io.kess.ecommerce.repository.ProductRepository

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _productDetail = MutableLiveData<ProductDetail>()
    val productDetail: LiveData<ProductDetail> = _productDetail

    fun loadAllProducts() {
        repository.getProduct(onResult = { data ->
            _products.value = data
        }, onFailure = { e ->
            Log.d("GET_ALL_PRODUCT", e.message.toString())

        })
    }

    fun getProductDetail(productId: String) {
        repository.getProductDetail(
            productId = productId,
            onResult = { data -> _productDetail.value = data },
            onFailure = { e -> Log.d("Get_All_Product", e.message.toString()) })
    }

    fun categoryProduct(categoryId: String) {
        repository.getProductByCategory(
            categoryId = categoryId,
            onResult = { data -> _products.value = data },
            onFailure = { e ->
                Log.d("PRODUCT_BY_CATEGORY", e.message.toString())
            })
    }
}

