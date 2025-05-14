//package com.example.sportsgear.ui.screens.products
//import android.net.Uri
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.example.sportsgear.R
//import com.example.sportsgear.models.ProductModel
//import com.example.sportsgearapp.data.ProductViewModel
//import com.google.firebase.database.*
//
//@Composable
//fun UpdateProductScreen(navController: NavController, productId: String) {
//    val imageUri = rememberSaveable { mutableStateOf<Uri?>(null) }
//    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        uri?.let { imageUri.value = it }
//    }
//    var name by remember { mutableStateOf("") }
//    var price by remember { mutableStateOf("") }
//    var category by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//
//    val productViewModel: ProductViewModel = viewModel()
//    val context = LocalContext.current
//
//    val currentDataRef = FirebaseDatabase.getInstance().getReference("Products/$productId")
//
//    // Fetch current product data
//    DisposableEffect(Unit) {
//        val listener = object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val product = snapshot.getValue(ProductModel::class.java)
//                product?.let {
//                    name = it.name
//                    price = it.price
//                    category = it.category
//                    description = it.description
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
//            }
//        }
//        currentDataRef.addValueEventListener(listener)
//        onDispose { currentDataRef.removeEventListener(listener) }
//    }
//
//    // UI
//    Column(
//        modifier = Modifier
//            .padding(10.dp)
//            .fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(Color.DarkGray)
//                .padding(16.dp)
//        ) {
//            Text(
//                text = "UPDATE PRODUCT",
//                fontWeight = FontWeight.Bold,
//                fontSize = 26.sp,
//                color = Color.White,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//
//        Card(shape = CircleShape, modifier = Modifier.padding(10.dp).size(200.dp)) {
//            AsyncImage(
//                model = imageUri.value ?: R.drawable.img,
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .size(200.dp)
//                    .clickable { launcher.launch("image/*") }
//            )
//        }
//
//        Text("Tap to Upload Product Image")
//
//        OutlinedTextField(
//            value = name,
//            onValueChange = { name = it },
//            label = { Text("Product Name") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        OutlinedTextField(
//            value = price,
//            onValueChange = { price = it },
//            label = { Text("Price") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        OutlinedTextField(
//            value = category,
//            onValueChange = { category = it },
//            label = { Text("Category") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        OutlinedTextField(
//            value = description,
//            onValueChange = { description = it },
//            label = { Text("Description") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(150.dp),
//            singleLine = false
//        )
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Button(onClick = { navController.popBackStack() }) {
//                Text("GO BACK")
//            }
//            Spacer(modifier = Modifier.width(20.dp))
//            Button(onClick = {
//                productViewModel.updateProduct(
//                    context = context,
//                    navController = navController,
//                    name = name,
//                    price = price,
//                    category = category,
//                    description = description,
//                    imageUri = imageUri.value,
//                    productId = productId
//                )
//            }) {
//                Text("UPDATE")
//            }
//        }
//    }
//}
