package io.kess.ecommerce.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.User
import io.kess.ecommerce.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val _authData = MutableLiveData<User?>()
    val authData: LiveData<User?> = _authData
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun register(name: String, email: String, password: String) {
        repository.register(name, email, password, onSuccess = { result ->
            _authData.value = result
        }, onFailure = { e ->
            _authData.value = null
            Log.d("REGISTER", e.message.toString())
        })
    }

    fun login(email: String, password: String) {
        repository.login(
            email,
            password,
            onSuccess = { user ->
                _authData.value = user
            },
            onFailure = { e ->
                _authData.value = null
                Log.d("LOGIN", e.message.toString())
            })
    }

    fun updateUser(
        name: String? = null,
        address: String? = null,
        phoneNumber: String? = null
    ) {
        repository.updateUser(
            name = name,

            address = address,
            phoneNumber = phoneNumber,
            onSuccess = { txt ->
                val currentUser = _authData.value

                if (currentUser != null) {
                    val updatedUser = currentUser.copy(
                        name = name ?: currentUser.name,
                        address = address ?: currentUser.address,
                        phoneNumber = phoneNumber ?: currentUser.phoneNumber
                    )

                    _authData.value = updatedUser
                }

                _message.value = txt
            },
            onFailure = { e ->
                Log.d("UPDATE", e.message.toString())
                _message.value = e.message
            }
        )
    }

    fun getUser() {
        repository.getCurrentUser(
            onSuccess = { user ->
                _authData.value = user
            },
            onFailure = { e ->
//                _authData.value = null
                Log.d("USER_SESSION", e.message.toString())
            })
    }

    fun getUserId() {
        repository.getUserId()
    }

    fun logout() {
        repository.logout()
    }


}
