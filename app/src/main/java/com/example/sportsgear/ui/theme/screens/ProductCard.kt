package com.example.sportsgear.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.models.Product
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark

@Composable
fun ProductCard(
    product: Product,
    isAdmin: Boolean = false,
    onClick: () -> Unit = {},
    onAddToCart: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    productViewModel: ProductViewModel
) {
    val isLoading by productViewModel.isLoading.collectAsState()
    val isValidProduct = product.productId.isNotBlank()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ✅ Derive stock status once — used in two places below
    val quantityInStock = product.quantity.toIntOrNull() ?: 0
    val isInStock = quantityInStock > 0

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {

            // Product image with optional offer badge
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                if (product.onOffer) { // ✅ simplified — OnOffer is non-nullable Boolean
                    Surface(
                        color = Maroon,
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "ON OFFER",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product info
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaroonDark
            )
            Text(
                text = "Ksh ${product.price}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaroonDark.copy(alpha = 0.7f)
            )

            // ✅ Stock status line
            Text(
                text = if (isInStock) "In stock: $quantityInStock" else "Out of stock",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isInStock)
                    MaroonDark.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ✅ Add to Cart — disabled when out of stock
                Button(
                    onClick = { onAddToCart() },
                    enabled = isInStock, // ✅ disabled when quantity is 0
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isInStock) "Add to Cart" else "Out of Stock",
                        color = Color.White
                    )
                }

                // Admin controls — only shown to admins with valid products
                if (isAdmin && isValidProduct) {
                    IconButton(onClick = { onEditClick() }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit product",
                            tint = MaroonDark
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete product",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Confirm Deletion", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to delete \"${product.name}\"? This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}