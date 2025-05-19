package com.example.sportsgear.ui.theme.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sportsgear.R
import com.example.sportsgear.navigation.ROUTE_ADD_PRODUCT
import com.example.sportsgear.navigation.ROUTE_CART
import com.example.sportsgear.navigation.ROUTE_VIEW_PRODUCTS
import com.example.sportsgear.ui.theme.screens.login.MaroonDark

val Maroon = Color(0xFF800000) // Maroon color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val selectedItem = remember { mutableStateOf(0) }
    val context = LocalContext.current
    val gearItems = listOf("Footwear", "Apparel", "Equipment", "New Arrivals")

    var showDropdown by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Maroon) {
                NavigationBarItem(
                    selected = selectedItem.value == 0,
                    onClick = {
                        selectedItem.value = 0
                        navController.navigate(ROUTE_ADD_PRODUCT)
                    },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Add Product") },
                    label = { Text("Add Product") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = MaroonDark
                    )
                )

                NavigationBarItem(
                    selected = selectedItem.value == 1,
                    onClick = {
                        selectedItem.value = 1
                        navController.navigate(ROUTE_VIEW_PRODUCTS)
                    },
                    icon = { Icon(Icons.Filled.List, contentDescription = "View Products") },
                    label = { Text("View Products") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = MaroonDark
                    )
                )

                NavigationBarItem(
                    selected = selectedItem.value == 2,
                    onClick = {
                        selectedItem.value = 2
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@sportsgear.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Support Request")
                            putExtra(Intent.EXTRA_TEXT, "Hi, I need help with...")
                        }
                        context.startActivity(intent)
                    },
                    icon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                    label = { Text("Email") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = MaroonDark
                    )
                )
            }
        }
    ) { innerPadding ->
        Box {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Sports Gear Background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.padding(innerPadding)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = { Text("Home of sports") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Home, contentDescription = "Home")
                    }
                },
                
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Maroon,
                    titleContentColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Updated layout: 2 cards per row
            for (row in gearItems.chunked(2)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { item ->
                        Card(
                            modifier = Modifier
                                .padding(10.dp)
                                .width(160.dp)
                                .height(120.dp)
                                .clickable {
                                    navController.navigate(ROUTE_VIEW_PRODUCTS)
                                },
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(10.dp),
                            colors = CardDefaults.cardColors(Maroon)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box {
                Button(
                    onClick = { showDropdown = !showDropdown },
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon)
                ) {
                    Text("Add Product", color = Color.White)
                }

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    gearItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                showDropdown = false
                                navController.navigate(ROUTE_ADD_PRODUCT)
                            }
                        )
                    }
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

