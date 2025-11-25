package com.example.sportsgear.ui.theme.screens
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportsgear.R
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.navigation.ROUTE_ADMIN_DASHBOARD
import com.example.sportsgear.navigation.ROUTE_EDIT_PROFILE
import com.example.sportsgear.navigation.ROUTE_LOGIN
import com.example.sportsgear.navigation.ROUTE_ORDER

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    // Provide safe initial values to collectAsState
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val isAdmin by authViewModel.isAdmin.collectAsState(initial = null)

    // Use remember for state objects created during composition
    var firstName by remember { mutableStateOf<String?>(null) }
    var lastName by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }

    // Re-fetch profile whenever user changes
    LaunchedEffect(currentUser) {
        authViewModel.getCurrentUserProfile { f, l, e ->
            firstName = f
            lastName = l
            email = e
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // LOADING FIX
            if (firstName == null || lastName == null || email == null) {
                CircularProgressIndicator()
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

            Text(
                text = "${firstName!!.replaceFirstChar { it.uppercase() }} ${lastName!!.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = email!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileOption(icon = Icons.Default.List, title = "Order History") {
                navController.navigate(ROUTE_ORDER)
            }

            ProfileOption(icon = Icons.Default.Edit, title = "Edit Profile") {
                navController.navigate(ROUTE_EDIT_PROFILE)
            }

            if (isAdmin == true) {
                ProfileOption(icon = Icons.Filled.Dashboard, title = "Admin Dashboard") {
                    navController.navigate(ROUTE_ADMIN_DASHBOARD)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    authViewModel.logout(context)
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
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
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}
