package com.elfrikiamv.minegocio_puntodeventa.data.repository

// ProductRepository.kt

import com.elfrikiamv.minegocio_puntodeventa.data.dao.ProductDao
import com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * مخزن کالاها. فراخوانی‌های suspend روی نخ IO Room اجرا می‌شوند.
 */
class ProductRepository(private val dao: ProductDao) {

    fun getAll(): Flow<List<ProductEntity>> = dao.getAll()

    fun search(query: String): Flow<List<ProductEntity>> = dao.search(query)

    fun getByCategory(cat: String): Flow<List<ProductEntity>> = dao.getByCategory(cat)

    fun getLowStock(): Flow<List<ProductEntity>> = dao.getLowStock()

    suspend fun getById(id: Long): ProductEntity? = dao.getById(id)

    suspend fun findByBarcode(barcode: String): ProductEntity? = dao.findByBarcode(barcode)

    /** یافتن کالا با کد (برای بارکدهای وزنی). */
    suspend fun findByCode(code: String): ProductEntity? = dao.findByCode(code)

    suspend fun insert(product: ProductEntity): Long = dao.insert(product)

    suspend fun insertAll(products: List<ProductEntity>): List<Long> = dao.insertAll(products)

    suspend fun update(product: ProductEntity) = dao.update(product)

    suspend fun delete(product: ProductEntity) = dao.delete(product)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteAll() = dao.deleteAll()

    /** تغییر موجودی کالا (دلتای مثبت/منفی بر حسب واحد پایه). */
    suspend fun updateStock(productId: Long, delta: Double) = dao.updateStock(productId, delta)

    /** افزایش شمارندهٔ فروش کالا. */
    suspend fun increaseSoldCount(productId: Long, qty: Double) =
        dao.increaseSoldCount(productId, qty)

    /** دسته‌بندی‌های متمایز (برای فیلترها). */
    fun distinctCats(): Flow<List<String>> = dao.distinctCats()

    /** زیردسته‌های متمایز؛ با انتخاب دسته، محدود به همان دسته. */
    fun distinctSubCats(cat: String): Flow<List<String>> = dao.distinctSubCats(cat)

    /** برندهای متمایز (برای فیلترها). */
    fun distinctBrands(): Flow<List<String>> = dao.distinctBrands()

    /** فهرست کامل کالاها (یک‌بار، برای گزارش‌ها). */
    suspend fun getAllOnce(): List<com.elfrikiamv.minegocio_puntodeventa.data.entity.ProductEntity> =
        dao.getAllOnce()
}
