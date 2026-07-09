package com.example.sportsgear.ui.theme.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportsgear.R
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.navigation.ROUTE_ADMIN_DASHBOARD
import com.example.sportsgear.navigation.ROUTE_EDIT_PROFILE
import com.example.sportsgear.navigation.ROUTE_LOGIN
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel // ✅ FIX — shared instance from AppNavHost
) {
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val isAdmin by authViewModel.isAdmin.collectAsState(initial = null)
    var firstName by remember { mutableStateOf<String?>(null) }
    var lastName by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var isLoadingProfile by remember { mutableStateOf(true) }

    LaunchedEffect(currentUser) {
        isLoadingProfile = true
        authViewModel.getCurrentUserProfile { f, l, e ->
            firstName = f
            lastName = l
            email = e
            isLoadingProfile = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile", color = MaroonDark, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaroonDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Maroon.copy(alpha = 0.05f)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Maroon.copy(alpha = 0.05f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            if (isLoadingProfile) {
                CircularProgressIndicator(color = Maroon)
                return@Column
            }

            AsyncImage(
                model = R.drawable.img,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val displayName = buildString {
                append((firstName ?: "Unknown").replaceFirstChar { it.uppercase() })
                append(" ")
                append((lastName ?: "").replaceFirstChar { it.uppercase() })
            }.trim()

            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaroonDark
                )
            )

            Text(
                text = email ?: "No email found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaroonDark.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileOption(icon = Icons.Default.List, title = "Order History") {
                navController.navigate(com.example.sportsgear.navigation.ROUTE_ORDER)
            }
            HorizontalDivider(color = Maroon.copy(alpha = 0.1f))

            ProfileOption(icon = Icons.Default.Edit, title = "Edit Profile") {
                navController.navigate(ROUTE_EDIT_PROFILE)
            }
            HorizontalDivider(color = Maroon.copy(alpha = 0.1f))

            if (isAdmin == true) {
                ProfileOption(
                    icon = Icons.Filled.Dashboard,
                    title = "Admin Dashboard"
                ) {
                    navController.navigate(ROUTE_ADMIN_DASHBOARD)
                }
                HorizontalDivider(color = Maroon.copy(alpha = 0.1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    authViewModel.logout()
                    // ✅ FIX — was popUpTo(ROUTE_STARTER) which is a no-op if
                    // ROUTE_STARTER is no longer in the back stack (it's cleared
                    // by the post-login navigation). Using popUpTo(0) instead
                    // unconditionally clears the entire back stack, so the user
                    // can't press back into the authenticated area after logging out.
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaroonDark)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = MaroonDark
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaroonDark.copy(alpha = 0.5f)
        )
    }
}

// Note: removed the @Preview composable, since this screen now requires an
// authViewModel parameter Preview can't supply without a fake/mock instance.