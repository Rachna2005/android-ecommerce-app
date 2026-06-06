package io.kess.ecommerce.repository

import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.model.ProductDetail
import io.kess.ecommerce.model.ProductVariant
import org.junit.runner.notification.Failure

class ProductRepository {
    val fireStore = FirebaseFirestore.getInstance()

    fun getProduct(onResult: (List<Product>) -> Unit, onFailure: (Exception) -> Unit) {
        fireStore.collection("products").get().addOnSuccessListener { result ->
            val productList = result.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.apply { id = doc.id }
            }
            onResult(productList)
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }

    fun getProductDetail(
        productId: String,
        onResult: (ProductDetail) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fireStore.collection("products").document(productId).get().addOnSuccessListener { result ->
            val product = result.toObject(Product::class.java)?.apply { id = result.id }
            if (product == null) {
                onFailure(Exception("Product not found"))
                return@addOnSuccessListener
            }
            result.reference.collection("variants").get().addOnSuccessListener { variantDoc ->
                val variants = variantDoc.documents.mapNotNull { doc ->
                    doc.toObject(ProductVariant::class.java)?.apply { id = doc.id }
                }
                val productDetail = ProductDetail(product, variants)
                onResult(productDetail)
            }.addOnFailureListener { e -> onFailure(e) }

        }.addOnFailureListener { e -> onFailure(e) }
    }

    fun getProductByCategory(
        categoryId: String,
        onResult: (List<Product>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fireStore.collection("products").whereEqualTo("categoryId", categoryId).get()
            .addOnSuccessListener { result ->
                val product = result.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { id = doc.id }
                }
                onResult(product)
            }.addOnFailureListener {
                e -> onFailure(e)
            }
    }
}