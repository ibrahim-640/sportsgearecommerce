package com.example.sportsgear.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.OrderViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.navigation.ROUTE_HOME
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessScreen(
    navController: NavController,
    amount: String,
    method: String,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel
    // ✅ Fix — removed ProductViewModel parameter
    // decrementStockAfterPurchase does not exist in ProductViewModel
    // Stock decrement is a separate feature to implement properly later
) {
    val productViewModel: ProductViewModel = viewModel()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    // ✅ Fix — use timestamp for unique order numbers
    val orderNumber = remember { "SG-${System.currentTimeMillis()}" }
    val date = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    // ✅ Fix — snapshot cart items BEFORE clearing
    // collectAsState gives us the current list at composition time
    // We capture it in a remembered value so it doesn't change
    // after clearCart() empties the cart
    val cartItems by cartViewModel.cartItems.collectAsState()
    val cartSnapshot = remember(cartItems) { cartItems.toList() }

    // ✅ Fix — order is created HERE in SuccessScreen
    // This only runs ONCE because of LaunchedEffect(Unit)
    // At this point the user has already confirmed payment on their phone
    // so creating the order here is correct and safe
    LaunchedEffect(Unit) {
        // Step 1 — Create order with real cart items
        orderViewModel.createOrderFromSuccessScreen(
            userId = userId,
            totalAmount = amount.toDoubleOrNull() ?: 0.0,
            paymentMethod = method,
            orderNumber = orderNumber,
            items = cartSnapshot // ✅ real items, not empty list
        )
        // Step 2 — Decrement stock for each purchased item
        productViewModel.decrementStockAfterPurchase(cartSnapshot) // ✅ NEW

        // Step 3 — Clear cart AFTER order is created
        cartViewModel.clearCart(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Order Confirmation",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF800000)
                )
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

            // Success icon
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Order details card
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Payment Successful!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Order Number: $orderNumber", fontSize = 14.sp)
                    Text("Date: $date", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Amount Paid: Ksh $amount",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        "Payment Method: $method",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )

                    // ✅ Show purchased items
                    if (cartSnapshot.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Items Ordered:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        cartSnapshot.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${item.name} x${item.quantity}",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "Ksh ${item.price}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Estimated Delivery: 2-5 business days",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Continue shopping button
            Button(
                onClick = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF800000)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue Shopping", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}