package com.example.myapplication.model

data class Product(
    val id: Int = 0,
    val title: String,
    val platform: String,
    val category: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val imageUri: String? = null,
    val featured: Boolean = false,
    val isFavorite: Boolean = false,
)

