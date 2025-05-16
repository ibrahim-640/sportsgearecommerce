package com.example.sportsgear.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.sportsgear.R

@Composable
fun CartScreen(navController: NavHostController) {
    val cartItems = listOf(
        CartItem("Footwear", "Nike Running Shoes", 2, 120.0, R.drawable.img),
        CartItem("Apparel", "Adidas T-Shirt", 1, 35.0, R.drawable.img),
        CartItem("Accessories", "Fitness Watch", 1, 80.0, R.drawable.img),
        CartItem("Equipment", "Yoga Mat", 1, 25.0, R.drawable.img),
        CartItem("Offers", "Discounted Headband", 3, 5.0, R.drawable.img),
        CartItem("New Arrivals", "Puma Jacket", 1, 90.0, R.drawable.img)
    )

    val totalAmount = cartItems.sumOf { it.quantity * it.price }

    // State to manage the selected item for payment
    var selectedItemForPayment by remember { mutableStateOf<CartItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Your Cart", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(cartItems) { item ->
                CartItemCard(item = item, onItemClick = {
                    selectedItemForPayment = it
                })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Divider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total", style = MaterialTheme.typography.titleMedium)
            Text("$${"%.2f".format(totalAmount)}", style = MaterialTheme.typography.titleMedium)
        }

        Button(
            onClick = { /* Handle checkout */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Proceed to Checkout")
        }
    }

    // M-Pesa Payment Dialog
    if (selectedItemForPayment != null) {
        AlertDialog(
            onDismissRequest = { selectedItemForPayment = null },
            title = {
                Text("Pay with M-Pesa")
            },
            text = {
                Text("Do you want to pay KES ${selectedItemForPayment!!.price} for ${selectedItemForPayment!!.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Simulate payment (replace this with real M-Pesa logic)
                        selectedItemForPayment = null
                        // Show a toast/snackbar or log message if needed
                    }
                ) {
                    Text("Pay Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItemForPayment = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class CartItem(
    val category: String,
    val name: String,
    val quantity: Int,
    val price: Double,
    val imageResId: Int
)

@Composable
fun CartItemCard(item: CartItem, onItemClick: (CartItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = item.imageResId),
                contentDescription = item.name,
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(item.category, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Qty: ${item.quantity}")
                Text("Price: $${item.price}")
            }
        }
    }
}
