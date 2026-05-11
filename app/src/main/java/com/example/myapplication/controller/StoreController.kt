package com.example.myapplication.controller

import android.content.Context
import com.example.myapplication.model.CartLine
import com.example.myapplication.model.CheckoutResult
import com.example.myapplication.model.OrderWithItems
import com.example.myapplication.model.Product
import com.example.myapplication.model.StoreDatabaseHelper
import com.example.myapplication.model.User

class StoreController(context: Context) {
    private val database = StoreDatabaseHelper(context.applicationContext)
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        ensureCurrentUser()
    }

    fun getCurrentUser(): User {
        ensureCurrentUser()
        val id = prefs.getInt(KEY_CURRENT_USER_ID, -1)
        return database.getUserById(id) ?: database.getUserByRole("user")!!
    }

    fun switchRole(role: String): User {
        val user = database.getUserByRole(role) ?: database.getUserByRole("user")!!
        prefs.edit().putInt(KEY_CURRENT_USER_ID, user.id).apply()
        return user
    }

    fun getProducts(): List<Product> = database.getProducts()

    fun getProduct(productId: Int): Product? = database.getProduct(productId)

    fun addCurrentUserPhoto(photoUri: String?) {
        database.updateUserPhoto(getCurrentUser().id, photoUri)
    }

    fun saveProduct(product: Product): Product = database.saveProduct(product)

    fun getCart(): List<CartLine> = database.getCartLines(getCurrentUser().id)

    fun getCartCount(): Int = database.getCartItemCount(getCurrentUser().id)

    fun getCartTotal(): Double = database.getCartTotal(getCurrentUser().id)

    fun addToCart(productId: Int): String {
        val added = database.addToCart(getCurrentUser().id, productId, 1)
        return if (added) {
            "Producto añadido al carrito"
        } else {
            "No se pudo añadir. Revisa el stock disponible."
        }
    }

    fun increaseCartItem(productId: Int): String {
        val added = database.addToCart(getCurrentUser().id, productId, 1)
        return if (added) "Cantidad actualizada" else "Has alcanzado el stock máximo"
    }

    fun decreaseCartItem(productId: Int) {
        val current = getCart().firstOrNull { it.productId == productId } ?: return
        database.updateCartQuantity(getCurrentUser().id, productId, current.quantity - 1)
    }

    fun removeCartItem(productId: Int) {
        database.removeFromCart(getCurrentUser().id, productId)
    }

    fun checkout(
        buyerName: String,
        buyerEmail: String,
        paymentMethod: String,
    ): CheckoutResult = database.checkout(getCurrentUser().id, buyerName, buyerEmail, paymentMethod)

    fun getOrders(): List<OrderWithItems> = database.getOrders(getCurrentUser().id)

    private fun ensureCurrentUser() {
        if (!prefs.contains(KEY_CURRENT_USER_ID)) {
            val defaultUser = database.getUserByRole("user") ?: return
            prefs.edit().putInt(KEY_CURRENT_USER_ID, defaultUser.id).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "store_session"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }
}

