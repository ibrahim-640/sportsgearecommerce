package com.example.sportsgear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.models.Product
import com.example.sportsgear.navigation.ROUTE_UPDATE_PRODUCT
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark
import com.example.sportsgear.ui.theme.MaroonLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewProductsScreen(navController: NavController) {
    val productViewModel: ProductViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val productList by productViewModel.productList.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val userId = currentUser?.uid ?: ""

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            productViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "All Products",
                        color = MaroonDark,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaroonDark
                        )
                    }
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
                isLoading -> {
                    CircularProgressIndicator(
                        color = Maroon,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                productList.isEmpty() -> {
                    Text(
                        text = "No products found.",
                        color = MaroonDark.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(productList) { product ->
                            ViewProductItem(
                                product = product,
                                navController = navController,
                                productViewModel = productViewModel,
                                userId = userId,
                                cartViewModel = cartViewModel,
                                onCartAdded = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "${product.name} added to cart"
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViewProductItem(
    product: Product,
    navController: NavController,
    productViewModel: ProductViewModel,
    userId: String,
    cartViewModel: CartViewModel,
    onCartAdded: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // ✅ Derive stock status once — used in two places below
    val quantityInStock = product.quantity.toIntOrNull() ?: 0
    val isInStock = quantityInStock > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {

            // Product image
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(130.dp)
                    .height(220.dp) // ✅ slightly taller to fit extra fields
            )

            // Product details
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                // ✅ All model fields restored
                ProductDetail("Name", product.name)
                ProductDetail("Price", "Ksh ${product.price}")
                ProductDetail("Category", product.category)
                ProductDetail("Description", product.description)
                ProductDetail("Quantity", product.quantity)
                ProductDetail("Value", product.value)

                Spacer(modifier = Modifier.height(6.dp))

                // ✅ Stock status indicator
                Text(
                    text = if (isInStock) "In stock: $quantityInStock" else "Out of stock",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isInStock)
                        MaroonDark.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.error
                )

                // Offer badge
                if (product.onOffer) {
                    Surface(
                        color = Maroon,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "ON OFFER",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Delete button
                    Button(
                        onClick = { showDeleteDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Edit button
                    Button(
                        onClick = {
                            navController.navigate(
                                "$ROUTE_UPDATE_PRODUCT/${product.productId}"
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonLight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // ✅ Cart button disabled when out of stock
                    Button(
                        onClick = {
                            if (userId.isNotBlank()) {
                                cartViewModel.addToCart(userId, product)
                                onCartAdded()
                            }
                        },
                        enabled = isInStock, // ✅ disabled when quantity is 0
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cart", color = Color.White, fontWeight = FontWeight.Bold)
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
                Text("Confirm Delete", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Delete \"${product.name}\"? This cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    productViewModel.deleteProduct(
                        product.productId,
                        navController,
                        true
                    )
                    showDeleteDialog = false
                }) {
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

@Composable
fun ProductDetail(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column(modifier = Modifier.padding(bottom = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaroonDark.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaroonDark
        )
    }
}