// ProductDetailScreen.kt
package com.example.inventoritoko.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModel
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModelFactory
import com.example.inventoritoko.utils.Constants
import com.example.inventoritoko.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(navController: NavController, productId: Int) {
    val context = LocalContext.current
    val inventoryViewModel: InventoryViewModel = viewModel(factory = InventoryViewModelFactory(context))

    val selectedProduct by inventoryViewModel.selectedProduct.collectAsState()
    val loading by inventoryViewModel.loading.collectAsState()
    val error by inventoryViewModel.error.collectAsState()
    val addToCartResult by inventoryViewModel.addToCartResult.collectAsState()

    LaunchedEffect(productId) {
        inventoryViewModel.fetchProductById(productId)
    }

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
                title = { Text(selectedProduct?.name ?: "Detail Produk") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Keranjang")
                    }
                }
            )
        },
        bottomBar = {
            selectedProduct?.let { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { inventoryViewModel.addToCart(product.id, 1) },
                        modifier = Modifier.weight(1f),
                        enabled = !loading && product.stock > 0
                    ) {
                        Text("[+] Keranjang")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Navigasi ke CheckoutScreen dengan productId dan quantity untuk beli langsung
                            navController.navigate(Screen.Checkout.createRoute(product.id, 1))
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !loading && product.stock > 0 // Hanya bisa beli jika stok > 0
                    ) {
                        Text("Beli Sekarang") // Mengubah teks tombol
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            selectedProduct?.let { product ->
                val fullImageUrl = if (product.image != null) {
                    "${Constants.BASE_URL.removeSuffix("/")}${product.image}"
                } else {
                    Constants.NO_IMAGE_PLACEHOLDER_URL
                }

                AsyncImage(
                    model = fullImageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatCurrency(product.price),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Stok: ${product.stock}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = product.description ?: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.weight(1f)) // Spacer fleksibel untuk mendorong konten ke atas
            } ?: run {
                if (!loading && error == null) {
                    Text("Produk tidak ditemukan.", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProductDetailScreen() {
    ProductDetailScreen(navController = rememberNavController(), productId = 1)
}