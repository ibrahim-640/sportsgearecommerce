import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sportsgear.data.AuthViewModel
import com.example.sportsgear.navigation.ROUTE_ADD_PRODUCT
import com.example.sportsgear.navigation.ROUTE_HOME
import com.example.sportsgear.navigation.ROUTE_LOGIN
import com.example.sportsgear.navigation.ROUTE_PRODUCT_DETAIL
import com.example.sportsgear.navigation.ROUTE_PRODUCT_LIST
import com.example.sportsgear.navigation.ROUTE_REGISTER
import com.example.sportsgear.navigation.ROUTE_SPLASH
import com.example.sportsgear.ui.theme.screens.AddProductScreen
import com.example.sportsgear.ui.theme.screens.HomeScreen
import com.example.sportsgear.ui.theme.screens.ProductDetailScreen
import com.example.sportsgear.ui.theme.screens.SplashScreen
import com.example.sportsgear.ui.theme.screens.register.RegisterScreen

@Composable
fun AppNavHost(navController:NavHostController= rememberNavController(),startDestination:String= ROUTE_SPLASH){
    NavHost(navController=navController,startDestination=startDestination){
        composable(ROUTE_SPLASH){ SplashScreen{
            navController.navigate(ROUTE_REGISTER){
                popUpTo(ROUTE_SPLASH){inclusive=true}} } }
        composable(ROUTE_REGISTER) { RegisterScreen(navController) }
        composable(ROUTE_LOGIN) { LoginScreen(navController) }
        composable(ROUTE_HOME) { HomeScreen(navController) }
        composable(ROUTE_ADD_PRODUCT) { AddProductScreen(navController) }
//        composable(ROUTE_PRODUCT_DETAIL){ ProductDetailScreen(navController) }
//        composable("ROUTE_PRODUCT_LIST/{productId}") {
//                passedData -> ProductListScreen(
//            navController, passedData.arguments?.getString("productId")!! )
        }
    }

