package com.example.docspro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.docspro.ui.auth.AuthViewModel
import com.example.docspro.ui.home.HomeViewModel
import com.example.docspro.ui.navigation.MainAppNavGraph
import com.example.docspro.ui.profile.ProfileViewModel
import com.example.docspro.ui.theme.DocSproTheme
import com.example.docspro.ui.upload.UploadViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocSproTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val homeViewModel: HomeViewModel = viewModel()
                val uploadViewModel: UploadViewModel = viewModel()
                val profileViewModel: ProfileViewModel = viewModel()

                MainAppNavGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    homeViewModel = homeViewModel,
                    uploadViewModel = uploadViewModel,
                    profileViewModel = profileViewModel
                )
            }
        }
    }
}