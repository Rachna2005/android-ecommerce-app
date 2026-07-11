package io.kess.ecommerce.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.Category
import io.kess.ecommerce.repository.CategoryRepository

class CategoryViewModel : ViewModel() {

    private val repository = CategoryRepository()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

//    init {
//        loadCategories()
//    }

    fun loadCategories() {
        repository.getCategories(
            onResult = { data ->
                _categories.value = data
            },
            onFailure = { e ->
                Log.d("GET_CATEGORIES", e.message.toString())
            }
        )
    }
}