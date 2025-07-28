package com.example.inventoritoko.data.model

import com.google.gson.annotations.SerializedName

data class AddToCartRequest(
    @SerializedName("productid")
    val productId: Int,
    val quantity: Int
)

data class UpdateCartQuantityRequest(
    val quantity: Int // Kuantitas baru
)

data class CartItem(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("product_id")
    val productId: Int,
    val quantity: Int,
    val product: Product? // Assuming the API returns product details nested
)