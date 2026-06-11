package io.kess.ecommerce.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.Review
import io.kess.ecommerce.repository.AuthRepository
import io.kess.ecommerce.repository.ReviewRepository

class ReviewViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val repository = ReviewRepository(authRepo)

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun addReview(
        productId: String,
        review: String,
        rating: Int
    ) {
        repository.addReview(
            productId = productId,
            review = review,
            rating = rating, onSuccess = { result ->
                _message.value = result
                loadReviews(productId)
            }, onFailure = { _message.value = it.message }
        )
    }

    fun loadReviews(productId: String) {
        repository.getReview(
            productId = productId,
            onSuccess = { reviews ->
                _reviews.value = reviews
            },
            onFailure = { e ->
                _message.value = e.message ?: "Unknown error"
            }
        )
    }

//    fun clearMessage() {
//        _message.value = null
//    }
}