package com.example.myapplication.model

data class CheckoutResult(
    val success: Boolean,
    val message: String,
    val orderId: Int? = null,
)

