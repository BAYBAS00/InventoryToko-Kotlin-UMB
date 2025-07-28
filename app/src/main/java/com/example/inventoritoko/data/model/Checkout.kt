package com.example.inventoritoko.data.model

import com.google.gson.annotations.SerializedName

//data class CheckoutRequest(
//    // Your Node.js checkout endpoint might not need a body,
//    // but if it does, define it here.
//    // For now, assuming it processes the current user's cart.
//)

data class CheckoutResponse(
    val message: String,
    @SerializedName("transactionId")
    val transactionId: Int? = null,
    @SerializedName("totalPrice")
    val totalPrice: Double? = null
)