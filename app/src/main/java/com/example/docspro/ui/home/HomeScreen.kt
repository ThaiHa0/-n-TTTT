package com.example.docspro.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docspro.data.model.DocumentModel
import com.example.docspro.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onUploadClick: () -> Unit
) {
    // Tự động load lại danh sách khi vào màn hình Home
    LaunchedEffect(Unit) {
        viewModel.loadDocuments()
    }

    var selectedDateFilter by remember { mutableStateOf("Newest") }
    var selectedTypeFilter by remember { mutableStateOf("All") }

    val documents = viewModel.documents
    val context = LocalContext.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Logic kết hợp lọc Loại File (Type) và Ngày Tháng (Date)
    val filteredDocuments = remember(documents, selectedTypeFilter, selectedDateFilter) {
        documents.filter { doc ->
            // 1. Lọc theo định dạng file
            val matchesType = if (selectedTypeFilter == "All") true
            else doc.fileType.equals(selectedTypeFilter, ignoreCase = true)

            // 2. Lọc theo thời gian ngày tạo
            val matchesDate = filterByDate(doc.date, selectedDateFilter)

            matchesType && matchesDate
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
                    Text("Document Library", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${documents.size} documents available", fontSize = 14.sp, color = TextSecondary)
                }
                Icon(Icons.Default.Home, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(32.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Chưa có tài liệu nào trên hệ thống",
                        fontSize = 16.sp,
                        color = GrayText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onUploadClick,
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Tải file lên ngay", color = White)
                    }
                }
            }
        } else {
            // FILTER BY DATE (Đã kích hoạt click)
            Text("Filter by Date", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Newest", "Today", "Yesterday", "This week", "Last week")) { filter ->
                    FilterChip(
                        selected = selectedDateFilter == filter,
                        onClick = { selectedDateFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FILTER BY TYPE
            Text("Filter by Type", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "PDF", "WORD", "EXCEL", "OTHER")) { type ->
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = { selectedTypeFilter = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredDocuments) { doc ->
                    var showMenu by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (doc.fileUrl.isNotBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(doc.fileUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Không thể mở liên kết tài liệu", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Tài liệu không có URL khả dụng", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getFileIcon(doc.fileType, doc.title),
                                contentDescription = null,
                                tint = getFileIconColor(doc.fileType, doc.title),
                                modifier = Modifier.size(36.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = doc.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${doc.size}  •  ${doc.author}  •  ${doc.date}",
                                    fontSize = 12.sp,
                                    color = GrayText
                                )
                            }

                            Box {
                                IconButton(onClick = {
                                    if (doc.authorUid == currentUid) {
                                        showMenu = true
                                    } else {
                                        Toast.makeText(context, "Chỉ tác giả mới có quyền sửa chế độ xem", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = GrayText)
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (doc.visibility == "ONLY_ME") "✓ Chỉ mình tôi" else "Chỉ mình tôi") },
                                        onClick = {
                                            viewModel.updateVisibility(doc.id, "ONLY_ME")
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (doc.visibility == "PUBLIC") "✓ Mọi người" else "Mọi người") },
                                        onClick = {
                                            viewModel.updateVisibility(doc.id, "PUBLIC")
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (doc.visibility == "LINK_ONLY") "✓ Người có link chia sẻ" else "Người có link chia sẻ") },
                                        onClick = {
                                            viewModel.updateVisibility(doc.id, "LINK_ONLY")
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// THUẬT TOÁN LỌC NGÀY THÁNG CỦA FILE (dd/MM/yyyy)
// -------------------------------------------------------------
private fun filterByDate(dateString: String, filterType: String): Boolean {
    if (filterType == "Newest") return true

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val docDate: Date = try {
        sdf.parse(dateString) ?: return true
    } catch (e: Exception) {
        return true
    }

    val todayCalendar = Calendar.getInstance()

    // Đặt về 00:00:00 của ngày hôm nay
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return when (filterType) {
        "Today" -> docDate.after(todayStart.time) || docDate == todayStart.time
        "Yesterday" -> {
            val yesterdayStart = (todayStart.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
            docDate.after(yesterdayStart.time) && docDate.before(todayStart.time)
        }
        "This week" -> {
            val thisWeekStart = (todayStart.clone() as Calendar).apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            }
            docDate.after(thisWeekStart.time) || docDate == thisWeekStart.time
        }
        "Last week" -> {
            val thisWeekStart = (todayStart.clone() as Calendar).apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            }
            val lastWeekStart = (thisWeekStart.clone() as Calendar).apply { add(Calendar.WEEK_OF_YEAR, -1) }
            docDate.after(lastWeekStart.time) && docDate.before(thisWeekStart.time)
        }
        else -> true
    }
}

@Composable
private fun getFileIcon(fileType: String, title: String): ImageVector {
    return when {
        fileType.equals("PDF", ignoreCase = true) || title.endsWith(".pdf", ignoreCase = true) -> Icons.Default.PictureAsPdf
        fileType.equals("WORD", ignoreCase = true) || title.endsWith(".doc", ignoreCase = true) || title.endsWith(".docx", ignoreCase = true) -> Icons.Default.Description
        fileType.equals("EXCEL", ignoreCase = true) || title.endsWith(".xls", ignoreCase = true) || title.endsWith(".xlsx", ignoreCase = true) -> Icons.Default.TableChart
        fileType.equals("POWERPOINT", ignoreCase = true) || title.endsWith(".ppt", ignoreCase = true) || title.endsWith(".pptx", ignoreCase = true) -> Icons.Default.Slideshow
        fileType.equals("IMAGE", ignoreCase = true) || title.endsWith(".png", ignoreCase = true) || title.endsWith(".jpg", ignoreCase = true) -> Icons.Default.Image
        else -> Icons.Default.InsertDriveFile
    }
}

@Composable
private fun getFileIconColor(fileType: String, title: String): Color {
    return when {
        fileType.equals("PDF", ignoreCase = true) || title.endsWith(".pdf", ignoreCase = true) -> Color(0xFFE53935)
        fileType.equals("WORD", ignoreCase = true) || title.endsWith(".doc", ignoreCase = true) || title.endsWith(".docx", ignoreCase = true) -> Color(0xFF1E88E5)
        fileType.equals("EXCEL", ignoreCase = true) || title.endsWith(".xls", ignoreCase = true) || title.endsWith(".xlsx", ignoreCase = true) -> Color(0xFF43A047)
        fileType.equals("POWERPOINT", ignoreCase = true) || title.endsWith(".ppt", ignoreCase = true) || title.endsWith(".pptx", ignoreCase = true) -> Color(0xFFFB8C00)
        fileType.equals("IMAGE", ignoreCase = true) || title.endsWith(".png", ignoreCase = true) || title.endsWith(".jpg", ignoreCase = true) -> Color(0xFF8E24AA)
        else -> Color(0xFF757575)
    }
}