package com.example.sportsgear.ui.screens
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportsgear.R
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.models.Product
import com.example.sportsgear.navigation.*
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark
import com.example.sportsgear.ui.theme.screens.FeaturedProductCard
import com.example.sportsgear.ui.theme.screens.ProductCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel
) {
    val productViewModel: ProductViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val allProducts by productViewModel.productList.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val fullName by authViewModel.fullName.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()

    val errorMessage by productViewModel.errorMessage.collectAsState()
    val successMessage by productViewModel.successMessage.collectAsState()
    val cartMessage by cartViewModel.message.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            productViewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            productViewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(cartMessage) {
        cartMessage?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.clearMessage()
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            hasSearched = false
        }
    }

    if (currentUser != null && isAdmin == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Maroon.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Maroon)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading...", color = MaroonDark)
            }
        }
        return
    }

    val filteredProducts = remember(searchQuery, allProducts) {
        if (searchQuery.isBlank()) allProducts
        else allProducts.filter { product ->
            product.name.contains(searchQuery, ignoreCase = true) ||
                    product.category.contains(searchQuery, ignoreCase = true)
        }
    }

    // ✅ Fix 1 — Changed isOnOffer to onOffer to match Firebase field name
    val promoProducts = remember(allProducts) {
        allProducts.filter { it.onOffer }
    }

    val newArrivals = remember(allProducts) {
        allProducts.take(5)
    }

    // ✅ Fix 2 — Build category images from real Firebase products
    val categoryImages = remember(allProducts) {
        mapOf(
            "sports wear" to (allProducts.firstOrNull {
                it.category.equals("sports wear", ignoreCase = true)
            }?.imageUrl ?: ""),

            "Jerseys" to (allProducts.firstOrNull {
                it.category.equals("Jerseys", ignoreCase = true)
            }?.imageUrl ?: ""),

            "Equipment" to (allProducts.firstOrNull {
                it.category.equals("Equipment", ignoreCase = true)
            }?.imageUrl ?: ""),

            "Accessories" to (allProducts.firstOrNull {
                it.category.equals("Accessories", ignoreCase = true)
            }?.imageUrl ?: "")
        )
    }

    LaunchedEffect(filteredProducts, searchQuery) {
        if (searchQuery.isNotBlank()) {
            Log.d("SearchDebug", "Query: '$searchQuery' | Results: ${filteredProducts.size}")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("SportsGear", color = MaroonDark, fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ROUTE_ASSISTANT) }) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "Shopping Assistant",
                            tint = MaroonDark
                        )
                    }
                    IconButton(onClick = {
                        Log.d("NavDebug", "Smart search icon clicked")
                        navController.navigate(ROUTE_SMART_SEARCH)
                    }) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Smart Search",
                            tint = MaroonDark
                        )
                    }
                    if (isAdmin == true) {
                        IconButton(onClick = {
                            navController.navigate(ROUTE_ADMIN_DASHBOARD)
                        }) {
                            Icon(
                                Icons.Default.Dashboard,
                                contentDescription = "Admin Dashboard",
                                tint = MaroonDark
                            )
                        }
                    }
                    IconButton(onClick = { navController.navigate(ROUTE_CART) }) {
                        BadgedBox(badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge { Text(cartItems.size.toString()) }
                            }
                        }) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = MaroonDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Maroon.copy(alpha = 0.05f)
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate(ROUTE_ORDER) {
                            popUpTo(ROUTE_HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("Orders") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate(ROUTE_PROFILE) {
                            popUpTo(ROUTE_HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Maroon.copy(alpha = 0.05f))
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Greeting
            item {
                Text(
                    text = if (!fullName.isNullOrBlank())
                        "Welcome back, $fullName!"
                    else "Welcome!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaroonDark
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Banner slider — only shows if there are promo products
            if (promoProducts.isNotEmpty()) {
                item { BannerSliderAuto(promoProducts) }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search products...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = MaroonDark)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                hasSearched = false
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            hasSearched = true
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (searchQuery.isBlank())
                                        "Please enter a search term"
                                    else
                                        "Found ${filteredProducts.size} result(s) for '$searchQuery'"
                                )
                            }
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Maroon,
                        unfocusedBorderColor = Maroon.copy(alpha = 0.4f),
                        cursorColor = Maroon
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // ✅ Fix 3 — CategoryBannerSection now receives dynamic categoryImages
            item {
                CategoryBannerSection(
                    onCategorySelected = { category ->
                        navController.navigate(getCategoryRoute(category))
                    },
                    categoryImages = categoryImages // ✅ real product images from Firebase
                )
            }

            // New arrivals — hidden while searching
            if (newArrivals.isNotEmpty() && searchQuery.isBlank() && !hasSearched) {
                item {
                    Text(
                        text = "New Arrivals",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaroonDark
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                item {
                    LazyRow(
                        modifier = Modifier.height(260.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(newArrivals) { product ->
                            FeaturedProductCard(
                                product = product,
                                onClick = {
                                    navController.navigate(
                                        getProductDetailRoute(product.productId)
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
                                }
                            )
                        }
                    }
                }
            }

            // Search results or full product list
            if (hasSearched && searchQuery.isNotBlank()) {
                if (filteredProducts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaroonDark.copy(alpha = 0.3f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No products found for \"$searchQuery\"",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaroonDark.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = {
                                    navController.navigate(ROUTE_SMART_SEARCH)
                                    // optionally could pass searchQuery forward if you want to auto-fill it there
                                }) {
                                    Text("Try Smart Search instead", color = Maroon)
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "Search Results (${filteredProducts.size})",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaroonDark
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    items(filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            isAdmin = isAdmin == true,
                            onClick = {
                                navController.navigate(getProductDetailRoute(product.productId))
                            },
                            onEditClick = {
                                navController.navigate(
                                    "$ROUTE_UPDATE_PRODUCT/${product.productId}"
                                )
                            },
                            onDeleteClick = {
                                productViewModel.deleteProduct(
                                    productId = product.productId,
                                    isAdmin = isAdmin == true,
                                    navController = navController,
                                    onSuccess = {}
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
            } else if (!hasSearched) {
                items(allProducts) { product ->
                    ProductCard(
                        product = product,
                        isAdmin = isAdmin == true,
                        onClick = {
                            navController.navigate(getProductDetailRoute(product.productId))
                        },
                        onEditClick = {
                            navController.navigate(
                                "$ROUTE_UPDATE_PRODUCT/${product.productId}"
                            )
                        },
                        onDeleteClick = {
                            productViewModel.deleteProduct(
                                productId = product.productId,
                                isAdmin = isAdmin == true,
                                navController = navController,
                                onSuccess = {}
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

// ✅ Fix 4 — BannerSliderAuto unchanged — already correct
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerSliderAuto(promoProducts: List<Product>) {
    // ✅ No fallback needed — HomeScreen only calls this when list is not empty
    // ✅ No hardcoded Unsplash URLs — uses real product images from Firebase
    val banners = promoProducts.mapNotNull { it.imageUrl }

    // Guard — if somehow all products have null imageUrl
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(Unit) {
        if (banners.size <= 1) return@LaunchedEffect
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 8.dp)
        ) { page ->
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                AsyncImage(
                    model = banners[page],
                    contentDescription = "Promotional Banner",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.img), // ✅ while loading
                    error = painterResource(R.drawable.img),        // ✅ if load fails
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Page indicator dots
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(banners.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 10.dp else 7.dp)
                        .background(
                            color = if (pagerState.currentPage == index)
                                Maroon else Color.LightGray,
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        }
    }
}

// ✅ Fix 5 — CategoryBannerSection now accepts dynamic images from Firebase
@Composable
fun CategoryBannerSection(
    onCategorySelected: (String) -> Unit,
    categoryImages: Map<String, String> // ✅ new parameter to receive images from HomeScreen
) {
    val categoryBanners = listOf(
        "sports wear" to (categoryImages["sports wear"] ?: ""),
        "Jerseys" to (categoryImages["Jerseys"] ?: ""),
        "Equipment" to (categoryImages["Equipment"] ?: ""),
        "Accessories" to (categoryImages["Accessories"] ?: "")
    )

    Column(Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Shop by Category",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaroonDark
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categoryBanners) { (name, imageUrl) ->
                // ✅ Fix 6 — Card(onClick) instead of .clickable
                Card(
                    onClick = { onCategorySelected(name) },
                    modifier = Modifier
                        .width(180.dp)
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box {
                        // ✅ Fix 7 — placeholder and error added
                        // ✅ Fix 8 — ifBlank { null } prevents loading empty URL
                        AsyncImage(
                            model = imageUrl.ifBlank { null },
                            contentDescription = "$name category",
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.img),
                            error = painterResource(R.drawable.img),
                            modifier = Modifier.fillMaxSize()
                        )
                        // ✅ Fix 9 — fillMaxWidth on label bar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(MaroonDark.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}