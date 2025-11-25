package com.example.sportsgear.ui.screens.screens

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

val CustomMaroon = Color(0xFF800000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val isAdmin by authViewModel.isAdmin.collectAsState()

    // 🚫 If NOT admin, restrict access
    if (isAdmin != true) {
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
                Text("Only Admins Can Add Products", color = CustomMaroon)
            }
        }
        return
    }

    // 🔤 Form Fields
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var price by remember { mutableStateOf(TextFieldValue("")) }
    var category by remember { mutableStateOf(TextFieldValue("")) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    var isOnOffer by remember { mutableStateOf(false) } // ✅ Offer toggle

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri.value = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Product", color = Color.White) },
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
            // 🧾 Product Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name", color = CustomMaroon) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CustomMaroon,
                    cursorColor = CustomMaroon
                )
            )

            // 📝 Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", color = CustomMaroon) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CustomMaroon,
                    cursorColor = CustomMaroon
                )
            )

            // 💰 Price
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price", color = CustomMaroon) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CustomMaroon,
                    cursorColor = CustomMaroon
                )
            )

            // 🏷 Category
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category", color = CustomMaroon) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CustomMaroon,
                    cursorColor = CustomMaroon
                )
            )

            // 🧩 Offer Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = isOnOffer,
                    onCheckedChange = { isOnOffer = it },
                    colors = CheckboxDefaults.colors(checkedColor = CustomMaroon)
                )
                Text("Mark as Offer Product", color = CustomMaroon)
            }

            // 🖼 Image Picker
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = CustomMaroon)
            ) {
                Text("Pick Image", color = Color.White)
            }

            // 🚀 Upload Button
            Button(
                onClick = {
                    imageUri.value?.let { uri ->
                        val priceValue = price.text.toDoubleOrNull()
                        if (priceValue != null) {
                            // ✅ Updated: Include offer flag
                            productViewModel.uploadProductWithImage(
                                uri = uri,
                                context = context,
                                name = name.text,
                                category = category.text,
                                price = priceValue.toString(),
                                description = description.text,
                                navController = navController,
                                isOnOffer = isOnOffer // pass offer status
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "Please enter a valid price",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } ?: Toast.makeText(context, "Please pick an image", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CustomMaroon)
            ) {
                Text("Upload Product", color = Color.White)
            }
        }
    }
}
