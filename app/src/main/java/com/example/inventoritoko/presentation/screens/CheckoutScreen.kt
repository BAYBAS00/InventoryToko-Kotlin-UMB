// CheckoutScreen.kt
package com.example.inventoritoko.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
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
import com.example.inventoritoko.data.model.CartItem
import com.example.inventoritoko.data.model.Product
import com.example.inventoritoko.presentation.navigation.Screen
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModel
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModelFactory
import com.example.inventoritoko.utils.Constants
import com.example.inventoritoko.utils.formatCurrency
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    directProductId: Int? = null,
    directQuantity: Int? = null
) {
    val context = LocalContext.current
    val inventoryViewModel: InventoryViewModel = viewModel(factory = InventoryViewModelFactory(context))

    val cartItems by inventoryViewModel.cartItems.collectAsState()
    val loading by inventoryViewModel.loading.collectAsState()
    val error by inventoryViewModel.error.collectAsState()
    val checkoutResult by inventoryViewModel.checkoutResult.collectAsState()

    val directProduct by inventoryViewModel.directCheckoutProduct.collectAsState()
    val currentDirectQuantity by inventoryViewModel.directCheckoutQuantity.collectAsState()

    var showPaymentSuccessDialog by remember { mutableStateOf(false) }

    val isDirectPurchase = directProductId != null && directQuantity != null

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            inventoryViewModel.clearError()
        }
    }

    LaunchedEffect(checkoutResult) {
        checkoutResult?.let { success ->
            if (success) {
                showPaymentSuccessDialog = true
            } else {
                Toast.makeText(context, "Checkout gagal. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
            }
            inventoryViewModel.clearCheckoutResult()
        }
    }

    LaunchedEffect(directProductId) {
        Log.d("CheckoutScreen", "LaunchedEffect directProductId: $directProductId, isDirectPurchase: $isDirectPurchase")
        if (isDirectPurchase) {
            Log.d("CheckoutScreen", "Fetching product for direct purchase: $directProductId")
            // Gunakan '!!' di sini karena kita sudah memastikan directProductId tidak null dengan isDirectPurchase
            inventoryViewModel.fetchProductById(directProductId!!)
            directQuantity?.let {
                inventoryViewModel.updateDirectCheckoutQuantity(it)
                Log.d("CheckoutScreen", "Setting direct quantity to: $it")
            }
        } else {
            Log.d("CheckoutScreen", "Fetching cart items for regular checkout.")
            inventoryViewModel.fetchCartItems()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            val totalPrice = if (isDirectPurchase) {
                // Menggunakan 'directProduct?.price' dengan safe call
                (directProduct?.price ?: 0.0) * currentDirectQuantity
            } else {
                cartItems.sumOf { item ->
                    item.product?.price?.times(item.quantity) ?: 0.0
                }
            }

            if ((isDirectPurchase && directProduct != null) || (!isDirectPurchase && cartItems.isNotEmpty())) {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total: ${formatCurrency(totalPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                if (isDirectPurchase) {
                                    // Tangkap nilai directProduct ke variabel lokal yang immutable
                                    val productToCheckout = directProduct
                                    if (productToCheckout != null) {
                                        inventoryViewModel.directCheckout(productToCheckout.id, currentDirectQuantity)
                                    } else {
                                        Toast.makeText(context, "Produk untuk checkout langsung tidak tersedia.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    inventoryViewModel.checkout()
                                }
                            },
                            enabled = !loading
                        ) {
                            if (loading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Konfirmasi Checkout")
                            }
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

            if (isDirectPurchase) {
                directProduct?.let { product ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ringkasan Pembelian Langsung:",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                                        .height(200.dp)
                                        .padding(bottom = 8.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(text = product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(text = formatCurrency(product.price), style = MaterialTheme.typography.bodyLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Stok Tersedia: ${product.stock}", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { inventoryViewModel.updateDirectCheckoutQuantity(currentDirectQuantity - 1) },
                                        enabled = !loading && currentDirectQuantity > 1
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Kurangi Kuantitas")
                                    }
                                    Text(
                                        text = "$currentDirectQuantity",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { inventoryViewModel.updateDirectCheckoutQuantity(currentDirectQuantity + 1) },
                                        enabled = !loading && currentDirectQuantity < product.stock
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Tambah Kuantitas")
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Subtotal: ${formatCurrency(product.price * currentDirectQuantity)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                } ?: run {
                    if (!loading && error == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Produk untuk pembelian langsung tidak ditemukan.")
                        }
                    }
                }
            } else {
                if (cartItems.isEmpty() && !loading && error == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Keranjang Anda kosong. Tidak ada yang perlu di-checkout.")
                    }
                } else {
                    Text(
                        text = "Ringkasan Pesanan:",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cartItems) { item ->
                            CheckoutItemCard(item = item)
                        }
                    }
                }
            }
        }
    }

    if (showPaymentSuccessDialog) {
        PaymentSuccessDialog(
            onDismiss = {
                showPaymentSuccessDialog = false
                navController.navigate(Screen.ProductList.route) {
                    popUpTo(Screen.ProductList.route) { inclusive = true }
                }
            }
        )
    }
}

@Composable
fun CheckoutItemCard(item: CartItem) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val productName = item.product?.name ?: "Unknown Product"
            val productPrice = item.product?.price ?: 0.0
            Column(modifier = Modifier.weight(1f)) {
                Text(text = productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Quantity: ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Price per item: ${formatCurrency(productPrice)}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Subtotal: ${formatCurrency(productPrice * item.quantity)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun PaymentSuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pembayaran Berhasil!") },
        text = { Text("Pesanan Anda telah berhasil ditempatkan. Terima kasih atas pembelian Anda!") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCheckoutScreenWithCart() {
    CheckoutScreen(navController = rememberNavController())
}

@Preview(showBackground = true)
@Composable
fun PreviewCheckoutScreenDirectPurchase() {
    CheckoutScreen(navController = rememberNavController(), directProductId = 1, directQuantity = 1)
}

@Preview(showBackground = true)
@Composable
fun PreviewPaymentSuccessDialog() {
    PaymentSuccessDialog(onDismiss = {})
}