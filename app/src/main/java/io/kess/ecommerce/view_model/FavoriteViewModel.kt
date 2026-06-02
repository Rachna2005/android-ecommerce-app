package io.kess.ecommerce.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.repository.FavoriteRepository
import io.kess.ecommerce.ui.adapter.ProductAdapter

class FavoriteViewModel : ViewModel() {
    private val repository = FavoriteRepository()
    private val _favorites = MutableLiveData<Set<String>>()
    val favorite: LiveData<Set<String>> = _favorites

    fun toggleFavorite(productId: String) {
        val allFavorite = _favorites.value?.toMutableSet() ?: mutableSetOf()
        if(allFavorite.contains(productId)){
            allFavorite.remove(productId)
        }else{
            allFavorite.add(productId)
        }
        _favorites.value = allFavorite
        repository.toggleFavorite(productId)
    }

    fun loadFavorite() {
        repository.getAllFavorite(onResult = { result ->
            _favorites.value = result
        })
    }
}