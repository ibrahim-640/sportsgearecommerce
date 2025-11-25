package com.example.sportsgear.ui.theme.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.OrderViewModel
import com.example.sportsgear.models.Order
import com.example.sportsgear.navigation.ROUTE_HOME
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessScreen(
    navController: NavHostController,
    amount: String,
    method: String,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val orderNumber = "SG-${(1000..9999).random()}"
    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    // ✅ Save order to Firebase when screen is loaded
    orderViewModel.createOrderFromSuccessScreen(userId, amount.toDoubleOrNull() ?: 0.0, method, orderNumber)

    // ✅ Clear the cart
    cartViewModel.clearCart(userId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Confirmation") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF800000))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Confirmation Icon
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Order Details Card
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Payment Successful!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Order Number: $orderNumber", fontSize = 16.sp)
                    Text("Date: $date", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Amount Paid: Ksh $amount", fontWeight = FontWeight.SemiBold)
                    Text("Payment Method: $method", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estimated Delivery: 2-5 business days", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Continue Shopping Button
            Button(
                onClick = { navController.navigate(ROUTE_HOME) { popUpTo(0) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Shop")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue Shopping", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
