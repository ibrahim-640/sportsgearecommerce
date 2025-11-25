import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.models.Product
import com.example.sportsgear.R
import com.example.sportsgear.navigation.ROUTE_ADMIN_DASHBOARD
import com.example.sportsgear.navigation.ROUTE_PRODUCT_DETAIL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val allProducts by viewModel.productList.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortAscending by remember { mutableStateOf(true) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val categories = listOf("All") + allProducts
        .mapNotNull { it.category?.takeIf { cat -> cat.isNotBlank() } }
        .distinct()

    val filteredProducts = allProducts
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .filter { selectedCategory == "All" || it.category == selectedCategory }
        .sortedBy {
            val price = it.price.toDoubleOrNull() ?: 0.0
            if (sortAscending) price else -price
        }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isAdmin == true) {

            Button(
                onClick = { navController.navigate(ROUTE_ADMIN_DASHBOARD) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Admin Panel")
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Products") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box {
                Button(onClick = { showCategoryDropdown = true }) {
                    Text("Category: $selectedCategory")
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

            Button(onClick = { sortAscending = !sortAscending }) {
                Text(if (sortAscending) "Sort: Low to High" else "Sort: High to Low")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            allProducts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            filteredProducts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No products found matching your criteria")
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredProducts) { product ->
                        if (product is Product) {
                            ProductItem(
                                product = product,
                                onItemClick = { navController.navigate("$ROUTE_PRODUCT_DETAIL/${product.productId}") },
                                onAddToCart = {
                                    viewModel.addToCart(product, context)
                                    Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Text("Invalid product type") // Debug info
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onItemClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.placeholder_profile),
                error = painterResource(id = R.drawable.placeholder_profile)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Ksh ${product.price}",
                    color = Color(0xFF388E3C)
                )
                product.category?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            IconButton(onClick = onAddToCart) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Add to Cart"
                )
            }
        }
    }
}
