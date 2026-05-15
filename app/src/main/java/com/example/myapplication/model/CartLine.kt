package com.example.myapplication.model

data class CartLine(
    val productId: Int,
    val title: String,
    val platform: String,
    val price: Double,
    val quantity: Int,
    val stock: Int,
    val imageUri: String? = null,
) {
    val subtotal: Double
        get() = price * quantity
}

