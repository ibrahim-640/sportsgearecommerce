package com.example.sportsgear.ui.theme.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.R
import com.example.sportsgear.navigation.ROUTE_REGISTER

// Maroon shades
val Maroon = Color(0xFF800000)
val MaroonLight = Color(0xFFB22222)
val MaroonDark = Color(0xFF4B0000)

@Composable
fun LoginScreen(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
            text = " LOGIN",
            fontSize = 28.sp,
            color = MaroonDark,
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        Image(
            painter = painterResource(R.drawable.img),
            contentDescription = "logo",
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Maroon,
                unfocusedBorderColor = MaroonLight,
                cursorColor = Maroon
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = MaroonDark) },
            placeholder = { Text("Enter your password") },
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
                authViewModel.login(email, password, navController, context)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Maroon),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Login", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = buildAnnotatedString { append("Are you a new user? Register here!") },
            color = Maroon,
            modifier = Modifier
                .clickable { navController.navigate(ROUTE_REGISTER) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(rememberNavController())
}
