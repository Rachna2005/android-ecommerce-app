package io.kess.ecommerce.repository

import android.media.Rating
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.Review
import org.junit.runner.notification.Failure

class ReviewRepository(private val authRepo: AuthRepository) {
    val fireStore = FirebaseFirestore.getInstance()

    fun addReview(productId: String, review: String, rating: Int, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        authRepo.getCurrentUser(
            onSuccess = { user ->
                val reviewRef =
                    fireStore.collection("products")
                        .document(productId)
                        .collection("reviews")
                        .document()

                val review = Review(
                    userId = user.id,
                    username = user.name,
                    rating = rating,
                    review = review,
                    createdAt = Timestamp.now()
                )

                reviewRef.set(review)
                    .addOnSuccessListener {
                        onSuccess("Added review")
                    }
                    .addOnFailureListener {
                       onFailure(it)
                    }
            },
            onFailure = {
                Log.d("User", "No user logged in")
                onFailure(it)
            }
        )
    }

    fun getReview(
        productId: String,
        onSuccess: (List<Review>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fireStore.collection("products").document(productId).collection("reviews")
            .orderBy("createdAt").get().addOnSuccessListener { result ->
                val review = result.documents.mapNotNull { doc ->
                    doc.toObject(Review::class.java)?.apply {
                        id = doc.id
                    }

                }
                onSuccess(review)

            }.addOnFailureListener { e -> onFailure(e) }
    }
}