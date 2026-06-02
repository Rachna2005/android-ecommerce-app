package io.kess.ecommerce.model

import com.google.firebase.firestore.Exclude

data class User(
    @get:Exclude
    var id: String = "",

    val name: String = "",
    val email: String = "",
    val address: String = "",
    val phoneNumber: String = ""
)

