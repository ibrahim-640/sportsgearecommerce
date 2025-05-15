package com.example.sportsgear.ui.screens.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.sportsgear.models.ProductModel
import com.example.sportsgear.navigation.ROUTE_UPDATE_PRODUCT
import com.example.sportsgear.data.ProductViewModel


@Composable
fun ViewProductsScreen(navController: NavHostController) {
    val context = LocalContext.current
  val productRepository = ProductViewModel()

    val emptyProductState = remember {
        mutableStateOf(ProductModel("", "", "", "", "", ""))
    }
    val productListState = remember {
        mutableStateListOf<ProductModel>()
    }

   val products = productRepository.viewProducts(emptyProductState, productListState, context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "All Products",
            fontSize = 30.sp,
            fontFamily = FontFamily.SansSerif,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {
            items(products) { product ->
                ProductItem(
                    product = product,
                    navController = navController,
                    productRepository = productRepository
                )
            }
        }
    }
}

@Composable
fun ProductItem(
    product: ProductModel,
    navController: NavHostController,
    productRepository: ProductViewModel
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .height(220.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.Gray)
    ) {
        Row {
            Column {
                AsyncImage(
                    model = product.imageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(180.dp)
                        .height(140.dp)
                        .padding(10.dp)
                )

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            productRepository.deleteProduct(context, product.productId, navController)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(Color.Red)
                    ) {
                        Text("REMOVE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            navController.navigate("$ROUTE_UPDATE_PRODUCT/${product.productId}")
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(Color.Green)
                    ) {
                        Text("UPDATE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("PRODUCT NAME", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(product.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Text("PRICE", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(product.price, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Text("CATEGORY", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(product.category, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                Text("DESCRIPTION", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(product.description, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
