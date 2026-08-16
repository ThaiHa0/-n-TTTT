package com.example.docspro.data.model

data class DocumentModel(
    val id: String = "",
    val title: String = "",
    val fileType: String = "OTHER", // PDF, WORD, EXCEL, OTHER
    val size: String = "0 KB",
    val author: String = "",
    val authorUid: String = "",
    val date: String = "",
    val fileUrl: String = "",
    val visibility: String = "PUBLIC", // ONLY_ME, PUBLIC, LINK_ONLY
    val sharedEmails: List<String> = emptyList() // Danh sách email được nhận link qua trang Search
)