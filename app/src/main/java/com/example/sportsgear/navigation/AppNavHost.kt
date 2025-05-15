
package com.example.sportsgear.navigation
import com.example.sportsgear.ui.theme.screens.login.LoginScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportsgear.data.ProductViewModel
import com.example.sportsgear.ui.screens.screens.AddProductScreen
import com.example.sportsgear.ui.screens.screens.ViewProductsScreen
import com.example.sportsgear.ui.theme.screens.SplashScreen
import com.example.sportsgear.ui.theme.screens.home.HomeScreen
import com.example.sportsgear.ui.theme.screens.register.RegisterScreen

@Composable
fun AppNavHost(navController:NavHostController= rememberNavController(),startDestination:String= ROUTE_SPLASH){
    NavHost(navController=navController,startDestination=startDestination){
        composable(ROUTE_SPLASH){ SplashScreen{
            navController.navigate(ROUTE_REGISTER){
                popUpTo(ROUTE_SPLASH){inclusive=true}} } }
        composable(ROUTE_REGISTER) {
            RegisterScreen(navController)
        }

        composable(ROUTE_LOGIN) {
            LoginScreen(navController)
        }
        composable(ROUTE_HOME) {
            HomeScreen(navController)
        }
        composable(ROUTE_ADD_PRODUCT) {
            val productViewModel: ProductViewModel = viewModel()
            AddProductScreen(navController, productViewModel) // ✅ Correct
        }
        composable(ROUTE_VIEW_PRODUCTS) {
            ViewProductsScreen(navController)
        }
//        composable("$ROUTE_UPDATE_PRODUCT/{productId}") { backStackEntry ->
//            val productId = backStackEntry.arguments?.getString("productId") ?: ""
//            UpdateProductScreen(navController, productId)
        }
    }

