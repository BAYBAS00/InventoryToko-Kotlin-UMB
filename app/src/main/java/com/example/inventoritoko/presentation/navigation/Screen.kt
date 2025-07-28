// Screen.kt
package com.example.inventoritoko.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object ResetPassword : Screen("reset_password_screen")
    object ProductList : Screen("product_list_screen")
    object ProductDetail : Screen("product_detail_screen/{productId}") {
        fun createRoute(productId: Int) = "product_detail_screen/$productId"
    }
    object Cart : Screen("cart_screen")
    // Mengubah rute Checkout untuk menerima productId dan quantity sebagai opsional
    object Checkout : Screen("checkout_screen?productId={productId}&quantity={quantity}") {
        fun createRoute(productId: Int? = null, quantity: Int? = null): String {
            return if (productId != null && quantity != null) {
                "checkout_screen?productId=$productId&quantity=$quantity"
            } else {
                "checkout_screen"
            }
        }
    }
    object PurchaseHistory : Screen("purchase_history_screen")
    // No specific route for PaymentSuccessDialog as it's a dialog
}