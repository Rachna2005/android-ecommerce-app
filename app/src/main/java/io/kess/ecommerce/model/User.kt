package io.kess.ecommerce.model

import com.google.firebase.firestore.Exclude

enum class UserRole(){
    CUSTOMER, SELLER
}

data class User(
    @get:Exclude
    var id: String = "",
    val name: String = "",
    val email: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val role: String = UserRole.CUSTOMER.name
)

