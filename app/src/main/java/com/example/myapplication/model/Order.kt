package com.example.myapplication.model

data class Order(
    val id: Int = 0,
    val userId: Int,
    val buyerName: String,
    val buyerEmail: String,
    val paymentMethod: String,
    val total: Double,
    val createdAt: String,
    val status: String,
)

