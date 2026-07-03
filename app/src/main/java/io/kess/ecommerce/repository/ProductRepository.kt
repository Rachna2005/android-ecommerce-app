package io.kess.ecommerce.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.model.ProductDetail
import io.kess.ecommerce.model.ProductFilter
import io.kess.ecommerce.model.ProductVariant
import org.junit.runner.notification.Failure
import kotlin.text.get

class ProductRepository {
    val fireStore = FirebaseFirestore.getInstance()
    private var shopProductListener: ListenerRegistration? = null
    private var lastVisible: DocumentSnapshot? = null
    private var isLastPage = false
    private var isLoading = false
    private var currentQuery: Query? = null
    private var lastCreatedAt: Timestamp? = null

    fun getProduct(
        limit: Int,
        filter: ProductFilter,
        isRefresh: Boolean = false,
        onResult: (List<Product>, Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        if (isRefresh) {
            lastCreatedAt = null
            isLastPage = false
            isLoading = false
        }
        if (isLoading || isLastPage) return
        isLoading = true
        var query: Query = fireStore.collection("products")

        filter.categoryId?.let {
            query = query.whereEqualTo("categoryId", it)
        }
        filter.shopId?.let {
            query = query.whereEqualTo("shopId", it)
        }
        filter.minPrice?.let {
            query = query.whereGreaterThanOrEqualTo("price", it)
        }
        filter.maxPrice?.let {
            query = query.whereLessThanOrEqualTo("price", it)
        }
        query = query.orderBy("createdAt")

        if (lastCreatedAt != null) {
            query = query.startAfter(lastCreatedAt)
        }

        query.limit(limit.toLong()).get()
            .addOnSuccessListener { result ->
                val productList = result.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { id = doc.id }
                }
                lastCreatedAt = result.documents.lastOrNull()?.getTimestamp("createdAt")
                if ((productList.size < 6) || productList.isEmpty()) {
                    isLastPage = true
                }
                isLoading = false
                onResult(productList, isLastPage)
            }.addOnFailureListener { e ->
                isLoading = false
                onFailure(e)
            }
    }

    fun debugBrokenProducts() {
        fireStore.collection("products")
            .get()
            .addOnSuccessListener { result ->

                val broken = result.documents.filter {
                    it.getTimestamp("createdAt") == null
                }

                Log.d("DEBUG_PRODUCTS", "BROKEN COUNT = ${broken.size}")

                broken.forEach {
                    Log.d("DEBUG_PRODUCTS", "BROKEN ID = ${it.id} DATA = ${it.data}")
                }
            }
    }

//    fun getProduct(
//        isRefresh: Boolean = false,
//        onResult: (List<Product>) -> Unit,
//        onFailure: (Exception) -> Unit
//    ) {
//
//        Log.d("PAGINATION", "==============================")
//        Log.d("PAGINATION", "CALL getProduct() isRefresh=$isRefresh")
//        Log.d("PAGINATION", "lastVisible BEFORE = ${lastVisible?.id}")
//        Log.d("PAGINATION", "isLoading=$isLoading isLastPage=$isLastPage")
//
//        if (isRefresh) {
//            Log.d("PAGINATION", "RESET pagination state")
//
//            lastVisible = null
//            isLastPage = false
//            isLoading = false
//        }
//
//        if (isLoading || isLastPage) {
//            Log.d("PAGINATION", "BLOCKED → isLoading=$isLoading isLastPage=$isLastPage")
//            return
//        }
//
//        isLoading = true
//
//        var query: Query = fireStore.collection("products")
//            .orderBy("createdAt")
//
//        Log.d("PAGINATION", "BASE QUERY created")
//
//        if (lastVisible != null) {
//            Log.d("PAGINATION", "Applying startAfter lastVisible=${lastVisible?.id}")
//            query = query.startAfter(lastVisible)
//        } else {
//            Log.d("PAGINATION", "FIRST PAGE (no startAfter)")
//        }
//
//        query.limit(6)
//            .get()
//            .addOnSuccessListener { result ->
//
//                Log.d("PAGINATION", "SUCCESS RESPONSE RECEIVED")
//                Log.d("PAGINATION", "RAW SIZE = ${result.size()}")
//                Log.d("PAGINATION", "DOC COUNT = ${result.documents.size}")
//
//                if (result.documents.isEmpty()) {
//                    Log.d("PAGINATION", "EMPTY RESULT → END OF DATA")
//                    isLastPage = true
//                    isLoading = false
//                    onResult(emptyList())
//                    return@addOnSuccessListener
//                }
//
//                val productList = result.documents.mapNotNull { doc ->
//                    val product = doc.toObject(Product::class.java)
//                    Log.d(
//                        "PAGINATION",
//                        "MAP DOC → id=${doc.id} createdAt=${doc.getTimestamp("createdAt")}"
//                    )
//                    product?.apply { id = doc.id }
//                }
//
//                val newLast = result.documents.lastOrNull()
//
//                Log.d("PAGINATION", "NEW LAST DOC ID = ${newLast?.id}")
//                Log.d("PAGINATION", "NEW LAST createdAt = ${newLast?.getTimestamp("createdAt")}")
//
//                lastVisible = newLast
//
//                Log.d("PAGINATION", "UPDATED lastVisible = ${lastVisible?.id}")
//
//                if (productList.size < 6) {
//                    Log.d("PAGINATION", "LESS THAN LIMIT → LAST PAGE")
//                    isLastPage = true
//                }
//
//                isLoading = false
//
//                Log.d("PAGINATION", "FINAL RESULT SIZE = ${productList.size}")
//                Log.d("PAGINATION", "==============================")
//
//                onResult(productList)
//            }
//            .addOnFailureListener { e ->
//
//                Log.e("PAGINATION", "FAILED QUERY", e)
//
//                isLoading = false
//                Log.d("PAGINATION", "RESET isLoading=false due to error")
//
//                onFailure(e)
//            }
//    }

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
            }.addOnFailureListener { e ->
                onFailure(e)
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