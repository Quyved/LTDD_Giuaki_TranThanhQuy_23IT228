package com.example.ecomer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecomer.ui.screens.AdminProductsScreen
import com.example.ecomer.ui.screens.SignInScreen
import com.example.ecomer.ui.screens.SignUpScreen
import com.example.ecomer.ui.screens.UserProductsScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.SignIn.route
    ) {
        composable(Screen.SignIn.route) {
            SignInScreen(navController = navController)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        composable(Screen.AdminProducts.route) {
            AdminProductsScreen(navController = navController)
        }
        composable(Screen.UserProducts.route) {
            UserProductsScreen(navController = navController)
        }
    }
}
