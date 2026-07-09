package com.example.sportsgear.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.navigation.ROUTE_PRODUCT_DETAIL
import com.example.sportsgear.navigation.getProductDetailRoute
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    categoryName: String,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel // ✅ FIX — now passed in from AppNavHost instead of
    // creating its own instance, so this screen shares the same already-loaded
    // cartItems/Firebase listener as Home, Cart, Checkout, etc.
) {
    val productViewModel: ProductViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val allProducts by productViewModel.productList.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts()
    }

    val filteredProducts = remember(allProducts, categoryName) {
        allProducts.filter {
            it.category.equals(categoryName, ignoreCase = true)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Category: $categoryName",
                        color = MaroonDark,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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

                filteredProducts.isEmpty() -> {
                    Text(
                        text = "No products found in $categoryName",
                        color = MaroonDark.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ProductCard(
                                product = product,
                                isAdmin = isAdmin == true,
                                onClick = {
                                    navController.navigate(getProductDetailRoute(product.productId))
                                },
                                onEditClick = {
                                    navController.navigate(
                                        "update_product/${product.productId}"
                                    )
                                },
                                onDeleteClick = {
                                    productViewModel.deleteProduct(
                                        product.productId,
                                        navController = navController,
                                        isAdmin = isAdmin == true
                                    )
                                },
                                onAddToCart = {
                                    val userId = currentUser?.uid
                                    if (userId != null) {
                                        cartViewModel.addToCart(userId, product)
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Please log in to add to cart"
                                            )
                                        }
                                    }
                                },
                                productViewModel = productViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}