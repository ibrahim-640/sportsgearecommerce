package com.example.sportsgear.navigation

import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sportsgear.ui.theme.screens.*
import com.example.sportsgear.ui.theme.screens.login.LoginScreen
import com.example.sportsgear.ui.theme.screens.register.RegisterScreen
import com.example.sportsgear.ui.theme.screens.CartScreen
import com.example.sportsgear.ui.theme.screens.OrderHistoryScreen
import com.example.sportsgear.ui.theme.screens.ProductDetailScreen
import com.example.sportsgear.ui.theme.screens.EditProfileScreen
import com.example.sportsgear.ui.theme.screens.ProfileScreen
import com.example.sportsgear.ui.theme.screens.StartScreen
import com.example.sportsgear.ui.screens.screens.AddProductScreen
import com.example.sportsgear.ui.screens.screens.ViewProductsScreen
import com.example.sportsgear.ui.screens.HomeScreen
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.OrderViewModel
import com.example.sportsgear.models.Product
import com.example.sportsgear.ui.screens.PaymentScreen
import com.example.sportsgear.ui.screens.UpdateProductScreen
import com.example.sportsgear.ui.theme.AdminDashBoardScreens.EditProductScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.sportsgearecommerce.ui.screens.admin.AdminDashboardScreen
import com.google.firebase.database.*
import com.yourapp.ui.screens.debug.DebugScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_DEBUG
) {
    val context = LocalContext.current

    val authViewModel: AuthViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()


    NavHost(navController = navController, startDestination = startDestination) {
        composable(ROUTE_DEBUG) {
            DebugScreen {
                navController.navigate(ROUTE_REGISTER) {
                    popUpTo(ROUTE_DEBUG) { inclusive = true }
                }
            }
        }

        composable(ROUTE_REGISTER) {
            RegisterScreen(navController)
        }

        composable(ROUTE_LOGIN) {
            LoginScreen(navController)
        }
        composable(ROUTE_STARTER) {
            StartScreen(navController)
        }


        composable(ROUTE_HOME) {
            HomeScreen(
                navController = navController,
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                authViewModel = authViewModel
            )
        }

        composable(ROUTE_ORDER) {
            OrderHistoryScreen()
        }

        composable(ROUTE_ADD_PRODUCT) {
            AddProductScreen(
                navController = navController,
                productViewModel = productViewModel,
                authViewModel = authViewModel
            )
        }

        composable(ROUTE_EDIT_PROFILE) {
            EditProfileScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(ROUTE_VIEW_PRODUCTS) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            ViewProductsScreen(
                navController = navController,
                userId = userId,
                cartViewModel = cartViewModel
            )
        }

        composable(ROUTE_SPLASH) {
            StartScreen(navController)
        }

        composable(ROUTE_CART) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null) {
                CartScreen(
                    userId = currentUserId,
                    cartViewModel = cartViewModel,
                    navController = navController,
                )
            } else {
                Text("User not logged in")
            }
        }

        composable(
            route = "$ROUTE_UPDATE_PRODUCT/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            UpdateProductScreen(
                navController = navController,
                productId = productId,
                productViewModel = productViewModel
            )
        }

        // ✅ FIXED: Product Detail Route (Now matches ProductViewModel style)
        composable("$ROUTE_PRODUCT_DETAIL/{productId}") { backStackEntry ->
            val context = LocalContext.current
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            var product by remember { mutableStateOf<Product?>(null) }

            DisposableEffect(productId) {
                val productRef = FirebaseDatabase.getInstance().getReference("Products").child(productId)
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val fetched = snapshot.getValue(Product::class.java)
                        product = fetched
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, "Failed to load product: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                productRef.addValueEventListener(listener)
                onDispose {
                    productRef.removeEventListener(listener)
                }
            }

            product?.let {
                ProductDetailScreen(
                    product = it,
                    onAddToCart = {
                        cartViewModel.addToCart(userId, it)
                    },
                    onBack = { navController.popBackStack() }
                )
            } ?: run {
                Text("Loading product...")
            }
        }

        composable(ROUTE_CATEGORY) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "All"
            CategoryScreen(
                navController = navController,
                categoryName = categoryName,
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                authViewModel = authViewModel
            )
        }

        composable(ROUTE_ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                navController = navController,
                productViewModel = productViewModel
            )
        }

        composable(
            route = ROUTE_EDITPRODUCT,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""

            EditProductScreen(
                navController = navController,
                productId = productId,
                productViewModel = productViewModel,
                authViewModel = authViewModel
            )
        }

        composable(
            route = "$ROUTE_EDIT_CARTPRODUCT/{productJson}",
            arguments = listOf(navArgument("productJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val productJson = backStackEntry.arguments?.getString("productJson")
            val product = Gson().fromJson(productJson, Product::class.java)

            EditCartProductScreen(
                product = product,
                userId = userId,
                cartViewModel = cartViewModel,
                navController = navController
            )
        }

        composable(ROUTE_PROFILE) {
            ProfileScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable(ROUTE_CHECKOUT) {
            val cartViewModel: CartViewModel = viewModel()
            val orderViewModel: OrderViewModel = viewModel()

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            CheckoutScreen(
                userId = userId,
                cartViewModel = cartViewModel,
                navController = navController,
                orderViewModel = orderViewModel
            )
        }
        composable(
            route = "$ROUTE_PAYMENT/{amount}/{paymentMethod}",
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("paymentMethod") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0"
            val phone = backStackEntry.arguments?.getString("phone") ?: ""

            PaymentScreen(
                navController = navController,
                amount = amount,
                phone = phone,
            )
        }

        composable(
            route = "$ROUTE_SUCCESS/{amount}/{method}",
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("method") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0"
            val method = backStackEntry.arguments?.getString("method") ?: "Unknown"

            val orderViewModel: OrderViewModel = viewModel()
            SuccessScreen(
                navController = navController,
                amount = amount,
                method = method,
                cartViewModel = cartViewModel,
                orderViewModel = orderViewModel,
            )
        }

    }
}
