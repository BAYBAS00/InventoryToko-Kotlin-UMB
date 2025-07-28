package com.example.inventoritoko.utils

// app/src/main/java/com/example/inventoritoko/utils/Formatter.kt
import java.text.NumberFormat
import java.util.Locale

/**
 * Memformat jumlah Double menjadi string mata uang Rupiah Indonesia.
 * Contoh: 15000.0 akan menjadi "Rp15.000,00"
 */
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")) // Indonesian Rupiah
    return format.format(amount)
}