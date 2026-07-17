package io.kess.ecommerce.util

import android.app.DownloadManager
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import io.kess.ecommerce.model.Product

import io.kess.ecommerce.model.ProductQuery
import kotlinx.coroutines.tasks.await

class ProductPagingSource(private val query: Query) : PagingSource<DocumentSnapshot, Product>() {
    override suspend fun load(params: LoadParams<DocumentSnapshot>): LoadResult<DocumentSnapshot, Product> {
        Log.d(
            "Paging",
            "load() key=${params.key?.id}, loadSize=${params.loadSize}"
        )
        return try {
            var currentQuery = query
            if (params.key != null) {

//                Log.d(
//                    "Paging",
//                    "Start after id=${params.key!!.id}"
//                )
//
//                Log.d(
//                    "Paging",
//                    "Start after createdAt=${params.key!!.get("createdAt")}"
//                )

                currentQuery = currentQuery.startAfter(params.key!!)
//                currentQuery = currentQuery.startAfter(params.key!!.getTimestamp("createdAt"))
            }
            val snapshot = currentQuery.limit(params.loadSize.toLong()).get().await()
            Log.d(
                "FIRESTORE",
                "Result count = ${snapshot.size()}"
            )
            val products = snapshot.documents.mapNotNull { doc ->

                val product = doc.toObject(Product::class.java)
//                Log.d(
//                    "Paging",
//                    "Convert ${doc.id} -> $product"
//                )

                product?.apply {
                    id = doc.id
                }

            }
//            snapshot.documents.forEachIndexed { index, doc ->
//                Log.d(
//                    "Paging",
//                    "$index -> id=${doc.id}, createdAt=${doc.get("createdAt")}"
//                )
//            }
//            Log.d("PRODUCT_Count", products.size.toString())
//            Log.d(
//                "Paging",
//                "nextKey=${snapshot.documents.lastOrNull()?.id}"
//            )
            LoadResult.Page(
                data = products,
                prevKey = null,
                nextKey = snapshot.documents.lastOrNull()
            )
        } catch (e: Exception) {
            Log.e("PAGING", "Load failed", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<DocumentSnapshot, Product>): DocumentSnapshot? {
        return null
    }
}