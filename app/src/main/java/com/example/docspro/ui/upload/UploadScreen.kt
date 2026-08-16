package com.example.docspro.ui.upload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docspro.ui.theme.*

@Composable
fun UploadScreen(
    viewModel: UploadViewModel,
    onUploadSuccess: () -> Unit
) {
    // Tự động tải lại chỉ danh sách tài liệu của User hiện tại khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.loadUserDocuments()
    }

    val myDocuments = viewModel.myDocuments
    val context = LocalContext.current

    // Launcher mở trình chọn file hệ thống
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // 1. Tự động đọc tên file thực tế từ thiết bị
            val realFileName = getFileNameFromUri(context, it)
            // 2. Tự động nhận diện định dạng (WORD, EXCEL, PDF, IMAGE...)
            val realFileType = getFileTypeFromName(realFileName)

            viewModel.uploadFile(
                context = context,
                fileUri = it,
                fileName = realFileName,
                fileType = realFileType,
                onComplete = onUploadSuccess
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueLightBackground)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BlueCardBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("My documents", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${myDocuments.size} documents uploaded", fontSize = 14.sp, color = TextSecondary)
                }
                IconButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.background(BluePrimary, shape = RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Upload", tint = White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isUploading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = BluePrimary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Hiển thị lỗi nếu có
        viewModel.errorMessage?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (myDocuments.isEmpty() && !viewModel.isUploading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bạn chưa tải lên tài liệu nào.\nHãy ấn nút '+' để tải file mới!",
                    color = GrayText,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(myDocuments) { doc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(doc.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("≡ ${doc.fileType}  ★ ${doc.size}", fontSize = 12.sp, color = TextSecondary)
                                Text("👤 ${doc.author}  📅 ${doc.date}", fontSize = 11.sp, color = GrayText)
                            }
                            IconButton(onClick = { viewModel.deleteDocument(doc.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// HÀM BỔ TRỢ ĐỌC TÊN FILE VÀ ĐỊNH DẠNG TỪ URI
// -------------------------------------------------------------

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var name = "Document_${System.currentTimeMillis()}"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}

private fun getFileTypeFromName(fileName: String): String {
    return when {
        fileName.endsWith(".pdf", ignoreCase = true) -> "PDF"
        fileName.endsWith(".doc", ignoreCase = true) || fileName.endsWith(".docx", ignoreCase = true) -> "WORD"
        fileName.endsWith(".xls", ignoreCase = true) || fileName.endsWith(".xlsx", ignoreCase = true) -> "EXCEL"
        fileName.endsWith(".ppt", ignoreCase = true) || fileName.endsWith(".pptx", ignoreCase = true) -> "POWERPOINT"
        fileName.endsWith(".png", ignoreCase = true) || fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "IMAGE"
        else -> "OTHER"
    }
}