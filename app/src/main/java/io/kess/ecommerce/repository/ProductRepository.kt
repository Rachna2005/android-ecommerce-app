package io.kess.ecommerce.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
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
import io.kess.ecommerce.util.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf


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


        if (query.isSearch && query.keyword.isNullOrBlank()) {
            return flowOf(PagingData.empty())
        }

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

        val hasPriceFilter = productQuery.minPrice != null || productQuery.maxPrice != null

        productQuery.minPrice?.let {
            query = query.whereGreaterThanOrEqualTo("price", it.toDouble())
        }

        productQuery.maxPrice?.let {
            query = query.whereLessThanOrEqualTo("price", it.toDouble())
        }


        when (productQuery.displayType) {
            ProductDisplayType.ALL -> {
//                query = query.orderBy("createdAt")
            }

            ProductDisplayType.DISCOUNT -> {
                query = query.whereGreaterThan("discountPercentage", 0.0)
                query = query.orderBy("discountPercentage")
//                    .orderBy("createdAt")

            }

            ProductDisplayType.NEW_ARRIVAL -> {
                query =
                    query.whereGreaterThanOrEqualTo("createdAt", oneWeekAgo)
//                        .orderBy("createdAt")
            }
        }
        if (productQuery.displayType == ProductDisplayType.ALL &&
            productQuery.categoryId == null &&
            productQuery.shopId == null &&
            !hasPriceFilter &&
            !productQuery.keyword.isNullOrBlank()
        ) {
            return query
                .orderBy("name")
                .startAt(productQuery.keyword!!)
                .endAt(productQuery.keyword!! + "\uf8ff")
        }
//        return query.orderBy("createdAt")
        query = if (hasPriceFilter) {
            query.orderBy("price").orderBy("createdAt")
        } else {
            query.orderBy("createdAt")
        }

        return query
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

    fun getFavoriteProduct(
        favorite: Set<String>,
        onResult: (List<Product>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val products = mutableListOf<Product>()
        var complete = 0
        favorite.forEach { productId ->
            fireStore.collection("products").document(productId).get().addOnSuccessListener { doc ->
                if (doc.exists()) {

                    doc.toObject(Product::class.java)?.apply {
                        id = doc.id
                    }?.let {
                        products.add(it)
                    }
                }
                complete++
                if (complete == favorite.size) {
                    onResult(products)
                }
            }.addOnFailureListener {
                onFailure(it)
            }
        }
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

    fun updateVariantStock(
        productId: String,
        variantId: String,
        quantity: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val variantRef = fireStore.collection("products")
            .document(productId)
            .collection("variants")
            .document(variantId)

        variantRef.update(
            "stock",
            FieldValue.increment(-quantity.toLong())
        )
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
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