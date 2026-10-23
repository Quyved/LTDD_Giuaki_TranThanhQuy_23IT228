package com.example.ecomer.navigation

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object AdminProducts : Screen("admin_products")
    object UserProducts : Screen("user_products")
}
