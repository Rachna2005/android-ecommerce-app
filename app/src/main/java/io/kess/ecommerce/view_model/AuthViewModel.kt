package io.kess.ecommerce.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.kess.ecommerce.model.User
import io.kess.ecommerce.model.UserRole
import io.kess.ecommerce.repository.AuthRepository
import io.kess.ecommerce.util.UiState

//enum class MessageType {
//    SUCCESS,
//    ERROR,
//}
//
//data class UiMessage(
//    val text: String,
//    val type: MessageType
//)

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val _authState = MutableLiveData<UiState<User>>()
    val authState: LiveData<UiState<User>> = _authState

    //    private val _userId = MutableLiveData<String?>()
//    val userId: LiveData<String?> = _userId
    private val _actionState = MutableLiveData<UiState<User>>()
    val actionState: LiveData<UiState<User>> = _actionState

    fun clearState() {
        _actionState.value = UiState.Idle
    }

    fun register(name: String, email: String, password: String, role: UserRole) {

        _authState.value = UiState.Loading

        repository.registerUser(
            name,
            email,
            password,
            role,
            onSuccess = { user ->
                _authState.value = UiState.Success(user)
//                _userId.value = user.id
            },
            onFailure = { e ->
                _authState.value = UiState.Error(
                    e.message ?: "Register failed"
                )
            }
        )
    }

    fun login(email: String, password: String) {

        _authState.value = UiState.Loading

        repository.login(
            email,
            password,

            onSuccess = { user ->
                _authState.value = UiState.Success(user)
//                _userId.value = user.id
            },

            onFailure = { e ->
                _authState.value = UiState.Error(
                    e.message ?: "Login failed"
                )
            }
        )
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
            onSuccess = { message ->

                val currentState = _authState.value

                val currentUser = (currentState as? UiState.Success)?.data

                if (currentUser != null) {

                    val updatedUser = currentUser.copy(
                        name = name ?: currentUser.name,
                        address = address ?: currentUser.address,
                        phoneNumber = phoneNumber ?: currentUser.phoneNumber
                    )

                    _authState.value = UiState.Success(updatedUser)
                }
                // optional: if no current user, still emit success message logic is NOT needed anymore
            },

            onFailure = { e ->
                _authState.value = UiState.Error(
                    e.message ?: "Update failed"
                )
            }
        )
    }

    fun getUser() {

        _authState.value = UiState.Loading

        repository.getCurrentUser(
            onSuccess = { user ->
                _authState.value = UiState.Success(user)
//                _userId.value = user.id
            },
            onFailure = { e ->
                _authState.value = UiState.Error(
                    e.message ?: "User session error"
                )
            }
        )
    }

    fun getUserId(): String? {
        return repository.getUserId()
    }

    fun logout() {
        _actionState.value = UiState.Loading
        repository.logout(onSuccess = {

        }, onFailure = {})
    }

}
