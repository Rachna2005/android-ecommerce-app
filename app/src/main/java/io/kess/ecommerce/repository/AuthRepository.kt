package io.kess.ecommerce.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.kess.ecommerce.model.User

class AuthRepository {
    fun getCurrentUser(onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        val dbUser = FirebaseAuth.getInstance().currentUser?.uid
        if(dbUser == null){
            onFailure(Exception("User not Logged in"))
            return
        }
        FirebaseFirestore.getInstance().collection("users").document(dbUser).get().addOnSuccessListener { doc ->
            val user = doc.toObject(User::class.java)
            if(user != null){
                user.id = doc.id
                onSuccess(user)
            }else{
                onFailure(Exception("User not found"))
            }
        }.addOnFailureListener { e ->
            onFailure(e)
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
                onFailure(it)
            }
    }

    fun logout(){
        FirebaseAuth.getInstance().signOut()
    }

    fun getUserId(): String?{
        return FirebaseAuth.getInstance().currentUser?.uid
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
                Log.d("LOGIN", "Auth success. UID=${result.user?.uid}")
                val uid = result.user!!.uid
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val user = doc.toObject(User::class.java)
                        if (user != null) {
                            onSuccess(user)
                            Log.d("LOGIN", "Firestore success")
                        } else {
                            onFailure(Exception("User data not found"))
                        }
                    }.addOnFailureListener { e ->
                        Log.d("LOGIN", "Auth failed: ${e.message}")
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.d("LOGIN", "Auth failed: ${e.message}")
                onFailure(e)
            }
    }
}