package com.example.sportsgear.ui.theme.AdminDashBoardScreens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.models.Product

val CustomMaroon = Color(0xFF800000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    navController: NavController,
    productId: String,
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val isAdmin by authViewModel.isAdmin.collectAsState()
    val allProducts by productViewModel.productList.collectAsState()

    // ✅ Find the product
    val product = allProducts.find { it.productId == productId }

    // Restrict non-admins
    if (isAdmin == false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Access Denied", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CustomMaroon)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Only Admins Can Edit Products", color = CustomMaroon)
            }
        }
        return
    }

    // ✅ If product not loaded yet
    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        LaunchedEffect(Unit) { productViewModel.fetchProducts() }
        return
    }

    // ✅ UI States
    var name by remember { mutableStateOf(TextFieldValue(product.name)) }
    var description by remember { mutableStateOf(TextFieldValue(product.description)) }
    var price by remember { mutableStateOf(TextFieldValue(product.price)) }
    var category by remember { mutableStateOf(TextFieldValue(product.category)) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    // ✅ New: Offer state
    var isOnOffer by remember { mutableStateOf(product.isOnOffer) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri.value = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Product", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CustomMaroon)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name", color = CustomMaroon) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", color = CustomMaroon) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price", color = CustomMaroon) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category", color = CustomMaroon) },
                modifier = Modifier.fillMaxWidth()
            )

            // ✅ Checkbox for Offer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Checkbox(
                    checked = isOnOffer,
                    onCheckedChange = { isOnOffer = it }
                )
                Text(
                    text = "Mark as Offer / Promotion",
                    color = CustomMaroon
                )
            }

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = CustomMaroon)
            ) {
                Text("Pick New Image (Optional)", color = Color.White)
            }

            // ✅ Update Button
            Button(
                onClick = {
                    val priceVal = price.text.toDoubleOrNull()
                    if (priceVal != null) {
                        val finalImageUri = imageUri.value

                        productViewModel.updateProduct(
                            context = context,
                            navController = navController,
                            name = name.text.trim(),
                            price = price.text.trim(),
                            category = category.text.trim(),
                            description = description.text.trim(),
                            imageUri = finalImageUri,
                            productId = product.productId,
                            oldImageUrl = product.imageUrl,
                            isOnOffer = isOnOffer // ✅ Added parameter
                        )

                        productViewModel.fetchProducts()

                        Toast.makeText(context, "Updating product...", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Enter a valid price", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CustomMaroon)
            ) {
                Text("Update Product", color = Color.White)
            }
        }
    }
}
