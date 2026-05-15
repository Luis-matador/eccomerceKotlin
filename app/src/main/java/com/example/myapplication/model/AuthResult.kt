package com.example.myapplication.model

data class AuthResult(
    val success: Boolean,
    val message: String,
    val user: User? = null,
)

