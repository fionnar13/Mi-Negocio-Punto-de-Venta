package com.elfrikiamv.minegocio_puntodeventa.database

// AppDatabase.kt

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.elfrikiamv.minegocio_puntodeventa.converter.ProductListConverter
import com.elfrikiamv.minegocio_puntodeventa.dao.ProductDao
import com.elfrikiamv.minegocio_puntodeventa.dao.TicketDao
import com.elfrikiamv.minegocio_puntodeventa.entity.ProductEntity
import com.elfrikiamv.minegocio_puntodeventa.entity.TicketEntity

@Database(
    entities = [ProductEntity::class, TicketEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(ProductListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "product_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}