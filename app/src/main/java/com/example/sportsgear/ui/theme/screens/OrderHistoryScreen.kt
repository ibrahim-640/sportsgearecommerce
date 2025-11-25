package com.example.sportsgear.ui.theme.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sportsgear.data.OrderViewModel
@Composable
fun OrderHistoryScreen(viewModel: OrderViewModel = viewModel()) {
    val orders = viewModel.orders

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(orders) { order ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order Date: ${order.orderDate}", style = MaterialTheme.typography.titleMedium)
                    Text("Total: $${order.total}", style = MaterialTheme.typography.titleMedium, color = Color.Green)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Items:", style = MaterialTheme.typography.labelLarge)

                    order.items.forEach { item ->
                        Text("- ${item.name} x${item.quantity} @ $${item.price}")
                    }
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                Text("No past orders.", modifier = Modifier.padding(top = 32.dp))
            }
        }
    }
}
