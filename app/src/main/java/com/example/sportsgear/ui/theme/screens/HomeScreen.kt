package com.example.sportsgear.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.models.CartItem
import com.example.sportsgear.models.Product
import com.example.sportsgear.navigation.*
import com.example.sportsgear.ui.theme.screens.FeaturedProductCard
import com.example.sportsgear.ui.theme.screens.ProductCard
import kotlinx.coroutines.delay

// 🖼️ Auto-sliding dynamic Banner Slider
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BannerSliderAuto(promoProducts: List<Product>) {
    val banners = if (promoProducts.isNotEmpty()) {
        promoProducts.mapNotNull { it.imageUrl }
    } else listOf(
        "https://images.unsplash.com/photo-1606813903067-1d2a40c9baf4",
        "https://images.unsplash.com/photo-1599058917766-6be8599b71a6",
        "https://images.unsplash.com/photo-1600180758890-6c4c3f0b9e25"
    )

    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
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
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                AsyncImage(
                    model = banners[page],
                    contentDescription = "Promotional Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(8.dp)
                .zIndex(2f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(banners.size) { index ->
                val alpha = if (pagerState.currentPage == index) 1f else 0.4f
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (pagerState.currentPage == index)
                                MaterialTheme.colorScheme.primary
                            else Color.LightGray,
                            shape = RoundedCornerShape(50)
                        )
                        .alpha(alpha)
                )
            }
        }
    }
}

// 🏷️ Category Banner Section
@Composable
fun CategoryBannerSection(onCategorySelected: (String) -> Unit) {
    val categoryBanners = listOf(
        "Shoes" to "https://images.unsplash.com/photo-1513105737059-ff0cf0580b16",
        "Jerseys" to "https://images.unsplash.com/photo-1599058917212-d750089bc07e",
        "Equipment" to "https://images.unsplash.com/photo-1509021436665-8f07dbf5bf1d",
        "Accessories" to "https://images.unsplash.com/photo-1581539250439-c52e3e1c4b3a"
    )

    Column(Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "Shop by Category",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categoryBanners) { (name, imageUrl) ->
                Card(
                    modifier = Modifier
                        .width(180.dp)
                        .height(120.dp)
                        .clickable { onCategorySelected(name) },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Box {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "$name banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 🏠 Home Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val allProducts by productViewModel.productList.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val fullName by authViewModel.fullName.collectAsState()
    val cartItems: List<CartItem> by cartViewModel.cartItems
    var searchQuery by remember { mutableStateOf("") }

    var resolvedAdminState by remember { mutableStateOf<Boolean?>(null) }

    // Sync products & admin state
    LaunchedEffect(currentUser) {
        productViewModel.fetchProducts()
        currentUser?.uid?.let { uid ->
            authViewModel.fetchUserFullName(uid)
            authViewModel.checkAdminStatus(uid)
        } ?: run { resolvedAdminState = false }
    }

    LaunchedEffect(isAdmin) { resolvedAdminState = isAdmin }

    // Show loading while admin state is unresolved
    if (currentUser != null && resolvedAdminState == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Checking admin privileges...")
            }
        }
        return
    }

    val filteredProducts = remember(searchQuery, allProducts) {
        if (searchQuery.isBlank()) allProducts
        else allProducts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val promoProducts = allProducts.filter { it.isOnOffer == true }
    val featuredProducts = allProducts.take(5)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SportsGear") },
                actions = {
                    if (resolvedAdminState == true) {
                        IconButton(onClick = { navController.navigate(ROUTE_ADMIN_DASHBOARD) }) {
                            Icon(Icons.Default.Dashboard, contentDescription = "Admin Dashboard")
                        }
                    }

                    IconButton(onClick = { navController.navigate(ROUTE_CART) }) {
                        BadgedBox(badge = {
                            if (cartItems.isNotEmpty()) Badge { Text(cartItems.size.toString()) }
                        }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }

                    // Debug: optional
                    IconButton(onClick = {
                        Toast.makeText(context, "Admin: $resolvedAdminState", Toast.LENGTH_SHORT).show()
                        currentUser?.uid?.let { authViewModel.checkAdminStatus(it) }
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "Debug Info")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = {},
                    icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = false,
                    onClick = { navController.navigate(ROUTE_ORDER) },
                    icon = { Icon(Icons.Default.List, null) }, label = { Text("Orders") })
                NavigationBarItem(selected = false,
                    onClick = { navController.navigate(ROUTE_PROFILE) },
                    icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (!fullName.isNullOrBlank()) "Welcome back, $fullName 👋" else "Welcome 👋",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(16.dp)
                )
            }

            item { BannerSliderAuto(promoProducts) }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search products...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            item { CategoryBannerSection { category -> navController.navigate(getCategoryRoute(category)) } }

            if (featuredProducts.isNotEmpty()) {
                item {
                    Text(
                        text = "⭐ Featured Products",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(featuredProducts) { product ->
                            FeaturedProductCard(
                                product = product,
                                onClick = { navController.navigate("$ROUTE_PRODUCT_DETAIL/${product.productId}") },
                                onAddToCart = {
                                    val userId = currentUser?.uid
                                    if (userId != null) cartViewModel.addToCart(userId, product)
                                    else Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                item {
                    Text(
                        text = "No products found for \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        navController = navController,
                        isAdmin = resolvedAdminState == true,
                        onClick = { navController.navigate("$ROUTE_PRODUCT_DETAIL/${product.productId}") },
                        onAddToCart = {
                            val userId = currentUser?.uid
                            if (userId != null) cartViewModel.addToCart(userId, product)
                            else Toast.makeText(context, "Please log in", Toast.LENGTH_SHORT).show()
                        },
                        viewModel = productViewModel
                    )
                }
            }
        }
    }
}
