package com.example.myapplication.controller

import android.content.Context
import com.example.myapplication.data.StoreRepository
import com.example.myapplication.model.AuthResult
import com.example.myapplication.model.CartLine
import com.example.myapplication.model.CheckoutResult
import com.example.myapplication.model.OrderWithItems
import com.example.myapplication.model.Product
import com.example.myapplication.model.StoreSortOption
import com.example.myapplication.model.User

class StoreController(context: Context) {
    private val database = StoreRepository.getInstance(context.applicationContext)
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = getCurrentUserOrNull() != null

    fun getCurrentUserOrNull(): User? {
        val id = prefs.getInt(KEY_CURRENT_USER_ID, -1)
        return if (id == -1) null else database.getUserById(id)
    }

    fun getCurrentUser(): User =
        getCurrentUserOrNull() ?: error("No hay usuario autenticado")

    fun login(email: String, password: String): AuthResult {
        val user = database.login(email.trim().lowercase(), password)
        return if (user != null) {
            prefs.edit().putInt(KEY_CURRENT_USER_ID, user.id).apply()
            AuthResult(true, "Bienvenido de nuevo", user)
        } else {
            AuthResult(false, "Correo o contraseña incorrectos")
        }
    }

    fun register(name: String, email: String, password: String): AuthResult {
        val result = database.registerUser(name, email, password)
        if (result.success && result.user != null) {
            prefs.edit().putInt(KEY_CURRENT_USER_ID, result.user.id).apply()
        }
        return result
    }

    fun logout() {
        prefs.edit().remove(KEY_CURRENT_USER_ID).apply()
    }

    fun getProducts(
        query: String = "",
        category: String = StoreRepository.ALL_CATEGORIES,
        favoritesOnly: Boolean = false,
        sortOption: StoreSortOption = StoreSortOption.RELEVANCE,
    ): List<Product> = database.getProducts(requireUserId(), query, category, favoritesOnly, sortOption)

    fun getFeaturedProducts(): List<Product> = database.getFeaturedProducts(requireUserId())

    fun getCategories(): List<String> = listOf(StoreRepository.ALL_CATEGORIES) + database.getCategories()

    fun getProduct(productId: Int): Product? = database.getProduct(productId, requireUserId())

    fun toggleFavorite(productId: Int): Boolean = database.toggleFavorite(requireUserId(), productId)

    fun getFavoritesCount(): Int = database.getFavoritesCount(requireUserId())

    fun addCurrentUserPhoto(photoUri: String?) {
        database.updateUserPhoto(requireUserId(), photoUri)
    }

    fun saveProduct(product: Product): Product = database.saveProduct(product)

    fun deleteProduct(productId: Int) {
        database.deleteProduct(productId)
    }

    fun getCart(): List<CartLine> = database.getCartLines(requireUserId())

    fun getCartCount(): Int = database.getCartItemCount(requireUserId())

    fun getCartTotal(): Double = database.getCartTotal(requireUserId())

    fun addToCart(productId: Int): String {
        val added = database.addToCart(requireUserId(), productId, 1)
        return if (added) {
            "Producto añadido al carrito"
        } else {
            "No se pudo añadir. Revisa el stock disponible."
        }
    }

    fun increaseCartItem(productId: Int): String {
        val added = database.addToCart(requireUserId(), productId, 1)
        return if (added) "Cantidad actualizada" else "Has alcanzado el stock máximo"
    }

    fun decreaseCartItem(productId: Int) {
        val current = getCart().firstOrNull { it.productId == productId } ?: return
        database.updateCartQuantity(requireUserId(), productId, current.quantity - 1)
    }

    fun removeCartItem(productId: Int) {
        database.removeFromCart(requireUserId(), productId)
    }

    fun checkout(
        buyerName: String,
        buyerEmail: String,
        paymentMethod: String,
    ): CheckoutResult = database.checkout(requireUserId(), buyerName, buyerEmail, paymentMethod)

    fun getOrders(): List<OrderWithItems> = database.getOrders(requireUserId())

    fun getOrder(orderId: Int): OrderWithItems? = database.getOrder(requireUserId(), orderId)

    private fun requireUserId(): Int = getCurrentUser().id

    companion object {
        private const val PREFS_NAME = "store_session"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }
}
