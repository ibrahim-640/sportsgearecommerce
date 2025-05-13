import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

import com.example.sportsgear.ui.theme.ProductItem


@Composable
fun ProductListScreen(
    navController: NavController
) {
//    val productViewModel=productViewModel.ViewModel()
//    // Observe the products state
//    val products by productViewModel.products.observeAsState(emptyList())
//
//    if (products.isEmpty()) {
//        // Show a loading message if products are not loaded yet
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("Loading products...")
//        }
//    } else {
//        // Display the product list if products are available
//        LazyColumn {
//            items(products) { product ->
//                ProductItem(product = product)
//            }
//        }
//    }
}