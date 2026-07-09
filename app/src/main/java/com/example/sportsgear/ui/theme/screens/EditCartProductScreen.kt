package com.example.sportsgear.ui.theme.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.models.CartItem
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark
import com.example.sportsgear.ui.theme.MaroonLight
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCartProductScreen(
    navController: NavController,
    productId: String,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel // ✅ FIX — now passed in from AppNavHost instead of
    // creating its own instance. Previously this screen's own CartViewModel never had
    // loadCartItems called on it, so cartItems was permanently empty, cartItem was
    // permanently null, and the screen was stuck on the loading spinner forever.
    // Using the shared, already-loaded instance fixes that.
) {
    val scope = rememberCoroutineScope()

    val currentUser by authViewModel.currentUser.collectAsState()
    val userId = currentUser?.uid ?: ""

    val cartItems by cartViewModel.cartItems.collectAsState()
    val isLoading by cartViewModel.isLoading.collectAsState()
    val message by cartViewModel.message.collectAsState()

    val cartItem = cartItems.find { it.productId == productId }

    val snackbarHostState = remember { SnackbarHostState() }

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
                    Text(
                        "Edit Cart Item",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Maroon)
            )
        }
    ) { padding ->

        if (cartItem == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Maroon)
            }
            return@Scaffold
        }

        var quantity by remember { mutableStateOf(cartItem.quantity.toString()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = cartItem.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaroonDark
            )
            Text(
                text = "Ksh ${cartItem.price}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaroonDark.copy(alpha = 0.7f)
            )

            HorizontalDivider(
                color = Maroon.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity", color = MaroonDark) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Maroon,
                    unfocusedBorderColor = MaroonLight,
                    cursorColor = Maroon
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val qty = quantity.toIntOrNull()
                    when {
                        qty == null -> scope.launch {
                            snackbarHostState.showSnackbar("Quantity must be a number")
                        }
                        qty < 1 -> scope.launch {
                            snackbarHostState.showSnackbar("Quantity must be at least 1")
                        }
                        else -> {
                            val updatedCartItem = CartItem(
                                productId = cartItem.productId,
                                name = cartItem.name,
                                imageUrl = cartItem.imageUrl,
                                price = cartItem.price,
                                quantity = qty,
                                category = cartItem.category
                            )
                            cartViewModel.updateCartProduct(
                                userId = userId,
                                productId = cartItem.productId,
                                updatedProduct = updatedCartItem,
                                onSuccess = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Maroon)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Update Cart",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Note: removed the @Preview composable, since this screen now requires a
// cartViewModel parameter Preview can't supply without a fake/mock instance.