package io.kess.ecommerce.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.util.UserSession

class FavoriteRepository {
    val db = FirebaseFirestore.getInstance()
//    val userId = UserSession.currentUser!!.id
private fun getUserId(): String? {
    return UserSession.currentUser?.id
}

    fun toggleFavorite(productId: String) {
        val userId = getUserId()
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
                } else {
                    favorite.set(mapOf("productId" to productId))
                }
            }
    }

    fun getAllFavorite(onResult: (Set<String>) -> Unit) {
        val userId = getUserId()
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
            }
    }
}