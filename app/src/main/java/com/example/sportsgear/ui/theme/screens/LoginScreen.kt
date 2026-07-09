package com.example.sportsgear.ui.theme.screens.login
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sportsgear.R
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.navigation.ROUTE_REGISTER
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark
import com.example.sportsgear.ui.theme.MaroonLight

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel // ✅ FIX — shared instance from AppNavHost
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // ✅ NEW — tracks whether the password is currently visible
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Maroon.copy(alpha = 0.05f))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "LOGIN",
            fontSize = 28.sp,
            color = MaroonDark,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        Image(
            painter = painterResource(R.drawable.img),
            contentDescription = "Sports Gear Logo",
            modifier = Modifier
                .height(140.dp)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = MaroonDark) },
            placeholder = { Text("Enter your email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Maroon,
                unfocusedBorderColor = MaroonLight,
                cursorColor = Maroon
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ✅ Password field with visibility toggle
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = MaroonDark) },
            placeholder = { Text("Enter your password") },
            // ✅ NEW — switches between hidden and visible based on passwordVisible state
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            // ✅ NEW — eye icon button that toggles passwordVisible
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Default.Visibility       // eye open  → tap to hide
                        else
                            Icons.Default.VisibilityOff,   // eye closed → tap to show
                        contentDescription = if (passwordVisible)
                            "Hide password"
                        else
                            "Show password",
                        tint = MaroonDark
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Maroon,
                unfocusedBorderColor = MaroonLight,
                cursorColor = Maroon
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (!isLoading) authViewModel.login(email, password, navController, context)
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
                Text(text = "Login", color = Color.White, fontSize = 16.sp)
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.Red,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Are you a new user? Register here!",
            color = Maroon,
            modifier = Modifier.clickable { navController.navigate(ROUTE_REGISTER) }
        )
    }
}

// Note: removed the @Preview composable, since this screen now requires an
// authViewModel parameter Preview can't supply without a fake/mock instance.