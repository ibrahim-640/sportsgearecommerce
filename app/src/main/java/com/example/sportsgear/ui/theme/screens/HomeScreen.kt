package com.example.sportsgear.ui.theme.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sportsgear.R
import com.example.sportsgear.navigation.ROUTE_ADD_PRODUCT
import com.example.sportsgear.navigation.ROUTE_VIEW_PRODUCTS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sports Gear Home") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Welcome To Sports Gear Shop!")
            Spacer(modifier = Modifier.height(16.dp))
            // Add Product Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(ROUTE_ADD_PRODUCT)
                    },
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text("Add Product")
                }
            }

                Box() {
                    Image(
                        painter = painterResource(id = R.drawable.background),
                        contentDescription = "background image",
                        contentScale = ContentScale.FillBounds,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // View Products Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(ROUTE_VIEW_PRODUCTS)
                        },
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text("View Products")
                    }
                }

            }
        }
    }

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(rememberNavController())
}

