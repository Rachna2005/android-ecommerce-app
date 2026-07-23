package io.kess.ecommerce.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Category(
    @get:Exclude
    var id: String = "",
    val name: String = "",
    val productCount: Int = 0,
    val image: String = "",
    val alignRight: Boolean = true,
    val createAt: Timestamp = Timestamp.now()
)
