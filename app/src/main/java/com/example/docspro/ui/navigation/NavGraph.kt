package com.example.docspro.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.docspro.ui.auth.AuthViewModel
import com.example.docspro.ui.auth.LoginScreen
import com.example.docspro.ui.auth.LogupScreen
import com.example.docspro.ui.home.HomeScreen
import com.example.docspro.ui.home.HomeViewModel
import com.example.docspro.ui.profile.ProfileScreen
import com.example.docspro.ui.profile.ProfileViewModel
import com.example.docspro.ui.search.SearchScreen
import com.example.docspro.ui.upload.UploadScreen
import com.example.docspro.ui.upload.UploadViewModel
import com.example.docspro.ui.theme.*

@Composable
fun MainAppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    uploadViewModel: UploadViewModel,
    profileViewModel: ProfileViewModel
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Upload,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Kiểm tra xem màn hình hiện tại có hiển thị Bottom Bar không
    val showBottomBar = currentRoute in items.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = White) {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BluePrimary,
                                selectedTextColor = BluePrimary,
                                indicatorColor = BlueCardBackground
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        homeViewModel.loadDocuments()
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }

            composable("register") {
                LogupScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate("login") }
                )
            }

            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onUploadClick = { navController.navigate(BottomNavItem.Upload.route) }
                )
            }

            composable(BottomNavItem.Search.route) {
                SearchScreen(homeViewModel = homeViewModel)
            }

            composable(BottomNavItem.Upload.route) {
                UploadScreen(
                    viewModel = uploadViewModel,
                    onUploadSuccess = { homeViewModel.loadDocuments() }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}