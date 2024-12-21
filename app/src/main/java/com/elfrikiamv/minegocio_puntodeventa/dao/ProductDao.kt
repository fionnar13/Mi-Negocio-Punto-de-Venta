package com.elfrikiamv.minegocio_puntodeventa.dao

// ProductDao.kt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elfrikiamv.minegocio_puntodeventa.entity.ProductEntity

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("DELETE FROM products WHERE barcode = :barcode")
    suspend fun deleteProductByBarcode(barcode: String)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}
