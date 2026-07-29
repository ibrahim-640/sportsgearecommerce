package com.example.sportsgear.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.example.sportsgear.ai.analyzeProductImage
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.ui.theme.Maroon
import com.example.sportsgear.ui.theme.MaroonDark
import com.example.sportsgear.ui.theme.MaroonLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val productViewModel: ProductViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isAdmin by authViewModel.isAdmin.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage by productViewModel.errorMessage.collectAsState()
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            productViewModel.clearErrorMessage()
        }
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var onOffer by remember { mutableStateOf(false) }

    var isAnalyzing by remember { mutableStateOf(false) }
    var aiReasoning by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        aiReasoning = null // clear old reasoning when a new image is picked
    }

    fun analyzeImage() {
        val uri = imageUri ?: return
        scope.launch {
            isAnalyzing = true
            try {
                val bitmap = uriToBitmap(context, uri)
                val suggestion = analyzeProductImage(bitmap)

                name = suggestion.name
                description = suggestion.description
                category = suggestion.category
                price = suggestion.suggestedPrice
                aiReasoning = suggestion.reasoning

            } catch (e: Exception) {
                val friendly = if (e.message?.contains("quota", ignoreCase = true) == true ||
                    e.message?.contains("high demand", ignoreCase = true) == true
                ) {
                    "AI is busy right now — please try again in a moment"
                } else {
                    "Couldn't analyze the image. Please fill in the details manually."
                }
                snackbarHostState.showSnackbar(friendly)
            } finally {
                isAnalyzing = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Add New Product", color = Color.White, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Maroon)
            )
        }
    ) { paddingValues ->

        when (isAdmin) {
            null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Maroon) }
            }

            false -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Only admins can add products", color = Maroon, fontWeight = FontWeight.SemiBold)
                }
            }

            true -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (imageUri != null) "Change Image" else "Pick Image", color = Color.White)
                    }

                    imageUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected product image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick = { analyzeImage() },
                            enabled = !isAnalyzing,
                            colors = ButtonDefaults.buttonColors(containerColor = MaroonDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Analyzing...", color = Color.White)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Fill details with AI", color = Color.White)
                            }
                        }

                        aiReasoning?.let {
                            Text(
                                text = "AI suggestion: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaroonDark.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name", color = MaroonDark) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Maroon,
                            unfocusedBorderColor = MaroonLight,
                            cursorColor = Maroon
                        )
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = onOffer,
                            onCheckedChange = { onOffer = it },
                            colors = CheckboxDefaults.colors(checkedColor = Maroon)
                        )
                        Text("Mark as Offer Product", color = MaroonDark)
                    }

                    Button(
                        onClick = {
                            when {
                                name.isBlank() -> scope.launch { snackbarHostState.showSnackbar("Please enter a product name") }
                                price.isBlank() -> scope.launch { snackbarHostState.showSnackbar("Please enter a price") }
                                price.toDoubleOrNull() == null -> scope.launch { snackbarHostState.showSnackbar("Price must be a valid number") }
                                category.isBlank() -> scope.launch { snackbarHostState.showSnackbar("Please enter a category") }
                                quantity.isBlank() -> scope.launch { snackbarHostState.showSnackbar("Please enter quantity in stock") }
                                quantity.toIntOrNull() == null -> scope.launch { snackbarHostState.showSnackbar("Quantity must be a whole number") }
                                imageUri == null -> scope.launch { snackbarHostState.showSnackbar("Please pick an image") }
                                else -> {
                                    productViewModel.uploadProductWithImage(
                                        uri = imageUri!!,
                                        context = context,
                                        name = name.trim(),
                                        category = category.trim(),
                                        price = price.trim(),
                                        description = description.trim(),
                                        quantity = quantity.trim(),
                                        navController = navController,
                                        onOffer = onOffer,
                                        isAdmin = isAdmin == true
                                    )
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Maroon),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Upload Product", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun uriToBitmap(context: android.content.Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
}