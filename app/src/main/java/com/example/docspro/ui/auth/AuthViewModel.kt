package com.example.docspro.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docspro.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun login(email: String, pwd: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.signIn(email, pwd)
            isLoading = false
            result.onSuccess { onSuccess() }
                .onFailure { errorMessage = it.message }
        }
    }

    fun signUp(username: String, email: String, pwd: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.signUp(username, email, pwd)
            isLoading = false
            result.onSuccess { onSuccess() }
                .onFailure { errorMessage = it.message }
        }
    }

    fun logout() {
        repository.signOut()
    }
}