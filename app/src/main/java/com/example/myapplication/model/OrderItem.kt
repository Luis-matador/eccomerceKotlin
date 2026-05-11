package com.example.myapplication.model

data class OrderItem(
    val id: Int = 0,
    val orderId: Int,
    val productId: Int,
    val title: String,
    val platform: String,
    val unitPrice: Double,
    val quantity: Int,
    val generatedKeys: String,
)

