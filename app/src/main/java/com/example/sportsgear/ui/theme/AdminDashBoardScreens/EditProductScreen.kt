package com.example.sportsgear.ui.screens.admin
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark
import com.example.sportsgear.ui.theme.MaroonLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    navController: NavController,
    productId: String,
    authViewModel: AuthViewModel
) {
    val productViewModel: ProductViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAdmin by authViewModel.isAdmin.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val allProducts by productViewModel.productList.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()
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

    val product = allProducts.find { it.productId == productId }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Product",
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
    ) { paddingValues ->

        when (isAdmin) {
            null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Maroon)
                }
            }

            false -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Only admins can edit products",
                        color = Maroon,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            true -> {
                if (product == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Maroon)
                    }
                    return@Scaffold
                }

                var name by remember { mutableStateOf(product.name) }
                var description by remember { mutableStateOf(product.description) }
                var price by remember { mutableStateOf(product.price) }
                var category by remember { mutableStateOf(product.category) }
                var quantity by remember { mutableStateOf(product.quantity) }
                var imageUri by remember { mutableStateOf<Uri?>(null) }
                var isOnOffer by remember { mutableStateOf(product.isOnOffer) }

                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri -> imageUri = uri }

                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = imageUri ?: product.imageUrl,
                        contentDescription = "Product image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name", color = MaroonDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            unfocusedBorderColor = MaroonLight,
                            cursorColor = Maroon
                        )
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description", color = MaroonDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            unfocusedBorderColor = MaroonLight,
                            cursorColor = Maroon
                        )
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (Ksh)", color = MaroonDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            unfocusedBorderColor = MaroonLight,
                            cursorColor = Maroon
                        )
                    )

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity in Stock", color = MaroonDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            unfocusedBorderColor = MaroonLight,
                            cursorColor = Maroon
                        )
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category", color = MaroonDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            unfocusedBorderColor = MaroonLight,
                            cursorColor = Maroon
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isOnOffer,
                            onCheckedChange = { isOnOffer = it },
                            colors = CheckboxDefaults.colors(checkedColor = Maroon)
                        )
                        Text(
                            text = "Mark as Offer / Promotion",
                            color = MaroonDark
                        )
                    }

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (imageUri != null) "Change Image Again" else "Pick New Image",
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            when {
                                name.isBlank() -> scope.launch {
                                    snackbarHostState.showSnackbar("Product name cannot be empty")
                                }
                                price.isBlank() -> scope.launch {
                                    snackbarHostState.showSnackbar("Price cannot be empty")
                                }
                                price.toDoubleOrNull() == null -> scope.launch {
                                    snackbarHostState.showSnackbar("Price must be a valid number")
                                }
                                category.isBlank() -> scope.launch {
                                    snackbarHostState.showSnackbar("Category cannot be empty")
                                }
                                // ✅ FIX — same gap as UpdateProductScreen: quantity wasn't
                                // validated at all, not even for blankness.
                                quantity.isBlank() -> scope.launch {
                                    snackbarHostState.showSnackbar("Quantity cannot be empty")
                                }
                                quantity.toIntOrNull() == null -> scope.launch {
                                    snackbarHostState.showSnackbar("Quantity must be a whole number")
                                }
                                else -> {
                                    productViewModel.updateProduct(
                                        context = context,
                                        navController = navController,
                                        name = name.trim(),
                                        price = price.trim(),
                                        category = category.trim(),
                                        description = description.trim(),
                                        quantity = quantity.trim(),
                                        imageUri = imageUri,
                                        productId = product.productId,
                                        oldImageUrl = product.imageUrl,
                                        isOnOffer = isOnOffer,
                                        isAdmin = isAdmin == true
                                    )
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Update Product",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Note: removed the @Preview composable, since this screen requires an
// authViewModel parameter Preview can't supply without a fake/mock instance.