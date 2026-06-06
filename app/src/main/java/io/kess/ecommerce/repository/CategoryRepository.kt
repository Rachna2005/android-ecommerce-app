package io.kess.ecommerce.repository

import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.Category

class CategoryRepository {
    private val fireStore = FirebaseFirestore.getInstance()

    fun getCategories(
        onResult: (List<Category>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        fireStore.collection("categories")
            .get()
            .addOnSuccessListener { result ->

                val categories = result.documents.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.apply {
                        id = doc.id
                    }
                }
                onResult(categories)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}