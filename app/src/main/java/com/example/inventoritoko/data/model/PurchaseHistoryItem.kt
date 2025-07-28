// PurchaseHistoryItem.kt
package com.example.inventoritoko.data.model

import com.google.gson.annotations.SerializedName

data class PurchaseHistoryItem(
    @SerializedName("transactionId")
    val transactionId: Int,
    @SerializedName("transactionTotalPrice")
    val transactionTotalPrice: String?, // String untuk parsing manual
    @SerializedName("transactionCreatedAt")
    val transactionCreatedAt: String?, // String untuk parsing manual
    @SerializedName("itemId")
    val itemId: Int,
    @SerializedName("productId")
    val productId: Int,
    val quantity: Int,
    @SerializedName("itemPrice")
    val itemPrice: String?, // String untuk parsing manual
    @SerializedName("productName")
    val productName: String,
    @SerializedName("productImage")
    val productImage: String?
)