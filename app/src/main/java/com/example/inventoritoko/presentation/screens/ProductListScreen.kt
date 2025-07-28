package com.example.inventoritoko.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.inventoritoko.data.model.Product
import com.example.inventoritoko.presentation.navigation.Screen
import com.example.inventoritoko.presentation.viewmodel.AuthViewModel
import com.example.inventoritoko.presentation.viewmodel.AuthViewModelFactory
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModel
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModelFactory
import com.example.inventoritoko.utils.Constants
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(navController: NavController) {
    val context = LocalContext.current
    val inventoryViewModel: InventoryViewModel = viewModel(factory = InventoryViewModelFactory(context))
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    val products by inventoryViewModel.products.collectAsState()
    val loading by inventoryViewModel.loading.collectAsState()
    val error by inventoryViewModel.error.collectAsState()
    val addToCartResult by inventoryViewModel.addToCartResult.collectAsState()

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            inventoryViewModel.clearError()
        }
    }

    LaunchedEffect(addToCartResult) {
        addToCartResult?.let { success ->
            if (success) {
                Toast.makeText(context, "Produk ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal menambahkan produk ke keranjang.", Toast.LENGTH_SHORT).show()
            }
            inventoryViewModel.clearAddToCartResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Produk") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
                    }
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ProductList.route) { inclusive = true } // Hapus semua dari back stack
                        }
                        Toast.makeText(context, "Logged out berhasil", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (products.isEmpty() && !loading && error == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada produk yang tersedia.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        ProductItem(product = product) {
                            inventoryViewModel.addToCart(product.id, 1) // Add 1 quantity by default
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductItem(product: Product, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle product detail navigation if needed */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val fullImageUrl = if (product.image != null) {
                // Pastikan BASE_URL diakhiri dengan '/' dan product.image diawali dengan '/'
                // atau hapus '/' di product.image jika BASE_URL sudah menanganinya
                "${Constants.BASE_URL.removeSuffix("/")}${product.image}"
            } else {
                "https://placehold.co/100x100/E0E0E0/000000?text=No+Image"
            }
            AsyncImage(
                model = fullImageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = formatCurrency(product.price), style = MaterialTheme.typography.bodyMedium)
                Text(text = "Stock: ${product.stock}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onAddToCart) {
                Text("Add to Cart")
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")) // Indonesian Rupiah
    return format.format(amount)
}

@Preview(showBackground = true)
@Composable
fun PreviewProductListScreen() {
    ProductListScreen(rememberNavController())
}

@Preview(showBackground = true)
@Composable
fun PreviewProductItem() {
    ProductItem(product = Product(1, "Sample Product", 15000.0, 10, null)) {}
}