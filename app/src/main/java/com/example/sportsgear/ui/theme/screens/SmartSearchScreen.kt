package com.example.sportsgear.ui.theme.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.data.SmartSearchViewModel
import com.example.sportsgear.navigation.getProductDetailRoute
import com.example.sportsgear.ui.theme.MaroonDark
import kotlinx.coroutines.launch

@Composable
fun SmartSearchScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    searchViewModel: SmartSearchViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel()
) {
    var query by remember { mutableStateOf("") }
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val quickCategories = listOf("sports wear", "Jerseys", "Equipment", "Accessories")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Try: running shoes, cheap jerseys...") }
                )
                IconButton(onClick = { searchViewModel.search(query) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Or browse by category",
                style = MaterialTheme.typography.labelMedium,
                color = MaroonDark.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickCategories) { category ->
                    AssistChip(
                        onClick = {
                            query = category
                            searchViewModel.search(category)
                        },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (searchViewModel.isLoading.value) {
                CircularProgressIndicator(Modifier.padding(16.dp))
            }

            searchViewModel.errorMessage.value?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            searchViewModel.matchNote.value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaroonDark.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (!searchViewModel.isLoading.value &&
                searchViewModel.results.isEmpty() &&
                searchViewModel.matchNote.value != null
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Try one of the categories above, or search using simpler terms",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaroonDark.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            LazyColumn {
                items(searchViewModel.results) { product ->
                    ProductCard(
                        product = product,
                        isAdmin = isAdmin == true,
                        onClick = {
                            navController.navigate(getProductDetailRoute(product.productId))
                        },
                        onAddToCart = {
                            val userId = currentUser?.uid
                            if (userId != null) {
                                cartViewModel.addToCart(userId, product)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please log in to add to cart")
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