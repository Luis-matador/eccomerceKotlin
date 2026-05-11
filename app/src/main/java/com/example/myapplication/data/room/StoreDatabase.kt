package com.example.myapplication.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ProductEntity::class,
        FavoriteEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
}

