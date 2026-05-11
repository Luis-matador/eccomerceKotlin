package com.example.myapplication.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

data class ProductPopularityProjection(
    @ColumnInfo(name = "product_id") val productId: Int,
    @ColumnInfo(name = "total_sold") val totalSold: Int,
)

data class ProductFavoriteProjection(
    @ColumnInfo(name = "product_id") val productId: Int,
    @ColumnInfo(name = "favorite_count") val favoriteCount: Int,
)

data class CartLineProjection(
    @ColumnInfo(name = "product_id") val productId: Int,
    val title: String,
    val platform: String,
    val price: Double,
    val quantity: Int,
    val stock: Int,
    @ColumnInfo(name = "image_uri") val imageUri: String?,
)

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)],
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    @ColumnInfo(name = "photo_uri") val photoUri: String? = null,
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val platform: String,
    val category: String,
    val description: String,
    val price: Double,
    val stock: Int,
    @ColumnInfo(name = "image_uri") val imageUri: String? = null,
    val featured: Boolean = false,
)

@Entity(
    tableName = "favorites",
    indices = [Index(value = ["user_id", "product_id"], unique = true)],
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "product_id") val productId: Int,
)

@Entity(
    tableName = "cart_items",
    indices = [Index(value = ["user_id", "product_id"], unique = true)],
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "product_id") val productId: Int,
    val quantity: Int,
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "buyer_name") val buyerName: String,
    @ColumnInfo(name = "buyer_email") val buyerEmail: String,
    @ColumnInfo(name = "payment_method") val paymentMethod: String,
    val total: Double,
    @ColumnInfo(name = "created_at") val createdAt: String,
    val status: String,
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "order_id") val orderId: Int,
    @ColumnInfo(name = "product_id") val productId: Int,
    val title: String,
    val platform: String,
    @ColumnInfo(name = "unit_price") val unitPrice: Double,
    val quantity: Int,
    @ColumnInfo(name = "generated_keys") val generatedKeys: String,
)

