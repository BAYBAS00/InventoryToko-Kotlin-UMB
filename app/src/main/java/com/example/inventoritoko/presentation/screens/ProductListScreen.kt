// ProductListScreen.kt
package com.example.inventoritoko.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.inventoritoko.utils.formatCurrency
import android.util.Log
import androidx.navigation.compose.currentBackStackEntryAsState

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

    // Observer untuk error dari InventoryViewModel
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            inventoryViewModel.clearError()
        }
    }

    // Observer untuk hasil penambahan ke keranjang
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
                    IconButton(onClick = {
                        Log.d("ProductListScreen", "Navigating to PurchaseHistoryScreen")
                        navController.navigate(Screen.PurchaseHistory.route)
                    }) {
                        Icon(Icons.Filled.History, contentDescription = "Riwayat Pembelian")
                    }
                    IconButton(onClick = {
                        Log.d("ProductListScreen", "Navigating to CartScreen")
                        navController.navigate(Screen.Cart.route)
                    }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Keranjang")
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products) { product ->
                        ProductGridItem(product = product) {
                            navController.navigate(Screen.ProductDetail.createRoute(product.id))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductGridItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val fullImageUrl = if (product.image != null) {
                    "${Constants.BASE_URL.removeSuffix("/")}${product.image}"
                } else {
                    Constants.NO_IMAGE_PLACEHOLDER_URL
                }
                AsyncImage(
                    model = fullImageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(product.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stok: ${product.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun PreviewProductListScreen() {
    ProductListScreen(rememberNavController())
}

@Preview(showBackground = true, widthDp = 180)
@Composable
fun PreviewProductGridItem() {
    ProductGridItem(
        product = Product(
            id = 1,
            name = "Nama Produk Contoh yang Panjang Sekali untuk Menguji Overflow",
            price = 223110.0,
            stock = 15,
            image = null,
            description = "Ini adalah deskripsi produk contoh."
        )
    ) {}
}
