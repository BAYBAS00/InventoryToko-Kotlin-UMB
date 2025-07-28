// PurchaseHistoryScreen.kt
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.inventoritoko.data.model.PurchaseHistoryItem // Import model baru
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModel
import com.example.inventoritoko.presentation.viewmodel.InventoryViewModelFactory
import com.example.inventoritoko.utils.Constants
import com.example.inventoritoko.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val inventoryViewModel: InventoryViewModel = viewModel(factory = InventoryViewModelFactory(context))

    val purchaseHistory by inventoryViewModel.purchaseHistory.collectAsState()
    val loading by inventoryViewModel.loading.collectAsState()
    val error by inventoryViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        inventoryViewModel.fetchPurchaseHistory()
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            inventoryViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Pembelian") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
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

            if (purchaseHistory.isEmpty() && !loading && error == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada riwayat pembelian.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(purchaseHistory) { item -> // Iterasi langsung pada PurchaseHistoryItem
                        PurchaseHistoryItemCard(item = item) // Gunakan composable baru
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseHistoryItemCard(item: PurchaseHistoryItem) {
    // Parsing manual untuk itemPrice dan transactionTotalPrice
    val parsedItemPrice = item.itemPrice?.toDoubleOrNull() ?: 0.0
    val parsedTransactionTotalPrice = item.transactionTotalPrice?.toDoubleOrNull() ?: 0.0

    // --- LOGGING AGRESIIF UNTUK DEBUGGING ---
    Log.d("PurchaseHistoryDebug", "Item ID: ${item.itemId}")
    Log.d("PurchaseHistoryDebug", "Raw itemPrice string: '${item.itemPrice}', Parsed: $parsedItemPrice")
    Log.d("PurchaseHistoryDebug", "Raw transactionTotalPrice string: '${item.transactionTotalPrice}', Parsed: $parsedTransactionTotalPrice")
    if (parsedItemPrice == 0.0 && !item.itemPrice.isNullOrEmpty() && item.itemPrice != "0" && item.itemPrice != "0.00") {
        Log.e("PurchaseHistoryDebug", "CRITICAL WARNING: Parsed itemPrice is 0.0 despite non-zero input string '${item.itemPrice}'.")
    }
    if (parsedTransactionTotalPrice == 0.0 && !item.transactionTotalPrice.isNullOrEmpty() && item.transactionTotalPrice != "0" && item.transactionTotalPrice != "0.00") {
        Log.e("PurchaseHistoryDebug", "CRITICAL WARNING: Parsed transactionTotalPrice is 0.0 despite non-zero input string '${item.transactionTotalPrice}'.")
    }
    // --- AKHIR LOGGING AGRESIIF ---

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ID Transaksi: ${item.transactionId}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tanggal Transaksi: ${formatDate(item.transactionCreatedAt)}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total Transaksi: ${formatCurrency(parsedTransactionTotalPrice)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fullImageUrl = if (item.productImage != null) {
                    "${Constants.BASE_URL.removeSuffix("/")}${item.productImage}"
                } else {
                    Constants.NO_IMAGE_PLACEHOLDER_URL
                }
                AsyncImage(
                    model = fullImageUrl,
                    contentDescription = item.productName,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Qty: ${item.quantity} x ${formatCurrency(parsedItemPrice)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Subtotal Item: ${formatCurrency(item.quantity * parsedItemPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Fungsi formatDate tetap sama
fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) {
        Log.e("formatDate", "Input dateString is null or empty.")
        return "Tanggal Tidak Tersedia"
    }
    return try {
        val date: Date?

        val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoParser.timeZone = TimeZone.getTimeZone("UTC")
        isoParser.isLenient = false

        val mysqlParser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        mysqlParser.isLenient = false


        date = try {
            isoParser.parse(dateString)
        } catch (e: Exception) {
            null
        } ?: try {
            mysqlParser.parse(dateString)
        } catch (e: Exception) {
            null
        }

        if (date != null) {
            val formatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
            formatter.timeZone = TimeZone.getDefault()
            formatter.format(date)
        } else {
            Log.e("formatDate", "Failed to parse date string: '$dateString' with known formats.")
            "Format Tanggal Salah"
        }
    } catch (e: Exception) {
        Log.e("formatDate", "Unexpected error during date formatting for '$dateString'", e)
        "Format Tanggal Salah"
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPurchaseHistoryScreen() {
    PurchaseHistoryScreen(rememberNavController())
}

@Preview(showBackground = true)
@Composable
fun PreviewPurchaseHistoryItemCard() {
    val sampleItem = PurchaseHistoryItem(
        transactionId = 123,
        transactionTotalPrice = "150000.00",
        transactionCreatedAt = "2025-07-28T10:30:00.000Z",
        itemId = 1,
        productId = 101,
        quantity = 2,
        itemPrice = "75000.00",
        productName = "Produk Contoh Pembelian",
        productImage = null
    )
    PurchaseHistoryItemCard(item = sampleItem)
}