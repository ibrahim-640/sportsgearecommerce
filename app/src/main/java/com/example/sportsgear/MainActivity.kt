// MainActivity.kt
package com.example.sportsgear
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.sportsgear.navigation.AppNavHost
import com.example.sportsgear.ui.theme.SportsgearTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SportsgearTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
