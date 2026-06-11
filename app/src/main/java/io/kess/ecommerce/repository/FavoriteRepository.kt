package io.kess.ecommerce.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.Product

class FavoriteRepository(private val authRepo: AuthRepository) {
    val db = FirebaseFirestore.getInstance()

    fun toggleFavorite(productId: String, onResult: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = authRepo.getUserId()
        if(userId == null){
            Log.d("User", "No user")
            return
        }
        Log.d("FIREBASE_DEBUG", "userId=$userId productId=$productId")
        val favorite =
            db.collection("users").document(userId).collection("favorites").document(productId)
        favorite.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    favorite.delete()
                    onResult("Delete Favorite")
                } else {
                    favorite.set(mapOf("productId" to productId))
                    onResult("Favorite")
                }
            }.addOnFailureListener { onFailure(it) }
    }

    fun getAllFavorite(onResult: (Set<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = authRepo.getUserId()
        if(userId == null){
            Log.d("User", "No user")
            return
        }
        db.collection("users").document(userId).collection("favorites").get()
            .addOnSuccessListener { result ->
                val ids = result.documents.map {
                    it.id
                }
                onResult(ids.toSet())
            }.addOnFailureListener { onFailure(it) }
    }
}