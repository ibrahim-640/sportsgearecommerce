package com.example.sportsgear.navigation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.data.CartViewModel
import com.example.sportsgear.data.OrderViewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.ui.screens.AddProductScreen
import com.example.sportsgear.ui.screens.HomeScreen
import com.example.sportsgear.ui.screens.PaymentScreen
import com.example.sportsgear.ui.screens.UpdateProductScreen
import com.example.sportsgear.ui.screens.ViewProductsScreen
import com.example.sportsgear.ui.screens.admin.AdminDashboardScreen
import com.example.sportsgear.ui.screens.admin.EditProductScreen
import com.example.sportsgear.ui.theme.screens.*
import com.example.sportsgear.ui.theme.screens.login.LoginScreen
import com.example.sportsgear.ui.theme.screens.register.RegisterScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_SPLASH
) {
    // ✅ The ONE shared instance of each. Every screen below now receives
    // these as parameters instead of creating its own — this is what makes
    // the cart-loading fix AND the auth-listener-leak fix actually work as
    // intended, instead of each screen silently spinning up a disconnected
    // copy.
    val authViewModel: AuthViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()

    val currentUser by authViewModel.currentUser.collectAsState()
    // Replace the existing LaunchedEffect(currentUser?.uid) block with this:
    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            cartViewModel.loadCartItems(uid)
        } else {
            // ✅ FIX — user just logged out. Detach the Firebase listener
            // immediately, before Firebase's security rules see an unauthenticated
            // read attempt on Cart/{uid} and fire the permission-denied error.
            // Without this, the ValueEventListener attached during loadCartItems
            // keeps firing after logout since CartViewModel outlives the session
            // (it's scoped to the NavHost, not a single back-stack entry).
            cartViewModel.detachListener()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ----------------------------------------------------------
        // SPLASH
        // ----------------------------------------------------------
        composable(ROUTE_SPLASH) {
            SplashScreen {
                navController.navigate(ROUTE_REGISTER) {
                    popUpTo(ROUTE_SPLASH) { inclusive = true }
                }
            }
        }

        // ----------------------------------------------------------
        // REGISTER
        // ----------------------------------------------------------
        composable(ROUTE_REGISTER) {
            RegisterScreen(navController, authViewModel = authViewModel)
        }

        // ----------------------------------------------------------
        // LOGIN
        // ----------------------------------------------------------
        composable(ROUTE_LOGIN) {
            LoginScreen(navController, authViewModel = authViewModel)
        }

        // ----------------------------------------------------------
        // STARTER
        // ----------------------------------------------------------
        composable(ROUTE_STARTER) {
            StartScreen(navController, authViewModel = authViewModel)
        }

        // ----------------------------------------------------------
        // HOME
        // ✅ now receives the shared authViewModel too
        // ----------------------------------------------------------
        composable(ROUTE_HOME) {
            HomeScreen(
                navController = navController,
                cartViewModel = cartViewModel,
                authViewModel = authViewModel
            )
        }

        // ----------------------------------------------------------
        // PROFILE
        // ----------------------------------------------------------
        composable(ROUTE_PROFILE) {
            ProfileScreen(navController = navController, authViewModel = authViewModel)
        }

        // ----------------------------------------------------------
        // EDIT PROFILE
        // ----------------------------------------------------------
        composable(ROUTE_EDIT_PROFILE) {
            EditProfileScreen(navController = navController, authViewModel = authViewModel)
        }

        // ----------------------------------------------------------
// ORDERS
// ✅ now passes the shared authViewModel, so OrderHistoryScreen knows
// whether to show all orders (admin) or just this user's
// ----------------------------------------------------------
        composable(ROUTE_ORDER) {
            OrderHistoryScreen(authViewModel = authViewModel)
        }

        // ----------------------------------------------------------
        // CART
        // ✅ now receives the shared authViewModel too
        // ----------------------------------------------------------
        composable(ROUTE_CART) {
            CartScreen(
                navController = navController,
                cartViewModel = cartViewModel,
                authViewModel = authViewModel
            )
        }

        // ----------------------------------------------------------
        // CHECKOUT
        // ----------------------------------------------------------
        composable(ROUTE_CHECKOUT) {
            val orderViewModel: OrderViewModel = viewModel()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            CheckoutScreen(
                userId = userId,
                cartViewModel = cartViewModel,
                navController = navController,
                orderViewModel = orderViewModel
            )
        }

        // ----------------------------------------------------------
        // PAYMENT
        // ----------------------------------------------------------
        composable(
            route = "$ROUTE_PAYMENT/{amount}/{phone}",
            arguments = listOf(
                navArgument("amount") { type = NavType.StringType },
                navArgument("phone") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0"
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            PaymentScreen(
                navController = navController,
                amount = amount,
                phone = phone
            )
        }

        // ----------------------------------------------------------
        // SUCCESS
        // ----------------------------------------------------------
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
            val productViewModel: ProductViewModel = viewModel()
            SuccessScreen(
                navController = navController,
                amount = amount,
                method = method,
                cartViewModel = cartViewModel,
                orderViewModel = orderViewModel,
                productViewModel = viewModel()
            )
        }

        // ----------------------------------------------------------
        // PRODUCT DETAIL
        // ----------------------------------------------------------
        composable(
            route = ROUTE_PRODUCT_DETAIL,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val productViewModel: ProductViewModel = viewModel()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val allProducts by productViewModel.productList.collectAsState()
            val product = allProducts.find { it.productId == productId }

            LaunchedEffect(Unit) {
                productViewModel.fetchProducts()
            }

            if (product != null) {
                ProductDetailScreen(
                    product = product,
                    onAddToCart = { cartViewModel.addToCart(userId, product) },
                    onBack = { navController.popBackStack() }
                )
            } else {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // ----------------------------------------------------------
        // CATEGORY
        // ✅ now receives the shared authViewModel too
        // ----------------------------------------------------------
        composable(
            route = ROUTE_CATEGORY,
            arguments = listOf(
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "All"
            CategoryScreen(
                navController = navController,
                categoryName = categoryName,
                cartViewModel = cartViewModel,
                authViewModel = authViewModel
            )
        }

        // ----------------------------------------------------------
        // ADD PRODUCT
        // ----------------------------------------------------------
        composable(ROUTE_ADD_PRODUCT) {
            AddProductScreen(navController = navController, authViewModel = authViewModel)
        }

        // ----------------------------------------------------------
        // VIEW PRODUCTS
        // ----------------------------------------------------------
        composable(ROUTE_VIEW_PRODUCTS) {
            ViewProductsScreen(navController = navController)
        }

        // ----------------------------------------------------------
        // UPDATE PRODUCT
        // ----------------------------------------------------------
        composable(
            route = "$ROUTE_UPDATE_PRODUCT/{productId}",
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            UpdateProductScreen(
                navController = navController,
                productId = productId,
                authViewModel = authViewModel
            )
        }

        // ----------------------------------------------------------
        // ADMIN DASHBOARD
        // ----------------------------------------------------------
        composable(ROUTE_ADMIN_DASHBOARD) {
            AdminDashboardScreen(navController = navController, authViewModel = authViewModel)
        }

        // ----------------------------------------------------------
        // EDIT PRODUCT (Admin)
        // ----------------------------------------------------------
        composable(
            route = ROUTE_EDITPRODUCT,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            EditProductScreen(
                navController = navController,
                productId = productId,
                authViewModel = authViewModel
            )
        }

        // ----------------------------------------------------------
        // EDIT CART PRODUCT
        // ✅ now receives the shared authViewModel too
        // ----------------------------------------------------------
        composable(
            route = ROUTE_EDIT_CARTPRODUCT,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            EditCartProductScreen(
                navController = navController,
                productId = productId,
                cartViewModel = cartViewModel,
                authViewModel = authViewModel
            )
        }
    }
}