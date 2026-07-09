package com.example.sportsgear.ui.theme.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.OrderViewModel
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    authViewModel: AuthViewModel, // ✅ NEW — shared instance from AppNavHost,
    // supplies the one canonical isAdmin flag
    orderViewModel: OrderViewModel = viewModel()
) {
    val isAdmin by authViewModel.isAdmin.collectAsState()
    val orders = orderViewModel.orders
    val isLoading by orderViewModel.isLoading.collectAsState()

    // ✅ FIX — loads the correct order set (everyone's, for admins; just
    // this user's, otherwise) once isAdmin has actually resolved from
    // AuthViewModel, instead of OrderViewModel silently deciding this itself
    // via a Firebase path that never matched real data.
    LaunchedEffect(isAdmin) {
        isAdmin?.let { orderViewModel.loadOrders(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isAdmin == true) "All Orders" else "Order History",
                        color = MaroonDark,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Maroon.copy(alpha = 0.05f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // ✅ NEW — spinner while isAdmin is still resolving OR while
                // the orders listener hasn't returned data yet
                isAdmin == null || isLoading -> {
                    CircularProgressIndicator(
                        color = Maroon,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                orders.isEmpty() -> {
                    Text(
                        "No past orders.",
                        color = MaroonDark.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(orders) { order ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        "Order Date: ${order.orderDate}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaroonDark
                                    )
                                    Text(
                                        // ✅ FIX — was "$${order.total}", now
                                        // matches Ksh used everywhere else
                                        "Total: Ksh ${order.total}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Maroon,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Items:",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaroonDark
                                    )

                                    // ✅ NEW — fallback for orders placed before
                                    // this fix, which will still have an empty
                                    // items list saved in Firebase
                                    if (order.items.isEmpty()) {
                                        Text(
                                            "No item details for this order",
                                            color = MaroonDark.copy(alpha = 0.5f)
                                        )
                                    } else {
                                        order.items.forEach { item ->
                                            Text(
                                                "- ${item.name} x${item.quantity} @ Ksh ${item.price}",
                                                color = MaroonDark.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}