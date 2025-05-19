package com.example.sportsgear.ui.theme.screens.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.R
import com.example.sportsgear.navigation.ROUTE_LOGIN

// Maroon color palette
val Maroon = Color(0xFF800000)
val MaroonLight = Color(0xFFB22222)
val MaroonDark = Color(0xFF4B0000)

@Composable
fun RegisterScreen(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Maroon.copy(alpha = 0.05f))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Register Here",
            fontSize = 32.sp,
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Normal,
            color = MaroonDark,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = "logo",
            modifier = Modifier
                .height(80.dp)
                .padding(vertical = 12.dp)
        )

        OutlinedTextField(
            value = firstname,
            onValueChange = { firstname = it },
            label = { Text("First Name", color = MaroonDark) },
            placeholder = { Text("Enter first name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person Icon") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Maroon,
                unfocusedBorderColor = MaroonLight,
                cursorColor = Maroon
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = lastname,
            onValueChange = { lastname = it },
            label = { Text("Last Name", color = MaroonDark) },
            placeholder = { Text("Enter last name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person Icon") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Maroon,
                unfocusedBorderColor = MaroonLight,
                cursorColor = Maroon
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = MaroonDark) },
            placeholder = { Text("Enter email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
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
            placeholder = { Text("Enter password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
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
                authViewModel.signup(firstname, lastname, email, password, navController, context)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Maroon),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Register", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = buildAnnotatedString { append("Already registered? Login here") },
            color = Maroon,
            modifier = Modifier
                .clickable {
                    navController.navigate(ROUTE_LOGIN)
                }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(rememberNavController())
}
