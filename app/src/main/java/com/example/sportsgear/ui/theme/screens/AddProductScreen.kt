package com.example.sportsgear.ui.screens.screens
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sportsgear.data.ProductViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController, productViewModel: ProductViewModel) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var price by remember { mutableStateOf(TextFieldValue("")) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri.value = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add New Product") })
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
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth()
            )
            var category by remember { mutableStateOf(TextFieldValue("")) }

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )


            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Pick Image")
            }

            Row {
                Button(onClick = {
                    navController.popBackStack()
                }) {
                    Text(text = "GO BACK")
                }

                Spacer(modifier = Modifier.width(20.dp))

                Button(onClick = {
                    imageUri.value?.let { uri ->
                        val priceValue = price.text.toDoubleOrNull()
                        if (priceValue != null) {
                            productViewModel.uploadProductWithImage(
                                uri = uri,
                                context = context,
                                name = name.text,
                                price = priceValue,
                                description = description.text,
                                category = category.text,
                                navController = navController
                            )
                        } else {
                            Toast.makeText(context, "Please enter a valid price", Toast.LENGTH_LONG).show()
                        }
                    } ?: Toast.makeText(context, "Please pick an image", Toast.LENGTH_LONG).show()
                }) {
                    Text("Upload Product")
                }


                Button(
                    onClick = {
                        Toast.makeText(
                            context,
                            "Product '${name.text}' added successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit")
                }
            }
        }
    }
}

