package com.example.docspro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Search : BottomNavItem("search", "Search", Icons.Default.Search)
    object Upload : BottomNavItem("upload", "Upload", Icons.Default.AddCircle)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.Person)
}