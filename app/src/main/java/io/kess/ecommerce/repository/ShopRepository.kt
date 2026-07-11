package io.kess.ecommerce.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.Shop


class ShopRepository {

    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val SHOP_COLLECTION = "shops"
    }

    fun createShop(
        shop: Shop,
        onSuccess: (Shop) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val docRef = firestore.collection(SHOP_COLLECTION).document()

        val shopToSave = shop.copy(
            createAt = Timestamp.now()
        )

        docRef.set(shopToSave)
            .addOnSuccessListener {

                val createdShop = shopToSave.copy(
                    id = docRef.id
                )

                onSuccess(createdShop)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun getAllShops(
        onSuccess: (List<Shop>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        firestore.collection(SHOP_COLLECTION)
            .whereEqualTo("isActive", true) // optional
            .get()
            .addOnSuccessListener { snapshot ->

                val shops = snapshot.documents.mapNotNull { doc ->

                    doc.toObject(Shop::class.java)?.apply {
                        id = doc.id
                    }
                }

                onSuccess(shops)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

//    fun getShop(
//        shopId: String,
//        onSuccess: (Shop) -> Unit,
//        onFailure: (Exception) -> Unit
//    ) {
//
//        firestore.collection(SHOP_COLLECTION)
//            .document(shopId)
//            .get()
//            .addOnSuccessListener { doc ->
//
//                val shop = doc.toObject(Shop::class.java)
//
//                if (shop != null) {
//                    shop.id = doc.id
//                    onSuccess(shop)
//                } else {
//                    onFailure(Exception("Shop not found"))
//                }
//            }
//            .addOnFailureListener {
//                onFailure(it)
//            }
//    }

    fun getShopDetail(shopId: String, onSuccess: (Shop) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection(SHOP_COLLECTION).document(shopId).get().addOnSuccessListener { doc ->
            val shop = doc.toObject(Shop::class.java)
            if (shop != null) {
                shop.id = doc.id
                onSuccess(shop)
            } else {
                onFailure(Exception("Shop not found"))
            }
        }.addOnFailureListener {
            onFailure(it)
        }
    }


    fun getShopByOwner(
        ownerId: String,
        onSuccess: (Shop) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        firestore.collection(SHOP_COLLECTION)
            .whereEqualTo("ownerId", ownerId)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->

                if (snapshot.isEmpty) {
                    onFailure(Exception("Shop not found"))
                    return@addOnSuccessListener
                }

                val doc = snapshot.documents.first()

                val shop = doc.toObject(Shop::class.java)

                if (shop != null) {
                    shop.id = doc.id
                    onSuccess(shop)
                } else {
                    onFailure(Exception("Shop not found"))
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun updateShop(
        shopId: String,
        shopName: String? = null,
        description: String? = null,
        phone: String? = null,
        address: String? = null,
        logoUrl: String? = null,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val updates = mutableMapOf<String, Any>()

        shopName?.let { updates["shopName"] = it }
        description?.let { updates["description"] = it }
        phone?.let { updates["phone"] = it }
        address?.let { updates["address"] = it }
        logoUrl?.let { updates["logoUrl"] = it }
        if (updates.isEmpty()) {
            onFailure(Exception("No fields to update"))
            return
        }

        firestore.collection(SHOP_COLLECTION)
            .document(shopId)
            .update(updates)
            .addOnSuccessListener {
                onSuccess("Shop updated successfully")
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun disableShop(
        shopId: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        firestore.collection(SHOP_COLLECTION)
            .document(shopId)
            .update("isActive", false)
            .addOnSuccessListener {
                onSuccess("Shop disabled")
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}