package com.example.sportsgear.ui.theme.screens
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.models.CartItem
import com.example.sportsgear.models.Product
@Composable
fun EditCartProductScreen(
    product: Product,
    userId: String,
    cartViewModel: CartViewModel,
    navController: NavController
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf<String>(product.name) }
    var price by remember { mutableStateOf<String>(product.price.toString()) }
    var quantity by remember { mutableStateOf<String>(product.quantity.toString()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Edit Cart Product",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                // Convert the Product to a CartItem for update
                val updatedCartItem = CartItem(
                    productId = product.productId,
                    name = name.ifBlank { product.name },
                    imageUrl = product.imageUrl,
                    price = price.ifBlank { product.price },
                    quantity = quantity.toIntOrNull() ?: product.quantity.toIntOrNull() ?: 1,
                    category = product.category
                )

                // Call the function with the correct type
                cartViewModel.updateCartProduct(userId, product.productId, updatedCartItem)

                Toast.makeText(context, "Product updated successfully!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Product")
        }

    }
}
