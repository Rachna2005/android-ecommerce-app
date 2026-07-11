package io.kess.ecommerce.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.Date
import io.kess.ecommerce.model.Product
import io.kess.ecommerce.model.ProductDetail
import io.kess.ecommerce.model.ProductFilter
import io.kess.ecommerce.model.ProductQuery
import io.kess.ecommerce.model.ProductVariant
import io.kess.ecommerce.util.ProductPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow


enum class ProductDisplayType {
    ALL,
    DISCOUNT,
    NEW_ARRIVAL
}

class ProductRepository {
    val fireStore = FirebaseFirestore.getInstance()
    private var shopProductListener: ListenerRegistration? = null
    private var lastVisible: DocumentSnapshot? = null
    private var isLastPage = false
    private var isLoading = false
    private var currentQuery: Query? = null
    private var lastCreatedAt: Timestamp? = null
    val oneWeekAgo = Timestamp(Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000))

    //    fun getProduct(
//        limit: Int,
//        filter: ProductFilter,
//        isRefresh: Boolean = false,
//        disPlayType: ProductDisplayType,
//        onResult: (List<Product>, Boolean) -> Unit,
//        onFailure: (Exception) -> Unit
//    ) {
//        if (isRefresh) {
//            lastCreatedAt = null
//            isLastPage = false
//            isLoading = false
//        }
//        if (isLoading || isLastPage) return
//        isLoading = true
//        var query: Query = fireStore.collection("products")
//
//        filter.categoryId?.let {
//            query = query.whereEqualTo("categoryId", it)
//        }
//        filter.shopId?.let {
//            query = query.whereEqualTo("shopId", it)
//        }
//        filter.minPrice?.let {
//            query = query.whereGreaterThanOrEqualTo("price", it)
//        }
//        filter.maxPrice?.let {
//            query = query.whereLessThanOrEqualTo("price", it)
//        }
////        query = query.orderBy("createdAt")
//        when (disPlayType) {
//            ProductDisplayType.ALL -> {
//                query = query.orderBy("createdAt")
//            }
//
//            ProductDisplayType.NEW_ARRIVAL -> {
//                query = query.whereGreaterThanOrEqualTo("createdAt", oneWeekAgo)
//                    .orderBy("createdAt", Query.Direction.DESCENDING)
//            }
//
//            ProductDisplayType.DISCOUNT -> {
//                query = query.whereGreaterThan("discountPercentage", 0)
//                    .orderBy("createdAt", Query.Direction.DESCENDING)
//            }
//        }
//
//        if (lastCreatedAt != null) {
//            query = query.startAfter(lastCreatedAt)
//        }
//        query.limit(limit.toLong()).get()
//            .addOnSuccessListener { result ->
//                val productList = result.documents.mapNotNull { doc ->
//                    doc.toObject(Product::class.java)?.apply { id = doc.id }
//                }
//                lastCreatedAt = result.documents.lastOrNull()?.getTimestamp("createdAt")
//                if ((productList.size < 6) || productList.isEmpty()) {
//                    isLastPage = true
//                }
//                isLoading = false
//                onResult(productList, isLastPage)
//            }.addOnFailureListener { e ->
//                isLoading = false
//                onFailure(e)
//            }
//    }
    fun homeProduct(
        displayType: ProductDisplayType,
        limit: Int,
        onResult: (List<Product>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        var query: Query = fireStore.collection("products")
        when (displayType) {
            ProductDisplayType.ALL -> {}
            ProductDisplayType.DISCOUNT -> {
                query = query.whereGreaterThan("discountPercentage", 0)
            }

            ProductDisplayType.NEW_ARRIVAL -> {
                query = query.whereGreaterThanOrEqualTo("createdAt", oneWeekAgo)
            }
        }
        query.orderBy("createdAt")
        query.limit(limit.toLong()).get()
            .addOnSuccessListener { result ->
                val productList = result.documents.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { id = doc.id }
                }
                onResult(productList)
            }.addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun getProduct(query: ProductQuery): Flow<PagingData<Product>> {
        Log.d("PAGING", "Create Pager with query = $query")
        val fireStoreQuery = buildQuery(query)
        return Pager(
            config = PagingConfig(
                pageSize = 8,
                initialLoadSize = 8,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                ProductPagingSource(fireStoreQuery)
            }
        ).flow
    }

    private fun buildQuery(productQuery: ProductQuery): Query {
        var query: Query = fireStore.collection("products")
        productQuery.categoryId?.let {
            query = query.whereEqualTo("categoryId", it)
        }
        productQuery.shopId?.let {
            query = query.whereEqualTo("shopId", it)
        }
        productQuery.minPrice?.let {
            Log.d("FILTER", "Min price = $it")
            query = query.whereGreaterThanOrEqualTo("price", it.toDouble())
        }
        productQuery.maxPrice?.let {
            Log.d("FILTER", "Max price = $it")
            query = query.whereLessThanOrEqualTo("price", it.toDouble())
            query.orderBy("price")
        }
        when (productQuery.displayType) {
            ProductDisplayType.ALL -> {}
            ProductDisplayType.DISCOUNT -> {
                query = query.whereGreaterThan("discountPercentage", 0.0)
            }

            ProductDisplayType.NEW_ARRIVAL -> {
                query = query.whereGreaterThanOrEqualTo("createdAt", oneWeekAgo)
            }
        }
        return query.orderBy("createdAt")
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

    fun getProductByShop(
        shopId: String,
        onResult: (List<Product>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fireStore.collection("products")
            .whereEqualTo("shopId", shopId).get().addOnSuccessListener { doc ->
                val products = doc.documents.mapNotNull { data ->
                    data.toObject(Product::class.java)?.apply { id = data.id }
                }
                onResult(products)
            }.addOnFailureListener {
                onFailure(it)
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