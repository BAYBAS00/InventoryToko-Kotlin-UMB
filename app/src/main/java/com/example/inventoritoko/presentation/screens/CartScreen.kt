// CartScreen.kt
package com.example.inventoritoko.presentation.screens // Updated package name

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add // Added import for Add icon
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove // Added import for Remove icon
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
import com.example.inventoritoko.data.model.CartItem // Updated import
import com.example.inventoritoko.data.model.Product // Updated import
import com.example.inventoritoko.presentation.navigation.Screen // Updated import
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModel // Updated import
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModelFactory // Updated import
import com.example.inventoritoko.utils.Constants // Updated import
import com.example.inventoritoko.utils.formatCurrency // Added import for formatCurrency
// Removed java.text.NumberFormat and java.util.Locale imports as formatCurrency is now in utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val context = LocalContext.current
    val inventoryViewModel: InventoryViewModel = viewModel(factory = InventoryViewModelFactory(context))

    val cartItems by inventoryViewModel.cartItems.collectAsState()
    val loading by inventoryViewModel.loading.collectAsState()
    val error by inventoryViewModel.error.collectAsState()
    val deleteCartItemResult by inventoryViewModel.deleteCartItemResult.collectAsState()
    val clearCartResult by inventoryViewModel.clearCartResult.collectAsState()
    val updateCartQuantityResult by inventoryViewModel.updateCartQuantityResult.collectAsState() // State baru

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            inventoryViewModel.clearError()
        }
    }

    LaunchedEffect(deleteCartItemResult) {
        deleteCartItemResult?.let { success ->
            if (success) {
                Toast.makeText(context, "Produk dihapus dari keranjang!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal menghapus produk dari keranjang.", Toast.LENGTH_SHORT).show()
            }
            inventoryViewModel.clearDeleteCartItemResult()
        }
    }

    LaunchedEffect(clearCartResult) {
        clearCartResult?.let { success ->
            if (success) {
                Toast.makeText(context, "Keranjang berhasil dikosongkan!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal mengosongkan keranjang.", Toast.LENGTH_SHORT).show()
            }
            inventoryViewModel.clearClearCartResult()
        }
    }

    // Efek untuk hasil pembaruan kuantitas
    LaunchedEffect(updateCartQuantityResult) {
        updateCartQuantityResult?.let { success ->
            if (success) {
                Toast.makeText(context, "Kuantitas diperbarui!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal memperbarui kuantitas.", Toast.LENGTH_SHORT).show()
            }
            inventoryViewModel.clearUpdateCartQuantityResult()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang Anda") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(
                            onClick = { inventoryViewModel.clearCart() },
                            enabled = !loading
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Kosongkan Keranjang")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val totalPrice = cartItems.sumOf { item ->
                        item.product?.price?.times(item.quantity) ?: 0.0
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total: ${formatCurrency(totalPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { navController.navigate(Screen.Checkout.route) },
                            enabled = !loading
                        ) {
                            Text("Checkout")
                        }
                    }
                }
            }
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

            if (cartItems.isEmpty() && !loading && error == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Keranjang Anda kosong.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemCard(
                            item = item,
                            onRemoveItem = { productId ->
                                inventoryViewModel.deleteCartItem(productId)
                            },
                            onQuantityChange = { productId, newQuantity -> // Callback baru
                                inventoryViewModel.updateCartItemQuantity(productId, newQuantity)
                            },
                            isLoading = loading // Pass loading state to disable buttons
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CartItemCard(
    item: CartItem,
    onRemoveItem: (productId: Int) -> Unit,
    onQuantityChange: (productId: Int, newQuantity: Int) -> Unit, // Callback baru
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = if (item.product?.image != null) {
                "${Constants.BASE_URL.removeSuffix("/")}${item.product.image}"
            } else {
                "https://placehold.co/80x80/E0E0E0/000000?text=No+Image"
            }
            val productName = item.product?.name ?: "Unknown Product"
            val productPrice = item.product?.price ?: 0.0
            AsyncImage(
                model = imageUrl,
                contentDescription = productName,
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Quantity: ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Price: ${formatCurrency(productPrice)}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Subtotal: ${formatCurrency(productPrice * item.quantity)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                Spacer(modifier = Modifier.height(8.dp)) // Spacer untuk memisahkan teks dan kontrol kuantitas

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tombol Kurangi Kuantitas
                    IconButton(
                        onClick = { onQuantityChange(item.productId, item.quantity - 1) },
                        enabled = !isLoading && item.quantity > 0 // Nonaktifkan jika loading atau kuantitas sudah 0
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Kurangi Kuantitas")
                    }
                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    // Tombol Tambah Kuantitas
                    IconButton(
                        onClick = { onQuantityChange(item.productId, item.quantity + 1) },
                        enabled = !isLoading // Nonaktifkan jika loading
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Tambah Kuantitas")
                    }
                }
            }
            IconButton(
                onClick = { onRemoveItem(item.productId) },
                modifier = Modifier.align(Alignment.CenterVertically),
                enabled = !isLoading
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Hapus Item")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCartScreen() {
    CartScreen(rememberNavController())
}

@Preview(showBackground = true)
@Composable
fun PreviewCartItemCard() {
    CartItemCard(
        item = CartItem(
            id = 1,
            userId = 1,
            productId = 1,
            quantity = 2,
            product = Product(1, "Sample Product", 15000.0, 10, null)
        ),
        onRemoveItem = {}, // Added missing lambda
        onQuantityChange = { _, _ -> }, // Added missing lambda
        isLoading = false // Added missing argument
    )
}