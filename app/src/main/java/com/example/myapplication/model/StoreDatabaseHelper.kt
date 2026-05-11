package com.example.myapplication.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class StoreDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL,
                role TEXT NOT NULL,
                photo_uri TEXT
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                platform TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT NOT NULL,
                price REAL NOT NULL,
                stock INTEGER NOT NULL,
                image_uri TEXT,
                featured INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE cart_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                UNIQUE(user_id, product_id)
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                buyer_name TEXT NOT NULL,
                buyer_email TEXT NOT NULL,
                payment_method TEXT NOT NULL,
                total REAL NOT NULL,
                created_at TEXT NOT NULL,
                status TEXT NOT NULL
            )
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE order_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                platform TEXT NOT NULL,
                unit_price REAL NOT NULL,
                quantity INTEGER NOT NULL,
                generated_keys TEXT NOT NULL
            )
            """.trimIndent(),
        )

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS order_items")
        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS cart_items")
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun getProducts(): List<Product> {
        val products = mutableListOf<Product>()
        readableDatabase.rawQuery(
            "SELECT id, title, platform, category, description, price, stock, image_uri, featured FROM products ORDER BY featured DESC, title ASC",
            null,
        ).use { cursor ->
            while (cursor.moveToNext()) {
                products += Product(
                    id = cursor.getInt(0),
                    title = cursor.getString(1),
                    platform = cursor.getString(2),
                    category = cursor.getString(3),
                    description = cursor.getString(4),
                    price = cursor.getDouble(5),
                    stock = cursor.getInt(6),
                    imageUri = cursor.getString(7),
                    featured = cursor.getInt(8) == 1,
                )
            }
        }
        return products
    }

    fun getProduct(productId: Int): Product? {
        readableDatabase.rawQuery(
            "SELECT id, title, platform, category, description, price, stock, image_uri, featured FROM products WHERE id = ?",
            arrayOf(productId.toString()),
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                Product(
                    id = cursor.getInt(0),
                    title = cursor.getString(1),
                    platform = cursor.getString(2),
                    category = cursor.getString(3),
                    description = cursor.getString(4),
                    price = cursor.getDouble(5),
                    stock = cursor.getInt(6),
                    imageUri = cursor.getString(7),
                    featured = cursor.getInt(8) == 1,
                )
            } else {
                null
            }
        }
    }

    fun saveProduct(product: Product): Product {
        val values = ContentValues().apply {
            put("title", product.title)
            put("platform", product.platform)
            put("category", product.category)
            put("description", product.description)
            put("price", product.price)
            put("stock", product.stock)
            put("image_uri", product.imageUri)
            put("featured", if (product.featured) 1 else 0)
        }

        val db = writableDatabase
        val productId = if (product.id == 0) {
            db.insert("products", null, values).toInt()
        } else {
            db.update("products", values, "id = ?", arrayOf(product.id.toString()))
            product.id
        }
        return getProduct(productId) ?: product.copy(id = productId)
    }

    fun getUserById(userId: Int): User? {
        readableDatabase.rawQuery(
            "SELECT id, name, email, role, photo_uri FROM users WHERE id = ?",
            arrayOf(userId.toString()),
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                User(
                    id = cursor.getInt(0),
                    name = cursor.getString(1),
                    email = cursor.getString(2),
                    role = cursor.getString(3),
                    photoUri = cursor.getString(4),
                )
            } else {
                null
            }
        }
    }

    fun getUserByRole(role: String): User? {
        readableDatabase.rawQuery(
            "SELECT id, name, email, role, photo_uri FROM users WHERE role = ? LIMIT 1",
            arrayOf(role),
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                User(
                    id = cursor.getInt(0),
                    name = cursor.getString(1),
                    email = cursor.getString(2),
                    role = cursor.getString(3),
                    photoUri = cursor.getString(4),
                )
            } else {
                null
            }
        }
    }

    fun updateUserPhoto(userId: Int, photoUri: String?) {
        val values = ContentValues().apply {
            put("photo_uri", photoUri)
        }
        writableDatabase.update("users", values, "id = ?", arrayOf(userId.toString()))
    }

    fun getCartLines(userId: Int): List<CartLine> {
        val lines = mutableListOf<CartLine>()
        readableDatabase.rawQuery(
            """
            SELECT p.id, p.title, p.platform, p.price, c.quantity, p.stock, p.image_uri
            FROM cart_items c
            INNER JOIN products p ON p.id = c.product_id
            WHERE c.user_id = ?
            ORDER BY p.title ASC
            """.trimIndent(),
            arrayOf(userId.toString()),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lines += CartLine(
                    productId = cursor.getInt(0),
                    title = cursor.getString(1),
                    platform = cursor.getString(2),
                    price = cursor.getDouble(3),
                    quantity = cursor.getInt(4),
                    stock = cursor.getInt(5),
                    imageUri = cursor.getString(6),
                )
            }
        }
        return lines
    }

    fun addToCart(userId: Int, productId: Int, amount: Int = 1): Boolean {
        val product = getProduct(productId) ?: return false
        if (product.stock <= 0) return false

        val currentQty = getCartQuantity(userId, productId)
        if (currentQty >= product.stock) return false
        val newQty = (currentQty + amount).coerceAtMost(product.stock)

        val values = ContentValues().apply {
            put("user_id", userId)
            put("product_id", productId)
            put("quantity", newQty)
        }

        if (currentQty == 0) {
            writableDatabase.insert("cart_items", null, values)
        } else {
            writableDatabase.update(
                "cart_items",
                values,
                "user_id = ? AND product_id = ?",
                arrayOf(userId.toString(), productId.toString()),
            )
        }
        return true
    }

    fun updateCartQuantity(userId: Int, productId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(userId, productId)
            return
        }
        val product = getProduct(productId) ?: return
        val safeQty = quantity.coerceAtMost(product.stock)
        val values = ContentValues().apply {
            put("quantity", safeQty)
        }
        writableDatabase.update(
            "cart_items",
            values,
            "user_id = ? AND product_id = ?",
            arrayOf(userId.toString(), productId.toString()),
        )
    }

    fun removeFromCart(userId: Int, productId: Int) {
        writableDatabase.delete(
            "cart_items",
            "user_id = ? AND product_id = ?",
            arrayOf(userId.toString(), productId.toString()),
        )
    }

    fun getCartItemCount(userId: Int): Int {
        readableDatabase.rawQuery(
            "SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE user_id = ?",
            arrayOf(userId.toString()),
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    fun getCartTotal(userId: Int): Double {
        readableDatabase.rawQuery(
            """
            SELECT COALESCE(SUM(p.price * c.quantity), 0)
            FROM cart_items c
            INNER JOIN products p ON p.id = c.product_id
            WHERE c.user_id = ?
            """.trimIndent(),
            arrayOf(userId.toString()),
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        }
    }

    fun checkout(
        userId: Int,
        buyerName: String,
        buyerEmail: String,
        paymentMethod: String,
    ): CheckoutResult {
        val cart = getCartLines(userId)
        if (cart.isEmpty()) {
            return CheckoutResult(false, "El carrito está vacío")
        }

        for (line in cart) {
            val product = getProduct(line.productId) ?: return CheckoutResult(false, "Un producto ya no está disponible")
            if (product.stock < line.quantity) {
                return CheckoutResult(false, "No hay stock suficiente para ${line.title}")
            }
        }

        val db = writableDatabase
        db.beginTransaction()
        return try {
            val total = cart.sumOf { it.subtotal }
            val orderValues = ContentValues().apply {
                put("user_id", userId)
                put("buyer_name", buyerName)
                put("buyer_email", buyerEmail)
                put("payment_method", paymentMethod)
                put("total", total)
                put("created_at", now())
                put("status", "Entregado")
            }
            val orderId = db.insert("orders", null, orderValues).toInt()

            cart.forEach { line ->
                val keys = generateKeys(line.title, line.quantity)
                val itemValues = ContentValues().apply {
                    put("order_id", orderId)
                    put("product_id", line.productId)
                    put("title", line.title)
                    put("platform", line.platform)
                    put("unit_price", line.price)
                    put("quantity", line.quantity)
                    put("generated_keys", keys)
                }
                db.insert("order_items", null, itemValues)

                db.execSQL(
                    "UPDATE products SET stock = stock - ? WHERE id = ?",
                    arrayOf(line.quantity, line.productId),
                )
            }

            db.delete("cart_items", "user_id = ?", arrayOf(userId.toString()))
            db.setTransactionSuccessful()
            CheckoutResult(true, "Compra completada. Tus claves ya están en pedidos.", orderId)
        } catch (ex: Exception) {
            CheckoutResult(false, ex.message ?: "No se pudo procesar la compra")
        } finally {
            db.endTransaction()
        }
    }

    fun getOrders(userId: Int): List<OrderWithItems> {
        val orders = mutableListOf<OrderWithItems>()
        readableDatabase.rawQuery(
            "SELECT id, user_id, buyer_name, buyer_email, payment_method, total, created_at, status FROM orders WHERE user_id = ? ORDER BY id DESC",
            arrayOf(userId.toString()),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val order = Order(
                    id = cursor.getInt(0),
                    userId = cursor.getInt(1),
                    buyerName = cursor.getString(2),
                    buyerEmail = cursor.getString(3),
                    paymentMethod = cursor.getString(4),
                    total = cursor.getDouble(5),
                    createdAt = cursor.getString(6),
                    status = cursor.getString(7),
                )
                orders += OrderWithItems(order, getOrderItems(order.id))
            }
        }
        return orders
    }

    private fun getOrderItems(orderId: Int): List<OrderItem> {
        val items = mutableListOf<OrderItem>()
        readableDatabase.rawQuery(
            "SELECT id, order_id, product_id, title, platform, unit_price, quantity, generated_keys FROM order_items WHERE order_id = ? ORDER BY id ASC",
            arrayOf(orderId.toString()),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                items += OrderItem(
                    id = cursor.getInt(0),
                    orderId = cursor.getInt(1),
                    productId = cursor.getInt(2),
                    title = cursor.getString(3),
                    platform = cursor.getString(4),
                    unitPrice = cursor.getDouble(5),
                    quantity = cursor.getInt(6),
                    generatedKeys = cursor.getString(7),
                )
            }
        }
        return items
    }

    private fun getCartQuantity(userId: Int, productId: Int): Int {
        readableDatabase.rawQuery(
            "SELECT quantity FROM cart_items WHERE user_id = ? AND product_id = ?",
            arrayOf(userId.toString(), productId.toString()),
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        insertUser(db, "Demo Buyer", "buyer@g2a.local", "user")
        insertUser(db, "Admin Store", "admin@g2a.local", "admin")

        insertProduct(
            db,
            Product(
                title = "Elden Ring Nightreign Key",
                platform = "PC / Steam",
                category = "Juegos",
                description = "Clave digital inmediata para Steam. Incluye bonus de lanzamiento y entrega automática tras el pago.",
                price = 49.99,
                stock = 12,
                featured = true,
            ),
        )
        insertProduct(
            db,
            Product(
                title = "Cyberpunk 2077 Ultimate Edition",
                platform = "PC / GOG",
                category = "Juegos",
                description = "Edición completa con expansión Phantom Liberty. Ideal para catálogo premium tipo G2A.",
                price = 34.90,
                stock = 8,
                featured = true,
            ),
        )
        insertProduct(
            db,
            Product(
                title = "Minecraft Java & Bedrock",
                platform = "PC / Microsoft",
                category = "Keys",
                description = "Licencia oficial para jugar en Java y Bedrock. Entrega instantánea con clave segura.",
                price = 19.99,
                stock = 22,
            ),
        )
        insertProduct(
            db,
            Product(
                title = "PlayStation Store 50€",
                platform = "PlayStation",
                category = "Gift Cards",
                description = "Código digital para recargar tu wallet de PlayStation Store con entrega al instante.",
                price = 46.50,
                stock = 15,
            ),
        )
        insertProduct(
            db,
            Product(
                title = "EA Sports FC 26",
                platform = "Xbox Series",
                category = "Juegos",
                description = "Versión Xbox Series con acceso estándar y activación mediante código digital.",
                price = 54.00,
                stock = 6,
            ),
        )
        insertProduct(
            db,
            Product(
                title = "Game Pass Ultimate 3 Meses",
                platform = "Xbox / PC",
                category = "Suscripciones",
                description = "Suscripción digital para Game Pass Ultimate, ideal para catálogo recurrente.",
                price = 24.75,
                stock = 30,
            ),
        )
    }

    private fun insertUser(db: SQLiteDatabase, name: String, email: String, role: String) {
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("role", role)
        }
        db.insert("users", null, values)
    }

    private fun insertProduct(db: SQLiteDatabase, product: Product) {
        val values = ContentValues().apply {
            put("title", product.title)
            put("platform", product.platform)
            put("category", product.category)
            put("description", product.description)
            put("price", product.price)
            put("stock", product.stock)
            put("image_uri", product.imageUri)
            put("featured", if (product.featured) 1 else 0)
        }
        db.insert("products", null, values)
    }

    private fun now(): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    private fun generateKeys(title: String, quantity: Int): String {
        val prefix = title.take(3).uppercase().padEnd(3, 'X')
        return (1..quantity).joinToString("\n") {
            listOf(prefix, block(), block(), block()).joinToString("-")
        }
    }

    private fun block(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return buildString {
            repeat(4) {
                append(chars[Random.nextInt(chars.length)])
            }
        }
    }

    companion object {
        private const val DATABASE_NAME = "g2a_store.db"
        private const val DATABASE_VERSION = 2
    }
}

