package io.kess.ecommerce.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.model.ProductDetail
import io.kess.ecommerce.model.ProductVariant
import org.junit.runner.notification.Failure

class ProductRepository {
    val fireStore = FirebaseFirestore.getInstance()
    private var shopProductListener: ListenerRegistration? = null

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

    fun observeProductsByShop(
        shopId: String,
        onResult: (List<Product>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        shopProductListener?.remove()
        shopProductListener =
            fireStore.collection("products")
                .whereEqualTo("shopId", shopId)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        onFailure(error)
                        return@addSnapshotListener
                    }

                    val products =
                        snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Product::class.java)?.apply {
                                id = doc.id
                            }
                        } ?: emptyList()
                    onResult(products)
                }
    }

    fun removeShopProductListener() {
        shopProductListener?.remove()
        shopProductListener = null
    }

    fun addProduct(
        product: Product,
        onResult: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fireStore.collection("products")
            .add(product)
            .addOnSuccessListener {
                onResult(it.id)
            }
            .addOnFailureListener(onFailure)
    }

    fun updateProduct(
        productId: String,
        product: Product,
        onResult: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        fireStore.collection("products")
            .document(productId)
            .set(product)
            .addOnSuccessListener {
                onResult("Product updated successfully")
            }
            .addOnFailureListener(onFailure)
    }

    fun deleteProduct(
        productId: String,
        onResult: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val productRef =
            fireStore.collection("products")
                .document(productId)

        productRef.collection("variants")
            .get()
            .addOnSuccessListener { result ->

                val batch = fireStore.batch()

                result.documents.forEach {
                    batch.delete(it.reference)
                }

                batch.delete(productRef)

                batch.commit()
                    .addOnSuccessListener {
                        onResult("Product deleted successfully")
                    }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    fun addVariant(
        productId: String,
        variant: ProductVariant,
        onResult: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        fireStore.collection("products")
            .document(productId)
            .collection("variants")
            .add(variant)
            .addOnSuccessListener {
                updateTotalStock(
                    productId,
                    onFailure
                )
                onResult("Variant added successfully")
            }
            .addOnFailureListener(onFailure)
    }

    fun updateVariant(
        productId: String,
        variantId: String,
        variant: ProductVariant,
        onResult: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        fireStore.collection("products")
            .document(productId)
            .collection("variants")
            .document(variantId)
            .set(variant)
            .addOnSuccessListener {

                updateTotalStock(
                    productId,
                    onFailure
                )

                onResult("Variant updated successfully")
            }
            .addOnFailureListener(onFailure)
    }

    fun deleteVariant(
        productId: String,
        variantId: String,
        onResult: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        fireStore.collection("products")
            .document(productId)
            .collection("variants")
            .document(variantId)
            .delete()
            .addOnSuccessListener {

                updateTotalStock(
                    productId,
                    onFailure
                )

                onResult("Variant deleted successfully")
            }
            .addOnFailureListener(onFailure)
    }

    fun getVariants(
        productId: String,
        onResult: (List<ProductVariant>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        fireStore.collection("products")
            .document(productId)
            .collection("variants")
            .get()
            .addOnSuccessListener { result ->

                val variants =
                    result.documents.mapNotNull { doc ->
                        doc.toObject(ProductVariant::class.java)?.apply {
                            id = doc.id
                        }
                    }

                onResult(variants)
            }
            .addOnFailureListener(onFailure)
    }
    private fun updateTotalStock(
        productId: String,
        onFailure: ((Exception) -> Unit)? = null
    ) {

        val productRef =
            fireStore.collection("products")
                .document(productId)

        productRef.collection("variants")
            .get()
            .addOnSuccessListener { result ->

                val totalStock =
                    result.documents.sumOf {
                        it.toObject(ProductVariant::class.java)?.stock ?: 0
                    }

                productRef.update(
                    "totalStock",
                    totalStock
                )
            }
            .addOnFailureListener {
                onFailure?.invoke(it)
            }
    }

}