//package com.example.sportsgear.ui.theme.screens.home
//
//import android.content.Intent
//import android.net.Uri
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
//import com.example.sportsgear.R
//import com.example.sportsgear.navigation.ROUTE_ADD_PRODUCT
//import com.example.sportsgear.navigation.ROUTE_CART
//import com.example.sportsgear.navigation.ROUTE_VIEW_PRODUCTS
//import com.example.sportsgear.ui.theme.screens.login.MaroonDark
//
//val Maroon = Color(0xFF800000) // Maroon color
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun HomeScreen(navController: NavController) {
//    val selectedItem = remember { mutableStateOf(0) }
//    val context = LocalContext.current
//    val gearItems = listOf("Footwear", "Apparel", "Equipment",  "New Arrivals")
//
//    var showDropdown by remember { mutableStateOf(false) }
//
//    Scaffold(
//        bottomBar = {
//            NavigationBar(containerColor = Maroon) {
//                NavigationBarItem(
//                    selected = selectedItem.value == 0,
//                    onClick = {
//                        selectedItem.value = 0
//                        navController.navigate(ROUTE_ADD_PRODUCT)
//                    },
//                    icon = { Icon(Icons.Filled.Add, contentDescription = "Add Product") },
//                    label = { Text("Add Product") },
//                    colors = NavigationBarItemDefaults.colors(
//                        selectedIconColor = Color.White,
//                        unselectedIconColor = Color.White,
//                        selectedTextColor = Color.White,
//                        unselectedTextColor = Color.White,
//                        indicatorColor = MaroonDark
//                    )
//                )
//
//                NavigationBarItem(
//                    selected = selectedItem.value == 1,
//                    onClick = {
//                        selectedItem.value = 1
//                        navController.navigate(ROUTE_VIEW_PRODUCTS)
//                    },
//                    icon = { Icon(Icons.Filled.List, contentDescription = "View Products") },
//                    label = { Text("View Products") },
//                    colors = NavigationBarItemDefaults.colors(
//                        selectedIconColor = Color.White,
//                        unselectedIconColor = Color.White,
//                        selectedTextColor = Color.White,
//                        unselectedTextColor = Color.White,
//                        indicatorColor = MaroonDark
//                    )
//                )
//
//                NavigationBarItem(
//                    selected = selectedItem.value == 2,
//                    onClick = {
//                        selectedItem.value = 2
//                        val intent = Intent(Intent.ACTION_SENDTO).apply {
//                            data = Uri.parse("mailto:support@sportsgear.com")
//                            putExtra(Intent.EXTRA_SUBJECT, "Support Request")
//                            putExtra(Intent.EXTRA_TEXT, "Hi, I need help with...")
//                        }
//                        context.startActivity(intent)
//                    },
//                    icon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
//                    label = { Text("Email") },
//                    colors = NavigationBarItemDefaults.colors(
//                        selectedIconColor = Color.White,
//                        unselectedIconColor = Color.White,
//                        selectedTextColor = Color.White,
//                        unselectedTextColor = Color.White,
//                        indicatorColor = MaroonDark
//                    )
//                )
//            }
//        }
//
//    ) { innerPadding ->
//        Box {
//            Image(
//                painter = painterResource(id = R.drawable.background),
//                contentDescription = "Sports Gear Background",
//                contentScale = ContentScale.FillBounds,
//                modifier = Modifier.padding(innerPadding)
//            )
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(bottom = 60.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            TopAppBar(
//                title = { Text("Home of sports") },
//                navigationIcon = {
//                    IconButton(onClick = {}) {
//                        Icon(Icons.Filled.Home, contentDescription = "Home")
//                    }
//                },
//                actions = {
//                    IconButton(onClick = {}) {
//                        Icon(Icons.Filled.Person, contentDescription = "Profile")
//                    }
//                    IconButton(onClick = {}) {
//                        Icon(Icons.Filled.Search, contentDescription = "Search")
//                    }
//                    IconButton(onClick = {}) {
//                        Icon(Icons.Filled.Close, contentDescription = "Logout")
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Maroon,
//                    titleContentColor = Color.White
//                )
//            )
//
//            for (row in gearItems.chunked(3)) {
//                Row(modifier = Modifier.wrapContentWidth()) {
//                    row.forEach { item ->
//                        Card(
//                            modifier = Modifier
//                                .padding(10.dp)
//                                .clickable {
//                                    navController.navigate(ROUTE_VIEW_PRODUCTS)
//                                },
//                            shape = RoundedCornerShape(20.dp),
//                            elevation = CardDefaults.cardElevation(10.dp),
//                            colors = CardDefaults.cardColors(Maroon)
//                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .height(100.dp)
//                                    .padding(25.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(item, color = Color.White)
//                            }
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Box {
//                Button(
//                    onClick = { showDropdown = !showDropdown },
//                    colors = ButtonDefaults.buttonColors(containerColor = Maroon)
//                ) {
//                    Text("Add Product", color = Color.White)
//                }
//
//                DropdownMenu(
//                    expanded = showDropdown,
//                    onDismissRequest = { showDropdown = false }
//                ) {
//                    gearItems.forEach { item ->
//                        DropdownMenuItem(
//                            text = { Text(item) },
//                            onClick = {
//                                showDropdown = false
//                                navController.navigate(ROUTE_ADD_PRODUCT)
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun HomeScreenPreview() {
//    HomeScreen(rememberNavController())
//}
