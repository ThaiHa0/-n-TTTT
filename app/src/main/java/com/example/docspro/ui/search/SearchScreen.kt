package com.example.docspro.ui.search

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.docspro.data.model.DocumentModel
import com.example.docspro.data.repository.DocumentRepository
import com.example.docspro.ui.home.HomeViewModel
import com.example.docspro.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
fun SearchScreen(homeViewModel: HomeViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val allDocs = homeViewModel.documents
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val docRepository = remember { DocumentRepository() }

    var selectedDocForShare by remember { mutableStateOf<DocumentModel?>(null) }
    var emailInput by remember { mutableStateOf("") }
    var isPreparingFile by remember { mutableStateOf(false) } // Trạng thái đang tải file tạm

    val filteredDocs = remember(searchQuery, allDocs) {
        if (searchQuery.isBlank()) allDocs
        else allDocs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.author.contains(searchQuery, ignoreCase = true)
        }
    }

    if (selectedDocForShare != null) {
        AlertDialog(
            onDismissRequest = {
                if (!isPreparingFile) {
                    selectedDocForShare = null
                    emailInput = ""
                }
            },
            title = {
                Text(
                    text = "Chia sẻ tài liệu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Tài liệu sẽ gửi: ${selectedDocForShare?.title}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BluePrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Nhập địa chỉ Email nhận") },
                        placeholder = { Text("example@gmail.com") },
                        singleLine = true,
                        enabled = !isPreparingFile,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (isPreparingFile) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = BluePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang chuẩn bị file đính kèm...", fontSize = 13.sp, color = GrayText)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val recipientEmail = emailInput.trim()
                        val doc = selectedDocForShare

                        if (recipientEmail.isNotBlank() && doc != null) {
                            isPreparingFile = true
                            coroutineScope.launch {
                                // 1. Cập nhật danh sách quyền xem ở Firestore
                                docRepository.addSharedEmail(doc.id, recipientEmail)

                                // 2. Tải file từ Cloudinary về bộ nhớ tạm để gửi tệp đính kèm thực tế
                                val localFileUri = downloadTempFile(context, doc.fileUrl, doc.title)

                                isPreparingFile = false

                                if (localFileUri != null) {
                                    try {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/octet-stream"
                                            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                                            putExtra(Intent.EXTRA_SUBJECT, "Tài liệu chia sẻ: ${doc.title}")
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Xin chào,\n\n${doc.author} đã chia sẻ tài liệu \"${doc.title}\" với bạn.\n\n" +
                                                        "Tệp tin đã được đính kèm trực tiếp trong email này.\n\n" +
                                                        "Trân trọng,\nDocSpro App"
                                            )
                                            // Gắn file thực tế vào Email
                                            putExtra(Intent.EXTRA_STREAM, localFileUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Gửi email đính kèm..."))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Không thể mở ứng dụng Email", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Lỗi khi tải file tạm để đính kèm", Toast.LENGTH_SHORT).show()
                                }

                                selectedDocForShare = null
                                emailInput = ""
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng nhập Email hợp lệ!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isPreparingFile,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Gửi Mail", color = White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedDocForShare = null
                        emailInput = ""
                    },
                    enabled = !isPreparingFile
                ) {
                    Text("Hủy", color = GrayText)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueLightBackground)
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search document...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BluePrimary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = BluePrimary.copy(alpha = 0.5f),
                focusedContainerColor = White,
                unfocusedContainerColor = White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "All documents (${filteredDocs.size})",
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredDocs) { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(doc.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Author: ${doc.author}", fontSize = 12.sp, color = TextSecondary)
                            Text("${doc.size} • ${doc.date}", fontSize = 11.sp, color = GrayText)
                        }
                        IconButton(onClick = { selectedDocForShare = doc }) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = BluePrimary)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HÀM TẢI FILE TỪ URL VỀ CACHE VÀ TẠO URI QUA FILEPROVIDER
// -------------------------------------------------------------
private suspend fun downloadTempFile(context: Context, fileUrl: String, fileName: String): Uri? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(fileUrl)
            val cacheDir = File(context.cacheDir, "shared_files")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val tempFile = File(cacheDir, fileName)
            url.openStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}