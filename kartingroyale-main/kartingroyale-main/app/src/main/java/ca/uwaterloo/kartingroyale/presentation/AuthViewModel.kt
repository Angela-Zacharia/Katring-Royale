package ca.uwaterloo.kartingroyale.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.uwaterloo.kartingroyale.data.CloudStorage
import kotlinx.coroutines.launch

class AuthViewModel(private val cloudStorage: CloudStorage) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                cloudStorage.loginUser(email, password)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Login failed"
            } finally {
                isLoading = false
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                cloudStorage.logoutUser()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Logout failed"
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                cloudStorage.createUser(email, password, displayName)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Sign up failed"
            } finally {
                isLoading = false
            }
        }
    }
}