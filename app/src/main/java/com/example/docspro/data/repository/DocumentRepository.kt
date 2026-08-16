package com.example.docspro.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.docspro.data.model.DocumentModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

class DocumentRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // CẤU HÌNH CLOUDINARY
    private val CLOUD_NAME = "awt7ujtc"
    private val UPLOAD_PRESET = "jnoocy53"

    private fun initCloudinary(context: Context) {
        try {
            MediaManager.get()
        } catch (_: Exception) {
            val config = mapOf("cloud_name" to CLOUD_NAME)
            MediaManager.init(context, config)
        }
    }
    suspend fun uploadDocument(
        context: Context,
        fileUri: Uri,
        title: String,
        fileType: String,
        author: String,
        authorUid: String
    ): Result<DocumentModel> {
        initCloudinary(context)

        return try {
            // 1. Tạo file tạm để lấy dữ liệu chuẩn từ Uri
            val tempFile = java.io.File.createTempFile("upload_", null, context.cacheDir)
            context.contentResolver.openInputStream(fileUri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Đánh giá xem file có phải là file tài liệu raw (PDF, Word, Excel...) không
            val isRawDocument = fileType.uppercase() in listOf("PDF", "WORD", "EXCEL", "POWERPOINT", "OTHER")

            val downloadUrl = suspendCancellableCoroutine<String> { continuation ->
                MediaManager.get().upload(tempFile.absolutePath)
                    .unsigned(UPLOAD_PRESET)
                    // Nếu là PDF/Word/Excel thì ép resource_type = raw, ngược lại dùng image
                    .option("resource_type", if (isRawDocument) "raw" else "image")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {}
                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                        override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                            tempFile.delete()
                            var url = resultData?.get("secure_url") as? String ?: ""

                            // SỬA LỖI URL: Nếu là file tài liệu mà URL lỡ bị dính "/image/upload/", tự động đổi thành "/raw/upload/"
                            if (isRawDocument && url.contains("/image/upload/")) {
                                url = url.replace("/image/upload/", "/raw/upload/")
                            }

                            continuation.resume(url)
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            tempFile.delete()
                            android.util.Log.e("CloudinaryError", "Lỗi Upload: ${error?.description}")
                            continuation.resume("")
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                    }).dispatch()
            }

            if (downloadUrl.isBlank()) {
                return Result.failure(Exception("Upload file lên Cloudinary thất bại."))
            }

            val fileId = UUID.randomUUID().toString()
            val fileSize = getFileSizeFormatted(context, fileUri)
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            val document = DocumentModel(
                id = fileId,
                title = title,
                fileType = fileType,
                size = fileSize,
                author = author,
                authorUid = authorUid,
                date = currentDate,
                fileUrl = downloadUrl,
                visibility = "PUBLIC",
                sharedEmails = emptyList()
            )

            db.collection("documents").document(fileId).set(document).await()
            Result.success(document)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getAllDocuments(): Result<List<DocumentModel>> {
        return try {
            val snapshot = db.collection("documents").get().await()
            val docs = snapshot.toObjects(DocumentModel::class.java)
            Result.success(docs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserDocuments(authorUid: String): Result<List<DocumentModel>> {
        return try {
            val snapshot = db.collection("documents")
                .whereEqualTo("authorUid", authorUid)
                .get().await()
            val docs = snapshot.toObjects(DocumentModel::class.java)
            Result.success(docs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(documentId: String): Result<Boolean> {
        return try {
            db.collection("documents").document(documentId).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cập nhật chế độ riêng tư (ONLY_ME, PUBLIC, LINK_ONLY)
    suspend fun updateVisibility(documentId: String, visibility: String): Result<Boolean> {
        return try {
            db.collection("documents").document(documentId)
                .update("visibility", visibility).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Thêm email được chia sẻ vào danh sách sharedEmails
    suspend fun addSharedEmail(documentId: String, email: String): Result<Boolean> {
        return try {
            db.collection("documents").document(documentId)
                .update("sharedEmails", FieldValue.arrayUnion(email)).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileSizeFormatted(context: Context, uri: Uri): String {
        var sizeInBytes: Long = 0
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1 && cursor.moveToFirst()) {
                sizeInBytes = cursor.getLong(sizeIndex)
            }
        }
        return when {
            sizeInBytes >= 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", sizeInBytes / (1024.0 * 1024.0))
            sizeInBytes > 0 -> "${sizeInBytes / 1024} KB"
            else -> "Unknown size"
        }
    }
}