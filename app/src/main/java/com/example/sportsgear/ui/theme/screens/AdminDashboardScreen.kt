package com.example.sportsgear.ui.screens.admin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.models.Product
import com.example.sportsgear.navigation.ROUTE_ADD_PRODUCT
import com.example.sportsgear.navigation.ROUTE_UPDATE_PRODUCT
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel // ✅ FIX — was `isAdmin: Boolean = true`, a default
    // that was NEVER overridden by the caller (AppNavHost passed no isAdmin
    // argument at all), meaning any user who reached this route — by any
    // means, not just the gated Home icon — was treated as a full admin,
    // including being able to delete real products. Now derives the real,
    // canonical isAdmin from the shared AuthViewModel instance.
) {
    val productViewModel: ProductViewModel = viewModel()
    val isAdmin by authViewModel.isAdmin.collectAsState()
    val products by productViewModel.productList.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts()
    }

    // ✅ NEW — explicit guard. Even with the real isAdmin wired in, nothing
    // previously stopped this screen's full delete-capable UI from rendering
    // for a non-admin (or while isAdmin is still resolving as null) if they
    // reached this route by any means other than tapping Home's gated icon.
    if (isAdmin != true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Maroon.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            if (isAdmin == null) {
                CircularProgressIndicator(color = Maroon)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Admin access required",
                        color = MaroonDark.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { navController.navigateUp() },
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon)
                    ) {
                        Text("Go Back")
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Admin Dashboard",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(ROUTE_ADD_PRODUCT) },
                containerColor = Maroon
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Maroon.copy(alpha = 0.05f))
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Maroon
                    )
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                productViewModel.clearErrorMessage()
                                productViewModel.fetchProducts()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Maroon)
                        ) {
                            Text("Retry")
                        }
                    }
                }

                products.isEmpty() -> {
                    Text(
                        text = "No products found. Tap + to add one.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaroonDark.copy(alpha = 0.6f)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(products) { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(2.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = product.imageUrl,
                                            contentDescription = product.name,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                product.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaroonDark
                                            )
                                            Text(
                                                "Category: ${product.category}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaroonDark.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                "Price: ksh${product.price}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaroonDark.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row {
                                        Button(
                                            onClick = {
                                                navController.navigate(
                                                    "$ROUTE_UPDATE_PRODUCT/${product.productId}"
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Maroon
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Edit")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                productToDelete = product
                                                showDeleteDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                val product = productToDelete
                if (product != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showDeleteDialog = false
                            productToDelete = null
                        },
                        title = { Text("Confirm Delete", fontWeight = FontWeight.Bold) },
                        text = {
                            Text("Are you sure you want to delete \"${product.name}\"?")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    productViewModel.deleteProduct(
                                        productId = product.productId,
                                        navController = navController,
                                        isAdmin = isAdmin == true,
                                        onSuccess = {
                                            // ✅ FIX — stay on the dashboard after deleting,
                                            // matching HomeScreen's pattern. Previously no
                                            // onSuccess was passed, so deleteProduct's
                                            // fallback (navController.popBackStack()) bounced
                                            // the admin out of the dashboard after every
                                            // single delete — annoying when removing several
                                            // products in a row.
                                        }
                                    )
                                    showDeleteDialog = false
                                    productToDelete = null
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
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    productToDelete = null
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}