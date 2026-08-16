package com.example.docspro.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docspro.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    // Tự động load lại thông tin mỗi khi mở giao diện Profile
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    val user = viewModel.userProfile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueLightBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = BlueCardBackground
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = BluePrimary,
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = user?.username ?: "User",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Username", fontSize = 12.sp, color = GrayText)
                        Text(
                            text = if (!user?.username.isNullOrBlank()) user!!.username else "N/A",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = BlueLightBackground
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Email", fontSize = 12.sp, color = GrayText)
                        Text(
                            text = if (!user?.email.isNullOrBlank()) user!!.email else "N/A",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.logout(onLogoutSuccess = onLogout)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log out", fontWeight = FontWeight.Bold, color = White)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}