package com.example.sportsgear.ui.theme.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.OrderViewModel
import com.example.sportsgear.navigation.ROUTE_CHECKOUT

val CustomMaroon = Color(0xFF800000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    userId: String,
    cartViewModel: CartViewModel,
    navController: NavHostController,
    orderViewModel: OrderViewModel = viewModel()
) {
    val context = LocalContext.current
    val cartItems by cartViewModel.cartItems
    val subtotal by cartViewModel.subtotal
    val tax by cartViewModel.tax
    val shipping by cartViewModel.shipping
    val total by cartViewModel.total

    LaunchedEffect(Unit) {
        cartViewModel.loadCartItems(userId)
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Your Cart", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CustomMaroon)
            )
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            EmptyCartState(modifier = Modifier.padding(paddingValues))
        } else {
            CartContent(
                cartItems = cartItems,
                userId = userId,
                cartViewModel = cartViewModel,
                subtotal = subtotal,
                tax = tax,
                shipping = shipping,
                total = total,
                navController = navController,
                screenWidth = screenWidth,
                paddingValues = paddingValues
            )
        }
    }
}

@Composable
fun EmptyCartState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = "Empty Cart",
            tint = CustomMaroon.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your cart is empty",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = CustomMaroon,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add some amazing sports gear to get started!",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CartContent(
    cartItems: List<com.example.sportsgear.models.CartItem>,
    userId: String,
    cartViewModel: CartViewModel,
    subtotal: Double,
    tax: Double,
    shipping: Double,
    total: Double,
    navController: NavHostController,
    screenWidth: Int,
    paddingValues: PaddingValues
) {
    if (screenWidth < 600) {
        // Mobile Layout
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            CartItemsList(
                cartItems = cartItems,
                userId = userId,
                cartViewModel = cartViewModel,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OrderSummarySection(
                subtotal = subtotal,
                tax = tax,
                shipping = shipping,
                total = total,
                onCheckoutClick = {
                    navController.navigate(ROUTE_CHECKOUT)
                }
            )
        }
    } else {
        // Tablet/Desktop Layout
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cart Items - 2/3 width
            Box(modifier = Modifier.weight(2f)) {
                CartItemsList(
                    cartItems = cartItems,
                    userId = userId,
                    cartViewModel = cartViewModel
                )
            }

            // Order Summary - 1/3 width
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                OrderSummarySection(
                    subtotal = subtotal,
                    tax = tax,
                    shipping = shipping,
                    total = total,
                    onCheckoutClick = {
                        navController.navigate(ROUTE_CHECKOUT)
                    }
                )
            }
        }
    }
}

@Composable
fun CartItemsList(
    cartItems: List<com.example.sportsgear.models.CartItem>,
    userId: String,
    cartViewModel: CartViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cartItems) { item ->
            CartItemCard(
                item = item,
                userId = userId,
                cartViewModel = cartViewModel
            )
        }
    }
}

@Composable
fun CartItemCard(
    item: com.example.sportsgear.models.CartItem,
    userId: String,
    cartViewModel: CartViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Product Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ksh ${item.price}",
                    color = CustomMaroon,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Quantity Controls
                QuantityControls(
                    quantity = item.quantity,
                    onDecrease = {
                        if (item.quantity > 1) {
                            cartViewModel.updateQuantity(userId, item.productId, item.quantity - 1)
                        }
                    },
                    onIncrease = {
                        cartViewModel.updateQuantity(userId, item.productId, item.quantity + 1)
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete Button
            IconButton(
                onClick = {
                    cartViewModel.removeFromCart(userId, item.productId)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove from cart",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun QuantityControls(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Decrease Button
        IconButton(
            onClick = onDecrease,
            modifier = Modifier.size(32.dp),
            enabled = quantity > 1
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Decrease quantity",
                tint = if (quantity > 1) CustomMaroon else Color.Gray
            )
        }

        // Quantity Display
        Text(
            text = quantity.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )

        // Increase Button
        IconButton(
            onClick = onIncrease,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Increase quantity",
                tint = CustomMaroon
            )
        }
    }
}

@Composable
fun OrderSummarySection(
    subtotal: Double,
    tax: Double,
    shipping: Double,
    total: Double,
    onCheckoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Order Summary",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = CustomMaroon
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Summary Rows
            SummaryRow("Subtotal", "Ksh ${"%.2f".format(subtotal)}")
            SummaryRow("Tax (5%)", "Ksh ${"%.2f".format(tax)}")
            SummaryRow("Shipping", "Ksh ${"%.2f".format(shipping)}")

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = CustomMaroon.copy(alpha = 0.2f)
            )

            SummaryRow(
                label = "Total",
                value = "Ksh ${"%.2f".format(total)}",
                bold = true,
                highlight = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Single Checkout Button
            Button(
                onClick = onCheckoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CustomMaroon),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Proceed to Checkout",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Help Text
            Text(
                text = "You'll complete payment in the next step",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    bold: Boolean = false,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) CustomMaroon else Color.Black,
            fontSize = if (highlight) 18.sp else 14.sp
        )
        Text(
            text = value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) CustomMaroon else Color.Black,
            fontSize = if (highlight) 18.sp else 14.sp
        )
    }
}