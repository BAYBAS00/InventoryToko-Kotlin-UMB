package com.example.inventoritoko.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventoritoko.data.model.CartItem
import com.example.inventoritoko.presentation.navigation.Screen
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModel
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController) {
    val context = LocalContext.current
    val inventoryViewModel: InventoryViewModel = viewModel(factory = InventoryViewModelFactory(context))

    val cartItems by inventoryViewModel.cartItems.collectAsState()
    val loading by inventoryViewModel.loading.collectAsState()
    val error by inventoryViewModel.error.collectAsState()
    val checkoutResult by inventoryViewModel.checkoutResult.collectAsState()

    var showPaymentSuccessDialog by remember { mutableStateOf(false) }

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
                Toast.makeText(context, "Checkout gagal. Silahkan coba lagi.", Toast.LENGTH_SHORT).show()
            }
            inventoryViewModel.clearCheckoutResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                            onClick = { inventoryViewModel.checkout() },
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

            if (cartItems.isEmpty() && !loading && error == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Keranjang Anda kosong. Tidak ada yang di checkout.")
                }
            } else {
                Text(
                    text = "Pesanan Anda:",
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

    if (showPaymentSuccessDialog) {
        PaymentSuccessDialog(
            onDismiss = {
                showPaymentSuccessDialog = false
                navController.navigate(Screen.ProductList.route) {
                    popUpTo(Screen.ProductList.route) { inclusive = true } // Clear back stack
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
        text = { Text("Pesanan Anda telah berhasil dilakukan. Terima kasih atas pembelian Anda!") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCheckoutScreen() {
    CheckoutScreen(rememberNavController())
}

@Preview(showBackground = true)
@Composable
fun PreviewPaymentSuccessDialog() {
    PaymentSuccessDialog(onDismiss = {})
}
