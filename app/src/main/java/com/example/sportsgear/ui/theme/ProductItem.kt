package com.example.sportsgear.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.sportsgear.models.ProductModel

@Composable
fun ProductItem(product: ProductModel) {
    Card(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Load the product image using Coil
            Image(
                painter = rememberAsyncImagePainter(product.imageUri),
                contentDescription = product.name
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                // Product details
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(product.category)
                Text("Price: $${product.price}")
            }
        }
    }
}
