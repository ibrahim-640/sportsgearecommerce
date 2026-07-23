package com.example.sportsgear.ui.screens
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.sportsgear.R
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark
import com.example.sportsgear.ui.theme.MaroonLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(
    navController: NavController,
    productId: String,
    authViewModel: AuthViewModel // ✅ FIX — shared instance from AppNavHost,
    // same as every other screen, instead of creating its own
) {
    val productViewModel: ProductViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isAdmin by authViewModel.isAdmin.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val errorMessage by productViewModel.errorMessage.collectAsState()
    val successMessage by productViewModel.successMessage.collectAsState()
    val allProducts by productViewModel.productList.collectAsState()

    val product = allProducts.find { it.productId == productId }

    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var onOffer by remember { mutableStateOf(false) }
    var existingImageUrl by remember { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts()
    }

    LaunchedEffect(product) {
        product?.let {
            name = it.name
            price = it.price
            category = it.category
            description = it.description
            quantity = it.quantity
            existingImageUrl = it.imageUrl
            onOffer = it.onOffer
        }
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Update Product",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Maroon
                )
            )
        }
    ) { padding ->

        when (isAdmin) {
            null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Maroon)
                }
            }

            false -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Only admins can update products",
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
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Maroon)
                    }
                    return@Scaffold
                }

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Card(
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(180.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        AsyncImage(
                            model = imageUri ?: existingImageUrl.ifEmpty { R.drawable.img },
                            contentDescription = "Product image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                                .clickable { launcher.launch("image/*") }
                        )
                    }

                    Text(
                        text = if (imageUri != null) "Image selected — tap to change"
                        else "Tap image to change",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaroonDark.copy(alpha = 0.6f)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name", color = MaroonDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            unfocusedBorderColor = MaroonLight,
                            cursorColor = Maroon
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (Ksh)", color = MaroonDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            unfocusedBorderColor = MaroonLight,
                            cursorColor = Maroon
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category", color = MaroonDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            unfocusedBorderColor = MaroonLight,
                            cursorColor = Maroon
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity in Stock", color = MaroonDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            unfocusedBorderColor = MaroonLight,
                            cursorColor = Maroon
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description", color = MaroonDark) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            unfocusedBorderColor = MaroonLight,
                            cursorColor = Maroon
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = onOffer,
                            onCheckedChange = { onOffer = it },
                            colors = CheckboxDefaults.colors(checkedColor = Maroon)
                        )
                        Text(
                            text = "Mark as Offer / Promotion",
                            color = MaroonDark
                        )
                    }

                    Button(
                        onClick = { launcher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (imageUri != null) "Change Image Again" else "Pick New Image",
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            border = BorderStroke(1.dp, Maroon),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text("Go Back", color = Maroon, fontWeight = FontWeight.Bold)
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
                                    // ✅ FIX — quantity had no validation at all before,
                                    // not even a blank check. Same risk as AddProductScreen:
                                    // clearing/corrupting the field would silently save a
                                    // value that quantity.toIntOrNull() ?: 0 downstream
                                    // treats as 0/out-of-stock, with no warning given here.
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
                                            productId = productId,
                                            oldImageUrl = existingImageUrl,
                                            onOffer = onOffer,
                                            isAdmin = isAdmin == true
                                        )
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Update",
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
}

// Note: removed the @Preview composable, since this screen now requires an
// authViewModel parameter Preview can't supply without a fake/mock instance.