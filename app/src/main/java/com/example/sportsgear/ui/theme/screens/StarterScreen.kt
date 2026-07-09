package com.example.sportsgear.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sportsgear.R
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.navigation.ROUTE_ADMIN_DASHBOARD
import com.example.sportsgear.navigation.ROUTE_HOME
import com.example.sportsgear.navigation.ROUTE_STARTER
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark

@Composable
fun StartScreen(navController: NavController, authViewModel: AuthViewModel) {
    val isAdmin by authViewModel.isAdmin.collectAsState()
    val fullName by authViewModel.fullName.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Maroon.copy(alpha = 0.05f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // App name
        Text(
            text = "MichezoMall",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaroonDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Personalized greeting
        Text(
            text = if (fullName != null) "Welcome, $fullName!" else "Welcome!",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaroonDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Circular logo image
        Image(
            painter = painterResource(R.drawable.img),
            contentDescription = "MichezoMall logo",
            modifier = Modifier
                .size(260.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tagline
        Text(
            text = "Your game. Your gear.",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaroonDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "Fuel your grind with the right gear",
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = MaroonDark.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Get Started button — disabled until admin check completes
        Button(
            onClick = {
                if (isAdmin == true) {
                    navController.navigate(ROUTE_ADMIN_DASHBOARD) {
                        popUpTo(ROUTE_STARTER) { inclusive = true }
                    }
                } else {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_STARTER) { inclusive = true }
                    }
                }
            },
            enabled = isAdmin != null,
            colors = ButtonDefaults.buttonColors(containerColor = Maroon),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(50.dp)
        ) {
            if (isAdmin == null) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Get Started!",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}
