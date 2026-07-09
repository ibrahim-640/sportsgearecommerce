package com.example.sportsgear.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportsgear.R
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark
import com.example.sportsgear.ui.theme.MaroonLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel // ✅ FIX — shared instance from AppNavHost
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val isLoading by authViewModel.isLoading.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser) {
        authViewModel.getCurrentUserProfile { f, l, _ ->
            firstName = f ?: ""
            lastName = l ?: ""
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        color = MaroonDark,
                        fontWeight = FontWeight.Bold
                    )
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

            AsyncImage(
                model = currentUser?.photoUrl ?: R.drawable.img,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name", color = MaroonDark) },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaroonDark)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Maroon,
                    unfocusedBorderColor = MaroonLight,
                    cursorColor = Maroon
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name", color = MaroonDark) },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaroonDark)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Maroon,
                    unfocusedBorderColor = MaroonLight,
                    cursorColor = Maroon
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // ✅ FIX — was `firstName.isBlank() && lastName.isBlank()`,
                    // which only fires when BOTH are empty. A user could save
                    // with no first name as long as they had a last name, while
                    // the message said "please enter at least a first name."
                    // Now only first name is required; last name is optional.
                    if (firstName.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter a first name")
                        }
                        return@Button
                    }
                    authViewModel.updateUserProfile(
                        newFirstName = firstName.trim(),
                        newLastName = lastName.trim(),
                        context = context,
                        onSuccess = { navController.popBackStack() },
                        onError = { error ->
                            scope.launch { snackbarHostState.showSnackbar(error) }
                        }
                    )
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Note: removed the @Preview composable, since this screen now requires an
// authViewModel parameter Preview can't supply without a fake/mock instance.