package com.example.inventoritoko.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object ResetPassword : Screen("reset_password_screen")
    object ProductList : Screen("product_list_screen")
    object Cart : Screen("cart_screen")
    object Checkout : Screen("checkout_screen")
    // No specific route for PaymentSuccessDialog as it's a dialog
}

