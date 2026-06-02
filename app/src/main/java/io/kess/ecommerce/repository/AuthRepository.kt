package io.kess.ecommerce.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.User

class AuthRepository {
    fun getCurrentUser(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        val dbUser = FirebaseAuth.getInstance().currentUser
        if (dbUser == null) {
            onFailure(Exception("No user log in"))
            return
        }
        val id = dbUser.uid
        FirebaseFirestore.getInstance().collection("users").document(id).get()
            .addOnSuccessListener { document ->

                val user = document.toObject(User::class.java)

                if (user != null) {
                    user.id = document.id
                    onSuccess(user)

                } else {
                    onFailure(Exception("User data is null"))
                }
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid
                val user = User( name = name, email = email)
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .set(user).addOnSuccessListener {
                        onSuccess(user)
                    }.addOnFailureListener { e ->
                        onFailure(e)
                    }
            }.addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun login(
        email: String, password: String, onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
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