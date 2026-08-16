package com.example.docspro.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docspro.data.model.UserModel
import com.example.docspro.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var userProfile by mutableStateOf<UserModel?>(null)

    init {
        loadProfile()
    }

    fun loadProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = currentUser.uid
        val defaultEmail = currentUser.email ?: ""
        val fallbackUsername = if (defaultEmail.contains("@")) defaultEmail.substringBefore("@") else "User"

        viewModelScope.launch {
            val result = authRepository.getUserProfile(uid)
            result.onSuccess { profile ->
                // Nếu profile lấy từ Firestore rỗng email hoặc username, bổ sung thông tin từ Auth
                userProfile = UserModel(
                    uid = uid,
                    username = if (profile.username.isNotBlank()) profile.username else fallbackUsername,
                    email = if (profile.email.isNotBlank()) profile.email else defaultEmail
                )
            }.onFailure {
                // Dự phòng nếu không lấy được tài liệu từ Firestore
                userProfile = UserModel(
                    uid = uid,
                    username = fallbackUsername,
                    email = defaultEmail
                )
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        authRepository.signOut()
        userProfile = null
        onLogoutSuccess()
    }
}