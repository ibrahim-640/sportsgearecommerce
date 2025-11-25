package com.example.sportsgear.ui.screens.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.models.Product
import com.example.sportsgear.navigation.ROUTE_UPDATE_PRODUCT

val Maroon = Color(0xFF800000)
val MaroonDark = Color(0xFF4B0000)
val MaroonLight = Color(0xFFB22222)

@Composable
fun ViewProductsScreen(
    navController: NavHostController,
    userId: String,
    cartViewModel: CartViewModel,
    productViewModel: ProductViewModel = viewModel()
) {
    val context = LocalContext.current

    // ✅ Collect products from the ViewModel in real time
    val productList by productViewModel.productList.collectAsState()

    // ✅ Load all products once (this listens for live changes automatically)
    LaunchedEffect(Unit) {
        productViewModel.fetchProducts()

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "All Products",
            fontSize = 28.sp,
            fontFamily = FontFamily.SansSerif,
            color = MaroonDark,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (productList.isEmpty()) {
            Text(
                text = "No Products Found",
                color = Color.Gray,
                fontSize = 18.sp,
                fontFamily = FontFamily.SansSerif
            )
        } else {
            LazyColumn {
                items(productList) { product ->
                    ProductItem(
                        product = product,
                        navController = navController,
                        productViewModel = productViewModel,
                        userId = userId,
                        cartViewModel = cartViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    navController: NavHostController,
    productViewModel: ProductViewModel,
    userId: String,
    cartViewModel: CartViewModel
) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(320.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Maroon)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // ✅ Product image
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(180.dp)
                    .height(180.dp)
                    .padding(10.dp)
            )

            // ✅ Product details and actions
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxHeight()
            ) {
                ProductDetail("Name", product.name)
                ProductDetail("Price", product.price)
                ProductDetail("Category", product.category)
                ProductDetail("Description", product.description)
                ProductDetail("Quantity", product.quantity)
                ProductDetail("Value", product.value)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val context = LocalContext.current

                    Button(
                        onClick = {
                            productViewModel.deleteProduct(product.productId, context, navController)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonDark)
                    ) {
                        Text("REMOVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            navController.navigate("$ROUTE_UPDATE_PRODUCT/${product.productId}")
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonLight)
                    ) {
                        Text("UPDATE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            cartViewModel.addToCart(userId, product)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("ADD TO CART", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetail(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(
            text = label.uppercase(),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (value.isNotEmpty()) value else "N/A",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
