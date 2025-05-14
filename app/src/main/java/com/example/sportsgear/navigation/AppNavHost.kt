// AppNavHost.kt
package com.example.sportsgear.navigation

import LoginScreen
import ViewProductsScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

//import com.example.sportsgear.ui.screens.products.UpdateProductScreen
import com.example.sportsgear.ui.theme.screens.AddProductScreen
import com.example.sportsgear.ui.theme.screens.HomeScreen
import com.example.sportsgear.ui.theme.screens.RegisterScreen
import com.example.sportsgear.ui.theme.screens.SplashScreen


@Composable
fun AppNavHost(navController:NavHostController= rememberNavController(),startDestination:String= ROUTE_REGISTER){
    NavHost(navController=navController,startDestination=startDestination){
//        composable(ROUTE_LOGIN){ SplashScreen{
//            navController.navigate(ROUTE_REGISTER){
//                popUpTo(ROUTE_LOGIN){inclusive=true}} } }
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
            AddProductScreen(navController)
        }
        composable(ROUTE_VIEW_PRODUCTS) {
            ViewProductsScreen(navController)
        }
//        composable("$ROUTE_UPDATE_PRODUCT/{productId}") { backStackEntry ->
//            val productId = backStackEntry.arguments?.getString("productId") ?: ""
//            UpdateProductScreen(navController, productId)
        }
    }

