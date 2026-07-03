package io.kess.ecommerce.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.Shop
import io.kess.ecommerce.model.User
import io.kess.ecommerce.model.UserRole
class AuthRepository {
    fun getCurrentUser(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        val dbUser = FirebaseAuth.getInstance().currentUser?.uid
        if (dbUser == null) {
            onFailure(Exception("User not Logged in"))
            return
        }
        FirebaseFirestore.getInstance().collection("users").document(dbUser).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                if (user != null) {
                    user.id = doc.id
                    onSuccess(user)
                } else {
                    onFailure(Exception("User not found"))
                }
            }.addOnFailureListener { e ->
            onFailure(Exception("Failed to get user"))
        }
    }

    fun updateUser(
        name: String? = null,
        address: String? = null,
        phoneNumber: String? = null,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val updates = mutableMapOf<String, Any>()

        name?.let { updates["name"] = it }
        address?.let { updates["address"] = it }
        phoneNumber?.let { updates["phoneNumber"] = it }

        if (updates.isEmpty()) {
            onFailure(Exception("No fields to update"))
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .update(updates)
            .addOnSuccessListener {
                onSuccess("Profile updated successfully")
            }
            .addOnFailureListener {
                onFailure(Exception("Failed to updated profile"))
            }
    }

    fun logout(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            FirebaseAuth.getInstance().signOut()

            if (FirebaseAuth.getInstance().currentUser == null) {
                onSuccess()
            } else {
                onFailure(Exception("Logout failed"))
            }
        } catch (e: Exception) {
            onFailure(Exception("Logout failed"))
        }
    }

    fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun registerUser(
        name: String,
        email: String,
        password: String,
        role: UserRole ,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user!!.uid

                val user = User(
                    id = uid,
                    name = name,
                    email = email,
                    role = role.name
                )

                firestore.collection("users")
                    .document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        onSuccess(user)
                    }
                    .addOnFailureListener { e ->
                        result.user?.delete()
                        onFailure(e)
                    }
            }
            .addOnFailureListener(onFailure)
    }

    fun login(
        email: String, password: String, onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
//                Log.d("LOGIN", "Auth success. UID=${result.user?.uid}")
                val uid = result.user!!.uid
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toObject(User::class.java)
                        if (user != null) {
                            onSuccess(user)
                        } else {
                            onFailure(Exception("User data not found"))
                        }
                    }.addOnFailureListener { e ->
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }
}