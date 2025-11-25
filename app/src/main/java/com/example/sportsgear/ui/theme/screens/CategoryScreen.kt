package com.example.sportsgear.ui.theme.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.models.Product
import com.example.sportsgear.navigation.ROUTE_PRODUCT_DETAIL
import com.example.sportsgear.ui.theme.screens.ProductCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    categoryName: String,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel
) {
    val allProducts by productViewModel.productList.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val filteredProducts = allProducts.filter {
        it.category.equals(categoryName, ignoreCase = true)
    }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category: $categoryName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No products found in $categoryName")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 8.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        navController = navController,
                        isAdmin = false,
                        onClick = {
                            navController.navigate("$ROUTE_PRODUCT_DETAIL/${product.productId}")
                        },
                        onAddToCart = {
                            val userId = currentUser?.uid
                            if (userId != null) {
                                cartViewModel.addToCart(userId, product)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please log in to add to cart",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        viewModel = productViewModel
                    )


                }
            }
        }
    }
}