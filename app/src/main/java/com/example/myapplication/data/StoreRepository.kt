package com.example.myapplication.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.room.CartItemEntity
import com.example.myapplication.data.room.FavoriteEntity
import com.example.myapplication.data.room.OrderEntity
import com.example.myapplication.data.room.OrderItemEntity
import com.example.myapplication.data.room.ProductEntity
import com.example.myapplication.data.room.StoreDatabase
import com.example.myapplication.data.room.UserEntity
import com.example.myapplication.model.AuthResult
import com.example.myapplication.model.CartLine
import com.example.myapplication.model.CheckoutResult
import com.example.myapplication.model.Order
import com.example.myapplication.model.OrderItem
import com.example.myapplication.model.OrderWithItems
import com.example.myapplication.model.Product
import com.example.myapplication.model.StoreSortOption
import com.example.myapplication.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class StoreRepository private constructor(context: Context) {

    private val db = Room.databaseBuilder(context.applicationContext, StoreDatabase::class.java, DATABASE_NAME)
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .addCallback(
            object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seed(db)
                }
            },
        )
        .build()

    private val dao = db.storeDao()

    fun login(email: String, password: String): User? =
        dao.login(email.trim().lowercase(), password)?.toModel()

    fun registerUser(name: String, email: String, password: String): AuthResult {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return AuthResult(false, "Completa todos los campos")
        }
        if (!email.contains("@")) {
            return AuthResult(false, "Correo no válido")
        }
        if (password.length < 4) {
            return AuthResult(false, "La contraseña debe tener al menos 4 caracteres")
        }
        if (dao.findUserByEmail(email.trim()) != null) {
            return AuthResult(false, "Ese correo ya está registrado")
        }
        val id = dao.insertUser(
            UserEntity(
                name = name.trim(),
                email = email.trim().lowercase(),
                password = password,
                role = "user",
            ),
        ).toInt()
        return AuthResult(true, "Cuenta creada correctamente", dao.getUserById(id)?.toModel())
    }

    fun getUserById(userId: Int): User? = dao.getUserById(userId)?.toModel()

    fun updateUserPhoto(userId: Int, photoUri: String?) {
        dao.updateUserPhoto(userId, photoUri)
    }

    fun getProducts(
        userId: Int,
        query: String = "",
        category: String? = null,
        favoritesOnly: Boolean = false,
        sortOption: StoreSortOption = StoreSortOption.RELEVANCE,
    ): List<Product> {
        val favoriteIds = dao.getFavoriteIds(userId).toSet()
        var products = dao.getAllProducts().map { it.toModel(it.id in favoriteIds) }

        if (query.isNotBlank()) {
            val normalized = query.trim().lowercase()
            products = products.filter {
                it.title.lowercase().contains(normalized) ||
                    it.platform.lowercase().contains(normalized) ||
                    it.category.lowercase().contains(normalized)
            }
        }

        if (!category.isNullOrBlank() && category != ALL_CATEGORIES) {
            products = products.filter { it.category == category }
        }

        if (favoritesOnly) {
            products = products.filter { it.isFavorite }
        }

        val popularityMap = buildPopularityMap()
        products = when (sortOption) {
            StoreSortOption.PRICE_ASC -> products.sortedBy { it.price }
            StoreSortOption.PRICE_DESC -> products.sortedByDescending { it.price }
            StoreSortOption.POPULARITY -> products.sortedByDescending { popularityMap[it.id] ?: 0 }
            StoreSortOption.RELEVANCE -> products.sortedWith(
                compareByDescending<Product> { it.featured }
                    .thenByDescending { if (query.isBlank()) 0 else relevanceScore(it, query) }
                    .thenBy { it.title },
            )
        }
        return products
    }

    fun getFeaturedProducts(userId: Int, limit: Int = 5): List<Product> =
        getProducts(userId, sortOption = StoreSortOption.POPULARITY).filter { it.featured }.take(limit)

    fun getCategories(): List<String> = dao.getCategories()

    fun getProduct(productId: Int, userId: Int): Product? =
        getProducts(userId).firstOrNull { it.id == productId }

    fun saveProduct(product: Product): Product {
        val entity = ProductEntity(
            id = product.id,
            title = product.title,
            platform = product.platform,
            category = product.category,
            description = product.description,
            price = product.price,
            stock = product.stock,
            imageUri = product.imageUri,
            featured = product.featured,
        )
        val id = if (product.id == 0) {
            dao.insertProduct(entity).toInt()
        } else {
            dao.updateProduct(entity)
            product.id
        }
        return getProduct(id, ADMIN_USER_ID) ?: product.copy(id = id)
    }

    fun toggleFavorite(userId: Int, productId: Int): Boolean {
        val existing = dao.getFavoriteId(userId, productId)
        return if (existing != null) {
            dao.deleteFavorite(userId, productId)
            false
        } else {
            dao.insertFavorite(FavoriteEntity(userId = userId, productId = productId))
            true
        }
    }

    fun getFavoritesCount(userId: Int): Int = dao.getFavoritesCount(userId)

    fun getCartLines(userId: Int): List<CartLine> =
        dao.getCartLines(userId).map {
            CartLine(
                productId = it.productId,
                title = it.title,
                platform = it.platform,
                price = it.price,
                quantity = it.quantity,
                stock = it.stock,
                imageUri = it.imageUri,
            )
        }

    fun addToCart(userId: Int, productId: Int, amount: Int = 1): Boolean {
        val product = dao.getProductEntity(productId) ?: return false
        if (product.stock <= 0) return false
        val currentQty = dao.getCartQuantity(userId, productId) ?: 0
        if (currentQty >= product.stock) return false
        val newQty = (currentQty + amount).coerceAtMost(product.stock)
        dao.insertCartItem(CartItemEntity(userId = userId, productId = productId, quantity = newQty))
        return true
    }

    fun updateCartQuantity(userId: Int, productId: Int, quantity: Int) {
        if (quantity <= 0) {
            dao.removeCartItem(userId, productId)
        } else {
            val stock = dao.getProductEntity(productId)?.stock ?: quantity
            dao.updateCartQuantity(userId, productId, quantity.coerceAtMost(stock))
        }
    }

    fun removeFromCart(userId: Int, productId: Int) {
        dao.removeCartItem(userId, productId)
    }

    fun getCartItemCount(userId: Int): Int = dao.getCartCount(userId)

    fun getCartTotal(userId: Int): Double = dao.getCartTotal(userId)

    fun checkout(userId: Int, buyerName: String, buyerEmail: String, paymentMethod: String): CheckoutResult {
        val cart = getCartLines(userId)
        if (cart.isEmpty()) return CheckoutResult(false, "El carrito está vacío")

        cart.forEach { line ->
            val product = dao.getProductEntity(line.productId) ?: return CheckoutResult(false, "Un producto ya no está disponible")
            if (product.stock < line.quantity) {
                return CheckoutResult(false, "No hay stock suficiente para ${line.title}")
            }
        }

        return db.runInTransaction<CheckoutResult> {
            val total = cart.sumOf { it.subtotal }
            val orderId = dao.insertOrder(
                OrderEntity(
                    userId = userId,
                    buyerName = buyerName,
                    buyerEmail = buyerEmail,
                    paymentMethod = paymentMethod,
                    total = total,
                    createdAt = now(),
                    status = "Entregado",
                ),
            ).toInt()

            val items = cart.map { line ->
                val keys = generateKeys(line.title, line.quantity)
                val current = dao.getProductEntity(line.productId)!!
                dao.updateProduct(current.copy(stock = current.stock - line.quantity))
                OrderItemEntity(
                    orderId = orderId,
                    productId = line.productId,
                    title = line.title,
                    platform = line.platform,
                    unitPrice = line.price,
                    quantity = line.quantity,
                    generatedKeys = keys,
                )
            }
            dao.insertOrderItems(items)
            dao.clearCart(userId)
            CheckoutResult(true, "Compra completada. Tus claves ya están en pedidos.", orderId)
        }
    }

    fun getOrders(userId: Int): List<OrderWithItems> =
        dao.getOrders(userId).map { orderEntity ->
            OrderWithItems(
                order = orderEntity.toModel(),
                items = dao.getOrderItems(orderEntity.id).map { it.toModel() },
            )
        }

    fun getOrder(userId: Int, orderId: Int): OrderWithItems? {
        val order = dao.getOrder(userId, orderId) ?: return null
        return OrderWithItems(order.toModel(), dao.getOrderItems(orderId).map { it.toModel() })
    }

    private fun buildPopularityMap(): Map<Int, Int> {
        val soldMap = dao.getPopularityStats().associate { it.productId to it.totalSold }
        val favoriteMap = dao.getFavoriteStats().associate { it.productId to it.favoriteCount }
        val products = dao.getAllProducts()
        return products.associate { entity ->
            val score = (soldMap[entity.id] ?: 0) * 10 + (favoriteMap[entity.id] ?: 0) * 5 + if (entity.featured) 25 else 0
            entity.id to score
        }
    }

    private fun relevanceScore(product: Product, query: String): Int {
        val normalized = query.trim().lowercase()
        return when {
            product.title.lowercase() == normalized -> 100
            product.title.lowercase().contains(normalized) -> 60
            product.platform.lowercase().contains(normalized) -> 30
            product.category.lowercase().contains(normalized) -> 20
            else -> 0
        }
    }

    private fun UserEntity.toModel() = User(id, name, email, role, photoUri)

    private fun ProductEntity.toModel(isFavorite: Boolean) = Product(
        id = id,
        title = title,
        platform = platform,
        category = category,
        description = description,
        price = price,
        stock = stock,
        imageUri = imageUri,
        featured = featured,
        isFavorite = isFavorite,
    )

    private fun OrderEntity.toModel() = Order(id, userId, buyerName, buyerEmail, paymentMethod, total, createdAt, status)

    private fun OrderItemEntity.toModel() = OrderItem(id, orderId, productId, title, platform, unitPrice, quantity, generatedKeys)

    private fun now(): String = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

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

    private fun seed(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT INTO users(name, email, password, role) VALUES ('Demo Buyer', 'buyer@g2a.local', '1234', 'user')")
        db.execSQL("INSERT INTO users(name, email, password, role) VALUES ('Admin Store', 'admin@g2a.local', 'admin123', 'admin')")
        db.execSQL("INSERT INTO products(title, platform, category, description, price, stock, image_uri, featured) VALUES ('Elden Ring Nightreign Key', 'PC / Steam', 'Juegos', 'Clave digital inmediata para Steam. Incluye bonus de lanzamiento y entrega automática tras el pago.', 49.99, 12, NULL, 1)")
        db.execSQL("INSERT INTO products(title, platform, category, description, price, stock, image_uri, featured) VALUES ('Cyberpunk 2077 Ultimate Edition', 'PC / GOG', 'Juegos', 'Edición completa con expansión Phantom Liberty. Ideal para catálogo premium tipo G2A.', 34.90, 8, NULL, 1)")
        db.execSQL("INSERT INTO products(title, platform, category, description, price, stock, image_uri, featured) VALUES ('Minecraft Java & Bedrock', 'PC / Microsoft', 'Keys', 'Licencia oficial para jugar en Java y Bedrock. Entrega instantánea con clave segura.', 19.99, 22, NULL, 1)")
        db.execSQL("INSERT INTO products(title, platform, category, description, price, stock, image_uri, featured) VALUES ('PlayStation Store 50€', 'PlayStation', 'Gift Cards', 'Código digital para recargar tu wallet de PlayStation Store con entrega al instante.', 46.50, 15, NULL, 0)")
        db.execSQL("INSERT INTO products(title, platform, category, description, price, stock, image_uri, featured) VALUES ('EA Sports FC 26', 'Xbox Series', 'Juegos', 'Versión Xbox Series con acceso estándar y activación mediante código digital.', 54.00, 6, NULL, 0)")
        db.execSQL("INSERT INTO products(title, platform, category, description, price, stock, image_uri, featured) VALUES ('Game Pass Ultimate 3 Meses', 'Xbox / PC', 'Suscripciones', 'Suscripción digital para Game Pass Ultimate, ideal para catálogo recurrente.', 24.75, 30, NULL, 0)")
        db.execSQL("INSERT INTO products(title, platform, category, description, price, stock, image_uri, featured) VALUES ('Nintendo eShop 25€', 'Nintendo Switch', 'Gift Cards', 'Saldo digital para Nintendo eShop, perfecto para recargas rápidas y seguras.', 23.95, 18, NULL, 0)")
    }

    companion object {
        private const val DATABASE_NAME = "g2a_store_room.db"
        const val ALL_CATEGORIES = "Todas"
        private const val ADMIN_USER_ID = 2

        @Volatile
        private var INSTANCE: StoreRepository? = null

        fun getInstance(context: Context): StoreRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoreRepository(context).also { INSTANCE = it }
            }
    }
}

