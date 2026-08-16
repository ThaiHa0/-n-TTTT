package com.example.docspro.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docspro.data.model.DocumentModel
import com.example.docspro.data.repository.DocumentRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: DocumentRepository = DocumentRepository()) : ViewModel() {
    var documents by mutableStateOf<List<DocumentModel>>(emptyList())
    var isLoading by mutableStateOf(false)

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUid = currentUser?.uid ?: ""
        val currentEmail = currentUser?.email ?: ""

        viewModelScope.launch {
            isLoading = true
            val result = repository.getAllDocuments()
            result.onSuccess { allDocs ->
                // Điều kiện hiển thị tài liệu:
                // 1. File do chính mình tạo
                // 2. File được đặt ở chế độ PUBLIC (Mọi người)
                // 3. File LINK_ONLY nhưng email của mình nằm trong danh sách sharedEmails
                documents = allDocs.filter { doc ->
                    doc.authorUid == currentUid ||
                            doc.visibility == "PUBLIC" ||
                            (doc.visibility == "LINK_ONLY" && doc.sharedEmails.contains(currentEmail))
                }
            }
            isLoading = false
        }
    }

    fun updateVisibility(docId: String, visibility: String) {
        viewModelScope.launch {
            repository.updateVisibility(docId, visibility).onSuccess {
                loadDocuments() // Tải lại danh sách sau khi cập nhật
            }
        }
    }
}