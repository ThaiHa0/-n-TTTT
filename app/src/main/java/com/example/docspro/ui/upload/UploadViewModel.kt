package com.example.docspro.ui.upload

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docspro.data.model.DocumentModel
import com.example.docspro.data.repository.AuthRepository
import com.example.docspro.data.repository.DocumentRepository
import kotlinx.coroutines.launch

class UploadViewModel(
    private val docRepository: DocumentRepository = DocumentRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var myDocuments by mutableStateOf<List<DocumentModel>>(emptyList())
    var isUploading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        loadUserDocuments()
    }

    // Hàm chỉ lấy tài liệu thuộc về User đang đăng nhập (dựa theo authorUid)
    fun loadUserDocuments() {
        val uid = authRepository.getCurrentUserUid() ?: return
        viewModelScope.launch {
            val result = docRepository.getUserDocuments(uid)
            result.onSuccess { docs ->
                myDocuments = docs
            }.onFailure { e ->
                errorMessage = e.message
            }
        }
    }

    fun uploadFile(
        context: Context,
        fileUri: Uri,
        fileName: String,
        fileType: String,
        onComplete: () -> Unit
    ) {
        val uid = authRepository.getCurrentUserUid() ?: run {
            errorMessage = "Chưa đăng nhập!"
            return
        }

        viewModelScope.launch {
            isUploading = true
            errorMessage = null

            try {
                val username = authRepository.getUserProfile(uid).getOrNull()?.username ?: "User"

                val uploadResult = docRepository.uploadDocument(
                    context = context,
                    fileUri = fileUri,
                    title = fileName,
                    fileType = fileType,
                    author = username,
                    authorUid = uid
                )

                uploadResult.onSuccess {
                    loadUserDocuments() // Tải lại danh sách sau khi upload thành công
                    onComplete()
                }.onFailure { e ->
                    errorMessage = "Lỗi tải lên: ${e.localizedMessage}"
                }
            } catch (e: Exception) {
                errorMessage = "Có lỗi xảy ra: ${e.localizedMessage}"
            } finally {
                isUploading = false
            }
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            val result = docRepository.deleteDocument(id)
            result.onSuccess {
                loadUserDocuments() // Tải lại danh sách sau khi xóa thành công
            }.onFailure { e ->
                errorMessage = "Lỗi xóa file: ${e.localizedMessage}"
            }
        }
    }
}