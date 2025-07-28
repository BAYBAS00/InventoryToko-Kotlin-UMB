package com.example.inventoritoko.data.model

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val stock: Int,
    val image: String? = null, // Assuming image is a URL or base64 string
    val description: String? = null
)

