package com.example.myapplication.model

data class OrderWithItems(
    val order: Order,
    val items: List<OrderItem>,
)

