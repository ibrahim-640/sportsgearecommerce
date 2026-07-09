package com.example.sportsgear.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.models.CartItem
import com.example.sportsgear.navigation.ROUTE_CHECKOUT
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel // ✅ FIX — now passed in from AppNavHost, shares the
    // same instance (and already-loaded cartItems) as Home, Checkout, etc.
) {

    val currentUser by authViewModel.currentUser.collectAsState()
    val userId = currentUser?.uid ?: ""

    val cartItems by cartViewModel.cartItems.collectAsState()
    val isLoading by cartViewModel.isLoading.collectAsState()
    val message by cartViewModel.message.collectAsState()
    val subtotal by cartViewModel.subtotal
    val tax by cartViewModel.tax
    val shipping by cartViewModel.shipping
    val total by cartViewModel.total

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ✅ REMOVED — loadCartItems is now called once, centrally, in AppNavHost
    // on the shared cartViewModel instance. No need to reattach a listener here.

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Your Cart",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Maroon
                )
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Maroon)
                }
            }

            cartItems.isEmpty() -> {
                EmptyCartState(modifier = Modifier.padding(paddingValues))
            }

            else -> {
                CartContent(
                    cartItems = cartItems,
                    userId = userId,
                    cartViewModel = cartViewModel,
                    subtotal = subtotal,
                    tax = tax,
                    shipping = shipping,
                    total = total,
                    navController = navController,
                    paddingValues = paddingValues
                )
            }
        }
    }
}

@Composable
fun EmptyCartState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Maroon.copy(alpha = 0.05f))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = Maroon.copy(alpha = 0.4f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your cart is empty",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaroonDark,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add some sports gear to get started!",
            fontSize = 15.sp,
            color = MaroonDark.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CartContent(
    cartItems: List<CartItem>,
    userId: String,
    cartViewModel: CartViewModel,
    subtotal: Double,
    tax: Double,
    shipping: Double,
    total: Double,
    navController: NavController,
    paddingValues: PaddingValues
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp

    if (screenWidth < 600) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Maroon.copy(alpha = 0.05f))
                .padding(16.dp)
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
                onCheckoutClick = { navController.navigate(ROUTE_CHECKOUT) }
            )
        }
    } else {
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Maroon.copy(alpha = 0.05f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(modifier = Modifier.weight(2f)) {
                CartItemsList(
                    cartItems = cartItems,
                    userId = userId,
                    cartViewModel = cartViewModel
                )
            }
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
                    onCheckoutClick = { navController.navigate(ROUTE_CHECKOUT) }
                )
            }
        }
    }
}

@Composable
fun CartItemsList(
    cartItems: List<CartItem>,
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
                onIncrease = {
                    cartViewModel.updateQuantity(
                        userId, item.productId, item.quantity + 1
                    )
                },
                onDecrease = {
                    if (item.quantity > 1) {
                        cartViewModel.updateQuantity(
                            userId, item.productId, item.quantity - 1
                        )
                    }
                },
                onRemove = {
                    cartViewModel.removeFromCart(userId, item.productId)
                }
            )
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 2,
                    color = MaroonDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ksh ${item.price}",
                    color = Maroon,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                QuantityControls(
                    quantity = item.quantity,
                    onDecrease = onDecrease,
                    onIncrease = onIncrease
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove from cart",
                    tint = MaterialTheme.colorScheme.error
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
        IconButton(
            onClick = onDecrease,
            modifier = Modifier.size(32.dp),
            enabled = quantity > 1
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Decrease",
                tint = if (quantity > 1) Maroon else MaroonDark.copy(alpha = 0.3f)
            )
        }

        Text(
            text = quantity.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center,
            color = MaroonDark
        )

        IconButton(
            onClick = onIncrease,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Increase",
                tint = Maroon
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
                color = MaroonDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            SummaryRow("Subtotal", "Ksh ${"%.2f".format(subtotal)}")
            SummaryRow("Tax (5%)", "Ksh ${"%.2f".format(tax)}")
            SummaryRow("Shipping", "Ksh ${"%.2f".format(shipping)}")

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = 1.dp,
                color = Maroon.copy(alpha = 0.2f)
            )

            SummaryRow(
                label = "Total",
                value = "Ksh ${"%.2f".format(total)}",
                bold = true,
                highlight = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCheckoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Proceed to Checkout",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "You'll complete payment in the next step",
                fontSize = 12.sp,
                color = MaroonDark.copy(alpha = 0.5f),
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
            color = if (highlight) Maroon else MaroonDark.copy(alpha = 0.8f),
            fontSize = if (highlight) 18.sp else 14.sp
        )
        Text(
            text = value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) Maroon else MaroonDark.copy(alpha = 0.8f),
            fontSize = if (highlight) 18.sp else 14.sp
        )
    }
}

// Note: removed the @Preview composable at the bottom of this file, since
// CartScreen now requires a cartViewModel parameter that Preview has no way
// to supply. If you want a preview back, you'd need a fake/mock CartViewModel.