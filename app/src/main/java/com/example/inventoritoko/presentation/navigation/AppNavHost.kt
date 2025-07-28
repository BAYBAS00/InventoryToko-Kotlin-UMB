// AppNavHost.kt
package com.example.inventoritoko.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.inventoritoko.presentation.screens.*
import android.util.Log // Import Log

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route
) {
    Log.d("AppNavHost", "Building NavHost with startDestination: $startDestination")
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            Log.d("AppNavHost", "Composable for LoginScreen added.")
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            Log.d("AppNavHost", "Composable for RegisterScreen added.")
            RegisterScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            Log.d("AppNavHost", "Composable for ForgotPasswordScreen added.")
            ForgotPasswordScreen(navController = navController)
        }
        composable(Screen.ResetPassword.route) {
            Log.d("AppNavHost", "Composable for ResetPasswordScreen added.")
            ResetPasswordScreen(navController = navController)
        }
        composable(Screen.ProductList.route) {
            Log.d("AppNavHost", "Composable for ProductListScreen added.")
            ProductListScreen(navController = navController)
        }
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            Log.d("AppNavHost", "Composable for ProductDetailScreen added.")
            val productId = backStackEntry.arguments?.getInt("productId")
            if (productId != null) {
                ProductDetailScreen(navController = navController, productId = productId)
            } else {
                navController.popBackStack()
            }
        }
        composable(
            route = Screen.Checkout.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("quantity") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            Log.d("AppNavHost", "Composable for CheckoutScreen added.")
            val productIdString = backStackEntry.arguments?.getString("productId")
            val quantityString = backStackEntry.arguments?.getString("quantity")

            val productId = productIdString?.toIntOrNull()
            val quantity = quantityString?.toIntOrNull()

            CheckoutScreen(navController = navController, directProductId = productId, directQuantity = quantity)
        }
        composable(Screen.Cart.route) { // <--- PASTIKAN BAGIAN INI ADA DAN BENAR
            Log.d("AppNavHost", "Composable for CartScreen added.")
            CartScreen(navController = navController)
        }
        composable(Screen.PurchaseHistory.route) {
            Log.d("AppNavHost", "Composable for PurchaseHistoryScreen added.")
            PurchaseHistoryScreen(navController = navController)
        }
    }
}