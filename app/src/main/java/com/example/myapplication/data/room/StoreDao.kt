package com.example.myapplication.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface StoreDao {

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) AND password = :password LIMIT 1")
    fun login(email: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    fun findUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Int): UserEntity?

    @Query("UPDATE users SET photo_uri = :photoUri WHERE id = :userId")
    fun updateUserPhoto(userId: Int, photoUri: String?)

    @Query("SELECT * FROM products")
    fun getAllProducts(): List<ProductEntity>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getCategories(): List<String>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    fun getProductEntity(productId: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: ProductEntity): Long

    @Update
    fun updateProduct(product: ProductEntity)

    @Query("SELECT product_id FROM favorites WHERE user_id = :userId")
    fun getFavoriteIds(userId: Int): List<Int>

    @Query("SELECT id FROM favorites WHERE user_id = :userId AND product_id = :productId LIMIT 1")
    fun getFavoriteId(userId: Int, productId: Int): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE user_id = :userId AND product_id = :productId")
    fun deleteFavorite(userId: Int, productId: Int)

    @Query("SELECT COUNT(*) FROM favorites WHERE user_id = :userId")
    fun getFavoritesCount(userId: Int): Int

    @Query("SELECT product_id, COUNT(*) AS favorite_count FROM favorites GROUP BY product_id")
    fun getFavoriteStats(): List<ProductFavoriteProjection>

    @Query("SELECT product_id, COALESCE(SUM(quantity), 0) AS total_sold FROM order_items GROUP BY product_id")
    fun getPopularityStats(): List<ProductPopularityProjection>

    @Transaction
    @Query(
        """
        SELECT p.id AS product_id, p.title, p.platform, p.price, c.quantity, p.stock, p.image_uri
        FROM cart_items c
        INNER JOIN products p ON p.id = c.product_id
        WHERE c.user_id = :userId
        ORDER BY p.title ASC
        """,
    )
    fun getCartLines(userId: Int): List<CartLineProjection>

    @Query("SELECT quantity FROM cart_items WHERE user_id = :userId AND product_id = :productId LIMIT 1")
    fun getCartQuantity(userId: Int, productId: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCartItem(cartItem: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE user_id = :userId AND product_id = :productId")
    fun updateCartQuantity(userId: Int, productId: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE user_id = :userId AND product_id = :productId")
    fun removeCartItem(userId: Int, productId: Int)

    @Query("DELETE FROM cart_items WHERE user_id = :userId")
    fun clearCart(userId: Int)

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE user_id = :userId")
    fun getCartCount(userId: Int): Int

    @Query(
        """
        SELECT COALESCE(SUM(p.price * c.quantity), 0)
        FROM cart_items c
        INNER JOIN products p ON p.id = c.product_id
        WHERE c.user_id = :userId
        """,
    )
    fun getCartTotal(userId: Int): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM orders WHERE user_id = :userId ORDER BY id DESC")
    fun getOrders(userId: Int): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE user_id = :userId AND id = :orderId LIMIT 1")
    fun getOrder(userId: Int, orderId: Int): OrderEntity?

    @Query("SELECT * FROM order_items WHERE order_id = :orderId ORDER BY id ASC")
    fun getOrderItems(orderId: Int): List<OrderItemEntity>
}

