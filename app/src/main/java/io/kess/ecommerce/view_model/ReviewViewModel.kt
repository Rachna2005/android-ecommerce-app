package io.kess.ecommerce.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.Review
import io.kess.ecommerce.repository.AuthRepository
import io.kess.ecommerce.repository.ReviewRepository
import io.kess.ecommerce.util.UiState
import io.kess.ecommerce.view_model.TestVM.Event

class ReviewViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val repository = ReviewRepository(authRepo)

    private val _reviews = MutableLiveData<UiState<List<Review>>>()
    val reviews: LiveData<UiState<List<Review>>> = _reviews
    private val _actionState = MutableLiveData<UiState<Unit>>()
    val actionState: LiveData<UiState<Unit>> = _actionState
    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    fun clearState(){
        _actionState.value = UiState.Idle
    }

    fun addReview(
        productId: String,
        review: String,
        rating: Int
    ) {
        _actionState.value = UiState.Loading
        repository.addReview(
            productId = productId,
            review = review,
            rating = rating, onSuccess = { result ->
                _actionState.value = UiState.Success(Unit)
                _message.value = Event(result)
                loadReviews(productId)
            }, onFailure = { _actionState.value = UiState.Error(it.message.toString()) }
        )
    }

    fun loadReviews(productId: String) {
        _reviews.value = UiState.Loading
        repository.getReview(
            productId = productId,
            onSuccess = { reviews ->
                _reviews.value = UiState.Success(reviews)
            },
            onFailure = { e ->
                _reviews.value = UiState.Error(e.message.toString())
            }
        )
    }

//    fun clearMessage() {
//        _message.value = null
//    }
}