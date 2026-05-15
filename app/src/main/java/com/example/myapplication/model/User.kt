package com.example.myapplication.model

data class User(
    val id: Int = 0,
    val name: String,
    val email: String,
    val role: String,
    val photoUri: String? = null,
)

