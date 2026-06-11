package io.kess.ecommerce.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.repository.AuthRepository
import io.kess.ecommerce.repository.FavoriteRepository
import io.kess.ecommerce.ui.adapter.ProductAdapter

class FavoriteViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val repository = FavoriteRepository(authRepo)
    private val _favorites = MutableLiveData<Set<String>>()
    val favorite: LiveData<Set<String>> = _favorites
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message
    private val _loadingFavorites = MutableLiveData<Set<String>>(emptySet())
    val loadingFavorites: LiveData<Set<String>> = _loadingFavorites

    private fun startLoading(productId: String) {
        val current = _loadingFavorites.value?.toMutableSet() ?: mutableSetOf()
        current.add(productId)
        _loadingFavorites.value = current
    }

    private fun stopLoading(productId: String) {
        val current = _loadingFavorites.value?.toMutableSet() ?: mutableSetOf()
        current.remove(productId)
        _loadingFavorites.value = current
    }

    fun toggleFavorite(productId: String) {
        if (_loadingFavorites.value?.contains(productId) == true) {
            return
        }

        startLoading(productId)

        repository.toggleFavorite(productId, onResult = { txt ->
            val allFavorite = _favorites.value?.toMutableSet() ?: mutableSetOf()
            if (allFavorite.contains(productId)) {
                allFavorite.remove(productId)
            } else {
                allFavorite.add(productId)
            }
            _favorites.value = allFavorite
            _message.value = txt
            stopLoading(productId)
        }, onFailure = { _message.value = it.message
            stopLoading(productId)})
    }

    fun loadFavorite() {
        repository.getAllFavorite(onResult = { result ->
            _favorites.value = result
        }, onFailure = { _message.value = it.message })
    }
}