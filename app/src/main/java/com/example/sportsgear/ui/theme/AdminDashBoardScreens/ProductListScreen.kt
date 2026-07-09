package com.example.sportsgear.ui.theme.screens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportsgear.R
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.models.Product
import com.example.sportsgear.navigation.ROUTE_ADMIN_DASHBOARD
import com.example.sportsgear.navigation.getProductDetailRoute
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark
import com.example.sportsgear.ui.theme.MaroonLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel // ✅ FIX — added so this screen can add to cart
    // through the real CartViewModel.addToCart (correct increment logic) instead
    // of the removed ProductViewModel.addToCart shortcut. Pass in the shared
    // instance from AppNavHost wherever this screen is navigated to.
) {
    val productViewModel: ProductViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val allProducts by productViewModel.productList.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState() // ✅ FIX — needed to call addToCart with a real userId

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortAscending by remember { mutableStateOf(true) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
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

    val categories = listOf("All") + allProducts
        .mapNotNull { it.category?.takeIf { cat -> cat.isNotBlank() } }
        .distinct()

    val filteredProducts = remember(searchQuery, selectedCategory, sortAscending, allProducts) {
        val filtered = allProducts
            .filter { it.name.contains(searchQuery, ignoreCase = true) }
            .filter { selectedCategory == "All" || it.category == selectedCategory }

        if (sortAscending) {
            filtered.sortedBy { it.price.toDoubleOrNull() ?: 0.0 }
        } else {
            filtered.sortedByDescending { it.price.toDoubleOrNull() ?: 0.0 }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Products",
                        color = MaroonDark,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (isAdmin == true) {
                        IconButton(
                            onClick = { navController.navigate(ROUTE_ADMIN_DASHBOARD) }
                        ) {
                            Icon(
                                Icons.Default.Dashboard,
                                contentDescription = "Admin Panel",
                                tint = MaroonDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Maroon.copy(alpha = 0.05f)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Products", color = MaroonDark) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Maroon,
                    unfocusedBorderColor = MaroonLight,
                    cursorColor = Maroon
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    Button(
                        onClick = { showCategoryDropdown = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon)
                    ) {
                        Text("Category: $selectedCategory", color = Color.White)
                    }
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = { sortAscending = !sortAscending },
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon)
                ) {
                    Text(
                        if (sortAscending) "Low to High" else "High to Low",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Maroon)
                    }
                }

                allProducts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No products available yet.",
                            color = MaroonDark.copy(alpha = 0.6f)
                        )
                    }
                }

                filteredProducts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No products found for \"$searchQuery\"",
                            color = MaroonDark.copy(alpha = 0.6f)
                        )
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredProducts) { product ->
                            ProductItem(
                                product = product,
                                onItemClick = {
                                    // ✅ FIX — was "$ROUTE_PRODUCT_DETAIL/${product.productId}",
                                    // same duplicated-placeholder bug fixed everywhere else.
                                    navController.navigate(getProductDetailRoute(product.productId))
                                },
                                onAddToCart = {
                                    // ✅ FIX — was productViewModel.addToCart(product, context),
                                    // which has been removed (it bypassed CartViewModel,
                                    // never incremented quantity, and was unused dead code
                                    // everywhere else). Now uses the real CartViewModel path,
                                    // with the same login-guard pattern every other screen uses.
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
fun ProductItem(
    product: Product,
    onItemClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val quantityInStock = product.quantity.toIntOrNull() ?: 0
    val isInStock = quantityInStock > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.placeholder_profile),
                error = painterResource(id = R.drawable.placeholder_profile)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaroonDark
                )
                Text(
                    text = "Ksh ${product.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaroonDark.copy(alpha = 0.8f)
                )
                product.category?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaroonDark.copy(alpha = 0.5f)
                    )
                }

                Text(
                    text = if (isInStock) "In stock: $quantityInStock" else "Out of stock",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isInStock)
                        MaroonDark.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.error
                )

                if (product.isOnOffer) {
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
            }

            IconButton(
                onClick = onAddToCart,
                enabled = isInStock
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Add to Cart",
                    tint = if (isInStock) Maroon else MaroonDark.copy(alpha = 0.3f)
                )
            }
        }
    }
}